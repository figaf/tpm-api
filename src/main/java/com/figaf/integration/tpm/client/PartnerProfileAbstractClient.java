package com.figaf.integration.tpm.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.CreateBusinessEntityRequest;
import com.figaf.integration.tpm.entity.TpmBusinessEntity;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.TpmObjectDetails;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;

import java.util.*;

import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;

@Slf4j
public abstract class PartnerProfileAbstractClient extends BusinessEntityAbstractClient {

    public PartnerProfileAbstractClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    protected abstract TpmObjectType getTpmObjectType();

    protected abstract String getPartnerProfileResource();

    protected abstract String getPartnerProfileResourceById();

    protected abstract String getPartnerProfileSystemsResource();

    protected abstract String getPartnerProfileCommunicationsResource();

    protected abstract String getPartnerProfileConfigurationResource();

    protected abstract String getPartnerProfileConfigSignvalResource();

    public abstract AggregatedTpmObject getAggregatedPartnerProfile(RequestContext requestContext, String partnerProfileId);

    public List<TpmBusinessEntity> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext = {}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            getPartnerProfileResource(),
            response -> {
                JSONArray companiesJsonArray = new JSONArray(response);
                List<TpmBusinessEntity> partnerProfiles = new ArrayList<>();
                for (int i = 0; i < companiesJsonArray.length(); i++) {
                    JSONObject companyJsonObject = companiesJsonArray.getJSONObject(i);
                    TpmBusinessEntity tpmBusinessEntity = buildTpmBusinessEntity(companyJsonObject, getTpmObjectType());
                    partnerProfiles.add(tpmBusinessEntity);
                }
                return partnerProfiles;
            }
        );
    }

    public TpmObjectDetails getById(RequestContext requestContext, String partnerProfileId) {
        log.debug("#getById: requestContext = {}, partnerProfileId = {}", requestContext, partnerProfileId);

        return executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(getPartnerProfileResourceById(), partnerProfileId),
            this::buildTpmObjectDetails
        );
    }

    public String getRawById(RequestContext requestContext, String partnerProfileId) {
        log.debug("#getRawById: requestContext = {}, partnerProfileId = {}", requestContext, partnerProfileId);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(getPartnerProfileResourceById(), partnerProfileId),
            (response) -> response
        );
    }

    public List<System> getPartnerProfileSystems(RequestContext requestContext, String partnerProfileId) {
        log.debug("#getPartnerProfileSystems: requestContext = {}, partnerProfileId = {}", requestContext, partnerProfileId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(getPartnerProfileSystemsResource(), partnerProfileId),
            this::parseSystemsList
        );
    }

    public List<Channel> getPartnerProfileChannels(RequestContext requestContext, String partnerProfileId, String systemId) {
        log.debug("#getPartnerProfileChannels: requestContext = {}, partnerProfileId = {}, systemId = {}", requestContext, partnerProfileId, systemId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(getPartnerProfileCommunicationsResource(), partnerProfileId, systemId),
            this::parseChannelsList
        );
    }

    public ProfileConfiguration resolveProfileConfiguration(RequestContext requestContext, String partnerProfileId) {
        log.debug("#resolveProfileConfiguration: requestContext = {}, partnerProfileId = {}", requestContext, partnerProfileId);
        JSONObject partnerProfileConfig = executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(getPartnerProfileConfigurationResource(), partnerProfileId),
            JSONObject::new
        );
        if (partnerProfileConfig == null) {
            return null;
        }

        JSONArray signatureVerificationConfigurations = executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(getPartnerProfileConfigSignvalResource(), partnerProfileId),
            JSONArray::new
        );
        if (IterableUtils.isEmpty(signatureVerificationConfigurations)) {
            return null;
        }

        JSONObject signatureValidationConfig = new JSONObject();
        JSONObject configurationEntries = new JSONObject();

        signatureValidationConfig.put("ConfigurationType", "SIGNATURE_VALIDATION_CONFIG");
        signatureValidationConfig.put("ConfigurationEntries", configurationEntries);

        for (int i = 0; i < signatureVerificationConfigurations.length(); i++) {
            JSONObject signatureVerificationConfigurationsJSONObject = signatureVerificationConfigurations.getJSONObject(i);
            configurationEntries.put(signatureVerificationConfigurationsJSONObject.getString("Alias"), signatureVerificationConfigurationsJSONObject);
        }

        partnerProfileConfig.put("SignatureValidationConfigurations", signatureValidationConfig);

        return parseProfileConfiguration(partnerProfileConfig);
    }

    public TpmObjectDetails createPartnerProfile(RequestContext requestContext, CreateBusinessEntityRequest createBusinessEntityRequest) {
        log.debug("#createPartnerProfile: requestContext = {}, createBusinessEntityRequest = {}", requestContext, createBusinessEntityRequest);

        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            getPartnerProfileResource(),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateBusinessEntityRequest> requestEntity = new HttpEntity<>(createBusinessEntityRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create trading partner. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }

                return buildTpmObjectDetails(responseEntity.getBody());
            }
        );
    }

    public void deletePartnerProfile(RequestContext requestContext, String partnerProfileId, CreateBusinessEntityRequest createBusinessEntityRequest) {
        log.debug("#deletePartnerProfile: requestContext = {}, partnerProfileId = {}, createBusinessEntityRequest = {}", requestContext, partnerProfileId, createBusinessEntityRequest);

        executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            format(getPartnerProfileResourceById(), partnerProfileId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateBusinessEntityRequest> requestEntity = new HttpEntity<>(createBusinessEntityRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.DELETE, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format("Couldn't delete trading partner. Code: %d, Message: %s", responseEntity.getStatusCode().value(), requestEntity.getBody()));
                }

                return null;
            }
        );
    }

    public System createSystem(RequestContext requestContext, String partnerProfileId, CreateSystemRequest createSystemRequest) {
        log.debug("#createSystem: requestContext = {}, partnerProfileId = {}, createSystemRequest = {}", requestContext, partnerProfileId, createSystemRequest);

        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            format(getPartnerProfileSystemsResource(), partnerProfileId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateSystemRequest> requestEntity = new HttpEntity<>(createSystemRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create system. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                try {
                    return jsonMapper.readValue(responseEntity.getBody(), System.class);
                } catch (JsonProcessingException ex) {
                    throw new ClientIntegrationException("Can't parse System creation response: ", ex);
                }
            }
        );
    }

    public void createCommunication(RequestContext requestContext, String partnerProfileId, String systemId, CreateCommunicationRequest createCommunicationRequest) {
        log.debug("#createCommunication: requestContext = {}, partnerProfileId = {}, systemId = {}, createCommunicationRequest = {}", requestContext, partnerProfileId, systemId, createCommunicationRequest);

        executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            format(getPartnerProfileCommunicationsResource(), partnerProfileId, systemId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateCommunicationRequest> requestEntity = new HttpEntity<>(createCommunicationRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create communication. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

    public void createSignatureVerificationConfiguration(RequestContext requestContext, String partnerProfileId, CreateSignatureVerificationConfigurationRequest createSignatureVerificationConfigurationRequest) {
        log.debug("#createSignatureVerificationConfigurationRequest:  requestContext = {}, partnerProfileId = {}, createSignatureVerificationConfigurationRequest = {}", requestContext, partnerProfileId, createSignatureVerificationConfigurationRequest);

        executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            format(getPartnerProfileConfigSignvalResource(), partnerProfileId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateSignatureVerificationConfigurationRequest> requestEntity = new HttpEntity<>(createSignatureVerificationConfigurationRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create signature verification configurations. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

}
