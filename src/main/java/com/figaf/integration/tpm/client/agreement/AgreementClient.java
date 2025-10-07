package com.figaf.integration.tpm.client.agreement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.agreement.AgreementCreationRequest;
import com.figaf.integration.tpm.entity.agreement.AgreementUpdateRequest;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.*;

import java.util.List;

import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;

@Slf4j
public class AgreementClient extends TpmBaseClient {

    public AgreementClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TpmObjectMetadata> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAll: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            AGREEMENTS_RESOURCE,
            (response) -> new GenericTpmResponseParser().parseResponse(response, TpmObjectType.CLOUD_AGREEMENT)
        );
    }

    public TpmObjectMetadata createAgreement(RequestContext requestContext, AgreementCreationRequest agreementCreationRequest) {
        log.debug("#createAgreement: requestContext = {}, agreementCreationRequest = {}", requestContext, agreementCreationRequest);
        String payload = serializeToJson(agreementCreationRequest);
        return createAgreement(requestContext, payload);
    }

    public TpmObjectMetadata createAgreement(RequestContext requestContext, String agreementCreationRequestPayload) {
        log.debug("#createAgreement: requestContext = {}, agreementCreationRequestPayload = {}", requestContext, agreementCreationRequestPayload);
        return executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            AGREEMENTS_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> requestEntity = new HttpEntity<>(agreementCreationRequestPayload, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create agreement. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                try {
                    return new GenericTpmResponseParser().parseSingleObject(responseEntity.getBody(), TpmObjectType.CLOUD_AGREEMENT);
                } catch (JsonProcessingException e) {
                    throw new ClientIntegrationException("Can't parse response: ", e);
                }

            }
        );
    }

    public void updateAgreement(RequestContext requestContext, String agreementId, AgreementUpdateRequest agreementUpdateRequest) {
        log.debug("#updateAgreement: requestContext = {}, agreementId = {}, agreementUpdateRequest = {}", requestContext, agreementId, agreementUpdateRequest);
        String payload = serializeToJson(agreementUpdateRequest);
        updateAgreement(requestContext, agreementId, payload);
    }

    public void updateAgreement(RequestContext requestContext, String agreementId, String agreementUpdateRequestPayload) {
        log.debug("#updateAgreement: requestContext = {}, agreementId = {}, agreementUpdateRequestPayload = {}", requestContext, agreementId, agreementUpdateRequestPayload);
        executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            format(AGREEMENT_RESOURCE, agreementId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> requestEntity = new HttpEntity<>(agreementUpdateRequestPayload, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.PUT, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't update agreement. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

    public void deleteAgreement(RequestContext requestContext, String agreementId) {
        log.debug("#deleteAgreement: requestContext = {}, agreementId = {}", requestContext, agreementId);
        executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            format(AGREEMENT_RESOURCE, agreementId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<AgreementCreationRequest> requestEntity = new HttpEntity<>(httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.DELETE, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't delete agreement. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

    private String serializeToJson(AgreementCreationRequest agreementCreationRequest) {
        String payload;
        try {
            payload = jsonMapper.writeValueAsString(agreementCreationRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can't serialize payload", e);
        }
        return payload;
    }

}
