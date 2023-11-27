package com.figaf.integration.tpm.client.mig;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.lock.MigLocker;
import com.figaf.integration.tpm.parser.MigResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.function.BiFunction;

@Slf4j
public class MessageImplementationGuidelinesClient extends TpmBaseClient {

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

    public void editMig(RequestContext requestContext, String migVersionId) {
        log.debug("start editMig");
        HttpResponse uploadFileResponse = null;
        try {

            BiFunction<RequestContext, String, String> editMigInvocation = (requestContextArg, pathRequest) -> executeMethod(
                requestContextArg,
                "/api/1.0/user",
                pathRequest,
                (url, csrfToken, restTemplateWrapper) -> callEditMig(url, csrfToken, restTemplateWrapper.getRestTemplate())
            );
            //1 step of edit button
            MigLocker.lockMigObject(requestContext, migVersionId, editMigInvocation);
            //save
            //canc
        } catch (ClientIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error occurred while file uploading " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while file uploading: " + ex.getMessage(), ex);
        } finally {
            HttpClientUtils.closeQuietly(uploadFileResponse);
        }
    }

    private String callEditMig(String url, String csrfToken, RestTemplate restTemplate) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("X-CSRF-Token", csrfToken);
            httpHeaders.setContentType(MediaType.APPLICATION_XML);

            HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                return responseEntity.getBody();
            } else {
                throw new ClientIntegrationException(String.format(
                    "Couldn't get statisticOverviewCommandResponse. Code: %d, Message: %s",
                    responseEntity.getStatusCode().value(),
                    requestEntity.getBody())
                );
            }

        } catch (Exception ex) {
            log.error("Error occurred while getting statisticOverviewCommandResponse: " + ex.getMessage(), ex);
            throw new ClientIntegrationException("Error occurred while getting statisticOverviewCommandResponse: " + ex.getMessage(), ex);
        }
    }
}

