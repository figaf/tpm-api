package com.figaf.integration.tpm.client.b2bscenario;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.B2BScenarioMetadata;
import com.figaf.integration.tpm.parser.B2BScenarioResponseParser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.lang.String.format;

@Slf4j
public class B2BScenarioClient extends TpmBaseClient {

    public B2BScenarioClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<B2BScenarioMetadata> getB2BScenariosByAgreementId(RequestContext requestContext, String agreementId) {
        log.debug("#getB2BScenariosByAgreementId: requestContext = {}, agreementId = {}", requestContext, agreementId);
        return executeGet(
            requestContext,
            format(B2B_SCENARIOS_RESOURCE, agreementId),
            (response) -> new B2BScenarioResponseParser().parseResponse(response, agreementId)
        );
    }

}
