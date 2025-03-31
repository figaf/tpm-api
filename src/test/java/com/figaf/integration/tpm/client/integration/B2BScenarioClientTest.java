package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.agreement.AgreementClient;
import com.figaf.integration.tpm.client.b2bscenario.B2BScenarioClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.entity.B2BScenarioMetadata;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class B2BScenarioClientTest {

    private static AgreementClient agreementClient;
    private static B2BScenarioClient b2BScenarioClient;

    @BeforeAll
    static void setUp() {
        HttpClientsFactory httpClientsFactory = new HttpClientsFactory();
        agreementClient = new AgreementClient(httpClientsFactory);
        b2BScenarioClient = new B2BScenarioClient(httpClientsFactory);
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getB2BScenariosByAgreementId(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        List<TpmObjectMetadata> agreements = agreementClient.getAllMetadata(requestContext);

        List<B2BScenarioMetadata> b2BScenarios = new ArrayList<>();
        for (TpmObjectMetadata agreement : agreements) {
            b2BScenarios.addAll(b2BScenarioClient.getB2BScenariosByAgreementId(requestContext, agreement.getObjectId()));
        }

        assertThat(b2BScenarios).isNotEmpty();
    }
}