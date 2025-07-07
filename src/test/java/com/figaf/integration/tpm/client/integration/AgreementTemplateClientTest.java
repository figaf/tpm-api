package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.agreement.AgreementTemplateClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.entity.B2BScenarioInAgreementTemplate;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class AgreementTemplateClientTest {
    private static final String METADATA_NOT_NULL_MSG = "Actual agreementTemplatesResponse metadata not to be null.";

    private static AgreementTemplateClient agreementTemplateClient;

    @BeforeAll
    static void setUp() {
        agreementTemplateClient = new AgreementTemplateClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllMetadata(AgentTestData agentTestData) {
        log.debug("#test_getAllMetadata: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmObjectMetadata> agreementTemplates = agreementTemplateClient.getAllMetadata(requestContext);

        assertThat(agreementTemplates).as(METADATA_NOT_NULL_MSG).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getB2BScenariosForAgreementTemplate(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmObjectMetadata> agreementTemplates = agreementTemplateClient.getAllMetadata(requestContext);

        List<B2BScenarioInAgreementTemplate> allB2bScenarios = new ArrayList<>();
        for (TpmObjectMetadata agreementTemplate : agreementTemplates) {
            allB2bScenarios.addAll(agreementTemplateClient.getB2BScenariosForAgreementTemplate(agreementTemplate.getObjectId(), agreementTemplate.getB2bScenarioDetailsId(), requestContext));
        }

        assertThat(allB2bScenarios).isNotEmpty();
        allB2bScenarios.forEach(b2bScenario -> assertThat(b2bScenario).hasNoNullFieldsOrProperties());
    }
}
