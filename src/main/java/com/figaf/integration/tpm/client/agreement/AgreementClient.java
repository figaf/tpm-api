package com.figaf.integration.tpm.client.agreement;

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

    public String createAgreement(RequestContext requestContext, AgreementCreationRequest agreementCreationRequest) {
        log.debug("#createAgreement: requestContext = {}, agreementCreationRequest = {}", requestContext, agreementCreationRequest);
        return executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            AGREEMENTS_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<AgreementCreationRequest> requestEntity = new HttpEntity<>(agreementCreationRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create agreement. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return new JSONObject(responseEntity.getBody()).getString("id");
            }
        );
    }

    public void updateAgreement(RequestContext requestContext, String agreementId, AgreementUpdateRequest agreementUpdateRequest) {
        log.debug("#updateAgreement: requestContext = {}, agreementId = {}, agreementUpdateRequest = {}", requestContext, agreementId, agreementUpdateRequest);
        executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            format(AGREEMENT_RESOURCE, agreementId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<AgreementCreationRequest> requestEntity = new HttpEntity<>(agreementUpdateRequest, httpHeaders);
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

}
