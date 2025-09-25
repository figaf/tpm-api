package com.figaf.integration.tpm.client.b2bscenario;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.B2BScenarioMetadata;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.parser.B2BScenarioResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.http.*;

import java.util.List;

import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;

@Slf4j
public class B2BScenarioClient extends TpmBaseClient {

    public B2BScenarioClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<B2BScenarioMetadata> getB2BScenariosForAgreement(RequestContext requestContext, TpmObjectMetadata agreementMetadata) {
        log.debug("#getB2BScenariosForAgreement: requestContext = {}, agreementMetadata = {}", requestContext, agreementMetadata);
        return executeGet(
            requestContext,
            format(B2B_SCENARIOS_RESOURCE, agreementMetadata.getObjectId()),
            (response) -> new B2BScenarioResponseParser().parseResponse(response, agreementMetadata)
        );
    }

    public JSONObject getB2BScenariosForAgreementAsJsonObject(RequestContext requestContext, String agreementId) {
        log.debug("#getB2BScenariosForAgreementAsJsonResponse: requestContext = {}, agreementId = {}", requestContext, agreementId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(B2B_SCENARIOS_RESOURCE, agreementId),
            response -> {
                Object object = new JSONTokener(response).nextValue();
                if (object instanceof JSONArray jsonArray) {
                    return jsonArray.getJSONObject(0);
                } else if (object instanceof JSONObject jsonObject) {
                   return jsonObject;
                } else {
                    throw new JSONException("Unexpected JSON type: " + object.getClass());
                }
            }
        );
    }

    public void updateB2BScenario(RequestContext requestContext, String agreementId, String b2BScenarioDetailsId, String requestBody) {
        log.debug("#updateB2BScenario: requestContext = {}, agreementId  = {}, b2BScenarioDetailsId = {}, requestBody = {}", requestContext, agreementId, b2BScenarioDetailsId, requestBody);
        executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            format(B2B_SCENARIO_RESOURCE, agreementId, b2BScenarioDetailsId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.PUT, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't update B2B Scenario. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

}
