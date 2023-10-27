package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.agreement.AgreementTemplateClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.entity.agreement.AgreementTemplate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class AgreementTemplateClientTest {
    private static final String EXPECTED_NOT_NULL_MSG = "Expected agreement templates not to be null.";

    private static AgreementTemplateClient agreementTemplateClient;

    @BeforeAll
    static void setUp() {
        agreementTemplateClient = new AgreementTemplateClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllAgreementTemplate(AgentTestData agentTestData) {
        log.debug("#test_getAll: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<AgreementTemplate> agreementTemplates = agreementTemplateClient.getAll(requestContext);

        assertThat(agreementTemplates).as(EXPECTED_NOT_NULL_MSG).isNotNull();
    }
}
