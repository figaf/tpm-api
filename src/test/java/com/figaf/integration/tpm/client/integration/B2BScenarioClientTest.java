package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.agreement.AgreementClient;
import com.figaf.integration.tpm.client.b2bscenario.B2BScenarioClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
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
    void test_getB2BScenariosForAgreement(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        List<TpmObjectMetadata> agreements = agreementClient.getAllMetadata(requestContext);

        List<B2BScenarioMetadata> b2BScenarios = new ArrayList<>();
        for (TpmObjectMetadata agreement : agreements) {
            b2BScenarios.addAll(b2BScenarioClient.getB2BScenariosForAgreement(requestContext, agreement));
        }

        assertThat(b2BScenarios).isNotEmpty();

        List<TpmObjectReference> tpmObjectReferences = new ArrayList<>();
        List<MigMetadata> senderMigMetadataList = new ArrayList<>();
        List<MigMetadata> receiverMigMetadataList = new ArrayList<>();
        List<MagMetadata> magMetadataList = new ArrayList<>();
        List<CommunicationChannelTemplateMetadata> senderCommunicationMetadataList = new ArrayList<>();
        List<CommunicationChannelTemplateMetadata> receiverCommunicationMetadataList = new ArrayList<>();
        for (B2BScenarioMetadata b2BScenario : b2BScenarios) {
            tpmObjectReferences.addAll(b2BScenario.getTpmObjectReferences());
            if (b2BScenario.getSenderMigMetadata() != null) {
                senderMigMetadataList.add(b2BScenario.getSenderMigMetadata());
            }
            if (b2BScenario.getReceiverMigMetadata() != null) {
                receiverMigMetadataList.add(b2BScenario.getReceiverMigMetadata());
            }
            if (b2BScenario.getMagMetadata() != null) {
                magMetadataList.add(b2BScenario.getMagMetadata());
            }
            if (b2BScenario.getSenderCommunicationChannelMetadata() != null) {
                senderCommunicationMetadataList.add(b2BScenario.getSenderCommunicationChannelMetadata());
            }
            if (b2BScenario.getReceiverCommunicationChannelMetadata() != null) {
                receiverCommunicationMetadataList.add(b2BScenario.getReceiverCommunicationChannelMetadata());
            }
        }


        assertThat(tpmObjectReferences).isNotEmpty();
        assertThat(senderMigMetadataList).isNotEmpty();
        assertThat(receiverMigMetadataList).isNotEmpty();
        assertThat(magMetadataList).isNotEmpty();
        assertThat(senderCommunicationMetadataList).isNotEmpty();
        assertThat(receiverCommunicationMetadataList).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getB2BScenariosForAgreementAsJsonObject(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmObjectMetadata> agreements = agreementClient.getAllMetadata(requestContext);
        for (TpmObjectMetadata agreement : agreements) {
            JSONObject b2BScenariosForAgreementAsJsonObject = b2BScenarioClient.getB2BScenariosForAgreementAsJsonObject(requestContext, agreement.getObjectId());
            assertThat(b2BScenariosForAgreementAsJsonObject).isNotNull();
        }
    }

}