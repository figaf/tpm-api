package com.figaf.integration.tpm.client.mig;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.lock.MigLocker;
import com.figaf.integration.tpm.entity.mig.DraftCreationResponse;
import com.figaf.integration.tpm.exception.MigParseException;
import com.figaf.integration.tpm.parser.MigResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.zip.*;

import static java.lang.String.format;

@Slf4j
public class MessageImplementationGuidelinesClient extends TpmBaseClient {

    private static final String PATH_FOR_TOKEN = "/api/1.0/user";

    public MessageImplementationGuidelinesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TpmObjectMetadata> getAllLatestMetadata(RequestContext requestContext) {
        log.debug("#getAllLatestMetadata: requestContext={}", requestContext);
        return executeGet(
            requestContext,
            MIG_RESOURCE,
            (response) -> new MigResponseParser().parseJsonToTpmObjectMetadata(response)
        );
    }

    public String getRawById(String migVersionId, RequestContext requestContext) {
        log.debug("#getRawById: migVersionId={}, requestContext={}", migVersionId, requestContext);

        return executeGet(
            requestContext,
            String.format(MIG_RESOURCE_BY_ID, migVersionId),
            (response) -> response
        );
    }

    public void saveAllSegmentsAndFields(RequestContext requestContext, String migVersionId) {
        log.debug("#saveAllSegmentsAndFields: requestContext={}, migVersionId={}", requestContext, migVersionId);
        executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            format(MIG_RESOURCE_BY_ID, migVersionId),
            (url, token, restTemplateWrapper) -> {
                try {
                    saveAllSegmentsAndFields(
                        requestContext,
                        url,
                        token,
                        migVersionId,
                        restTemplateWrapper.getRestTemplate()
                    );
                } catch (IOException e) {
                    throw new MigParseException(e.getMessage(), e);
                }
                return null;
            }
        );
    }

    public void deleteDraftMig(RequestContext requestContext, String name, String migVersionId) {
        log.debug("#deleteDraftMig: requestContext={}, name={}, migVersionId={}", requestContext, name, migVersionId);
        executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            format(MIG_DELETE_DRAFT_RESOURCE, name, migVersionId),
            (url, token, restTemplateWrapper) -> {
                deleteDraftVersion(
                    requestContext,
                    url,
                    token,
                    migVersionId,
                    restTemplateWrapper.getRestTemplate()
                );
                return null;
            }
        );
    }

    public DraftCreationResponse createDraftWithAllSegmentsAndFieldsSelected(RequestContext requestContext, String name, String sourceMigVersionId) {
        log.debug("#createDraftWithAllSegmentsAndFieldsSelected: requestContext={}, name={}, sourceMigVersionId={}", requestContext, name, sourceMigVersionId);
        return executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            format(MIG_CREATE_DRAFT_RESOURCE, name, sourceMigVersionId),
            (url, token, restTemplateWrapper) -> createDraftWithAllSegmentsAndFieldsSelected(
                requestContext,
                url,
                token,
                sourceMigVersionId,
                restTemplateWrapper.getRestTemplate()
            )
        );
    }

    private void saveAllSegmentsAndFields(
        RequestContext requestContext,
        String url,
        String userApiCsrfToken,
        String migVersionId,
        RestTemplate restTemplate
    ) throws IOException {
        boolean locked = false;
        String rawMigWithAllSegmentsAndFields = getRawById(migVersionId, requestContext);
        String rawMigWithAllFieldsSelectedToTrue = createRequestWithSelectedTrueToAllFields(rawMigWithAllSegmentsAndFields);
        try {
            MigLocker.lockMigObject(requestContext, userApiCsrfToken, restTemplate, migVersionId);
            locked = true;

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Accept-Encoding", "gzip, deflate, br");
            httpHeaders.set("Content-Type", "application/json");
            httpHeaders.set("Content-encoding", "application/x-zip");
            httpHeaders.add("X-CSRF-Token", userApiCsrfToken);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
                ZipEntry zipEntry = new ZipEntry("mig.json");
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(rawMigWithAllFieldsSelectedToTrue.getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
            byte[] zipData = byteArrayOutputStream.toByteArray();
            String base64Encoded = Base64.getEncoder().encodeToString(zipData);

            HttpEntity<String> requestEntity = new HttpEntity<>(base64Encoded, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

            if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                throw new ClientIntegrationException(String.format(
                    "Couldn't edit Mig. Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    requestEntity.getBody())
                );
            }
        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred while editing mig" + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while editing mig: " + ex.getMessage(), ex);
        } finally {
            if (locked) {
                MigLocker.unlockMig(requestContext, userApiCsrfToken, restTemplate, migVersionId);
            }
        }
    }

    private DraftCreationResponse createDraftWithAllSegmentsAndFieldsSelected(
        RequestContext requestContext,
        String url,
        String userApiCsrfToken,
        String sourceMigVersionId,
        RestTemplate restTemplate
    ) {
        boolean locked = false;
        DraftCreationResponse draftCreationResponse;
        try {
            MigLocker.lockMigObject(requestContext, userApiCsrfToken, restTemplate, sourceMigVersionId);
            locked = true;

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Content-Type", "application/json");
            httpHeaders.add("X-CSRF-Token", userApiCsrfToken);

            HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
                String rawDraftCreationResponse = responseEntity.getBody();
                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                try {
                    draftCreationResponse = objectMapper.readValue(rawDraftCreationResponse, DraftCreationResponse.class);
                    saveAllSegmentsAndFields(requestContext, draftCreationResponse.getId());
                } catch (IOException e) {
                    log.error(e.getMessage());
                    throw new ClientIntegrationException("Error occurred during mig draft creation: " + e.getMessage(), e);
                }
            } else {
                throw new ClientIntegrationException(String.format(
                    "Couldn't create mig draft version. Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    requestEntity.getBody())
                );
            }

        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred during mig draft creation" + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred during mig draft creation: " + ex.getMessage(), ex);
        } finally {
            if (locked) {
                MigLocker.unlockMig(requestContext, userApiCsrfToken, restTemplate, sourceMigVersionId);
            }
        }
        return draftCreationResponse;
    }

    private void deleteDraftVersion(
        RequestContext requestContext,
        String url,
        String userApiCsrfToken,
        String migVersionId,
        RestTemplate restTemplate
    ) {
        boolean locked = false;
        try {
            MigLocker.lockMigObject(requestContext, userApiCsrfToken, restTemplate, migVersionId);
            locked = true;

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Content-Type", "application/json");
            httpHeaders.add("X-CSRF-Token", userApiCsrfToken);

            HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                throw new ClientIntegrationException(String.format(
                    "Couldn't delete mig draft version. Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    requestEntity.getBody())
                );
            }

        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred during mig draft deletion" + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred during mig draft deletion: " + ex.getMessage(), ex);
        } finally {
            if (locked) {
                MigLocker.unlockMig(requestContext, userApiCsrfToken, restTemplate, migVersionId);
            }
        }
    }

    private String createRequestWithSelectedTrueToAllFields(String rawMigWithAllSegmentsAndFields) throws
        IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(rawMigWithAllSegmentsAndFields);
        updateAllIsSelected(rootNode);

        return mapper.writeValueAsString(rootNode);
    }

    private static void updateAllIsSelected(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            JsonNode isSelectedNode = objectNode.get("IsSelected");
            if (isSelectedNode != null && isSelectedNode.isBoolean() && !isSelectedNode.booleanValue()) {
                objectNode.put("IsSelected", true);
            }

            for (JsonNode childNode : objectNode) {
                updateAllIsSelected(childNode);
            }
        } else if (node.isArray()) {
            for (JsonNode childNode : node) {
                updateAllIsSelected(childNode);
            }
        }
    }
}

