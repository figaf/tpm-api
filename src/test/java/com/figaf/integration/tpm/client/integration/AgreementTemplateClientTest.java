package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.AgreementTemplateClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.AgreementTemplateMetadata;
import com.figaf.integration.tpm.entity.B2BScenarioInAgreementTemplate;
import com.figaf.integration.tpm.entity.TpmObjectReference;
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

        List<AgreementTemplateMetadata> agreementTemplateMetadataList = agreementTemplateClient.getAllMetadata(requestContext);

        assertThat(agreementTemplateMetadataList).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getB2BScenariosForAgreementTemplate(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<AgreementTemplateMetadata> agreementTemplates = agreementTemplateClient.getAllMetadata(requestContext);

        List<B2BScenarioInAgreementTemplate> allB2bScenarios = new ArrayList<>();
        for (AgreementTemplateMetadata agreementTemplate : agreementTemplates) {
            allB2bScenarios.addAll(agreementTemplateClient.getB2BScenariosForAgreementTemplate(requestContext, agreementTemplate.getObjectId(), agreementTemplate.getB2bScenarioDetailsId()));
        }

        assertThat(allB2bScenarios).isNotEmpty();
        allB2bScenarios.forEach(b2bScenario -> assertThat(b2bScenario).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAgreementTemplateIntegrationAdvisoryLinks(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<AgreementTemplateMetadata> agreementTemplates = agreementTemplateClient.getAllMetadata(requestContext);

        List<TpmObjectReference> allIntegrationAdvisoryLinks = new ArrayList<>();
        for (AgreementTemplateMetadata agreementTemplate : agreementTemplates) {
            allIntegrationAdvisoryLinks.addAll(
                agreementTemplateClient.getAgreementTemplateIntegrationAdvisoryLinks(requestContext, agreementTemplate.getObjectId(), agreementTemplate.getB2bScenarioDetailsId())
            );
        }

        assertThat(allIntegrationAdvisoryLinks).isNotEmpty();
        allIntegrationAdvisoryLinks.forEach(tpmObjectReference -> assertThat(tpmObjectReference).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void getB2bScenarioDetailsAdministrativeData(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<AgreementTemplateMetadata> agreementTemplates = agreementTemplateClient.getAllMetadata(requestContext);

        List<AdministrativeData> b2bScenarioDetailsAdministrativeDatas = new ArrayList<>();
        for (AgreementTemplateMetadata agreementTemplate : agreementTemplates) {
            b2bScenarioDetailsAdministrativeDatas.add(agreementTemplateClient.getB2bScenarioDetailsAdministrativeData(requestContext, agreementTemplate.getObjectId(), agreementTemplate.getB2bScenarioDetailsId()));
        }

        assertThat(b2bScenarioDetailsAdministrativeDatas).isNotEmpty();
        b2bScenarioDetailsAdministrativeDatas.forEach(b2bScenarioDetailsAdministrativeData -> assertThat(b2bScenarioDetailsAdministrativeData).hasNoNullFieldsOrProperties());
    }

}
