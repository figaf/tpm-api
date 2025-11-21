package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.AgreementClient;
import com.figaf.integration.tpm.client.AgreementTasksClient;
import com.figaf.integration.tpm.data_provider.AgentDataExtension;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.agreement.AgreementCreationRequest;
import com.figaf.integration.tpm.entity.agreement.AliasWrapper;
import com.figaf.integration.tpm.entity.agreement.CompanyData;
import com.figaf.integration.tpm.entity.agreement.IdWrapper;
import com.figaf.integration.tpm.entity.agreement.TradingPartnerData;
import com.figaf.integration.tpm.entity.agreement.TradingPartnerDetails;
import com.figaf.integration.tpm.entity.agreement.TransactionOption;
import com.figaf.integration.tpm.entity.agreement_tasks.AgreementTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.List;

import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksAction.DEPLOY_V2;
import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksAction.REDEPLOY_V2;
import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksAction.UNDEPLOY_V2;
import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksArtifactType.TRADING_PARTNER_AGREEMENT_V2;
import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksStatus.COMPLETED;
import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksStatus.FAILED;
import static com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksStatus.IN_PROCESS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgreementTasksClientTest {

    @RegisterExtension
    private static AgentDataExtension agentDataExtension = new AgentDataExtension();
    private static RequestContext requestContext;
    private static AgentTestData agentTestData;
    private static String agreementId;

    private static AgreementClient agreementClient;
    private static AgreementTasksClient agreementTasksClient;

    @BeforeAll
    static void setUp() {
        // Create Agent Test Data
        agentTestData = agentDataExtension.get();
        requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        // Create Agreement client and Agreement tasks client instances
        agreementClient = new AgreementClient(new HttpClientsFactory());
        agreementTasksClient = new AgreementTasksClient(new HttpClientsFactory());
    }

    @BeforeEach
    void beforeEach() {
        // Create Agreement
        agreementId = createAgreement(requestContext);
    }

    @AfterEach
    void afterEach() {
        // Delete Agreement
        deleteAgreement(requestContext, agreementId);
    }

    @Test
    @DisplayName("Get Agreement Task Status")
    void test_checkAgreementStatus() {
        log.debug("#test_checkAgreementStatus: agentTestData={}, agreementId={}", agentTestData, agreementId);

        //Step 1: Deploy Agreement Tasks
        AgreementTaskResponse deployResponse = agreementTasksClient.deployAgreement(requestContext, agreementId);
        log.debug("#test_checkAgreementStatus: Agreement Tasks Deployed agentTestData={}, deployResponse={}", agentTestData, deployResponse);

        String taskId = deployResponse.taskId();
        assertThat(taskId).isNotNull();

        // Step 2: Get the Agreement tasks status
        AgreementTaskResponse statusResponse = agreementTasksClient.checkAgreementStatus(requestContext, taskId);
        log.debug("#test_checkAgreementStatus: Agreement Tasks Status Retrieved TestData={}, statusResponse={}", agentTestData, statusResponse);

        assertThat(statusResponse).isNotNull();
        assertThat(statusResponse.taskId()).isNotNull();
        assertThat(statusResponse.taskId()).isEqualTo(taskId);
        assertThat(statusResponse.taskInput().action()).isEqualTo(DEPLOY_V2);
        assertThat(statusResponse.taskInput().taskInput().artifactType()).isEqualTo(TRADING_PARTNER_AGREEMENT_V2);
        assertThat(statusResponse.taskInput().taskInput().semanticVersion()).isEqualTo("2.0");
        assertThat(statusResponse.taskInput().taskParameters().btList()).isEmpty();
        assertThat(statusResponse.executionStatus()).isNotNull();
        assertThat(statusResponse.executionStatus().status()).isNotNull();
        assertThat(statusResponse.executionStatus().status()).isIn(IN_PROCESS.name(), COMPLETED.name(), FAILED.name());
    }

    @Test
    @DisplayName("Deploy Agreement Task")
    void test_deployAgreement() {
        log.debug("#test_deployAgreement: agentTestData={}, agreementId={}", agentTestData, agreementId);

        // Deploy Agreement Tasks
        AgreementTaskResponse response = agreementTasksClient.deployAgreement(requestContext, agreementId);
        log.debug("#test_deployAgreement: Agreement Tasks Deployed agreementId={}, response={}", agreementId, response);

        assertThat(response).isNotNull();
        assertThat(response.taskId()).isNotNull();
        assertThat(response.taskInput().action()).isEqualTo(DEPLOY_V2);
        assertThat(response.taskInput().taskInput().id()).isEqualTo(agreementId);
        assertThat(response.taskInput().taskInput().artifactType()).isEqualTo(TRADING_PARTNER_AGREEMENT_V2);
        assertThat(response.taskInput().taskInput().semanticVersion()).isEqualTo("2.0");
        assertThat(response.taskInput().taskParameters().btList()).isEmpty();
    }

    @Test
    @DisplayName("Undeploy Agreement Task")
    void test_undeployAgreement() {
        log.debug("#test_undeployAgreement: agentTestData={}, agreementId={}", agentTestData, agreementId);

        // Undeploy Agreement Tasks
        AgreementTaskResponse response = agreementTasksClient.undeployAgreement(requestContext, agreementId);
        log.debug("#test_undeployAgreement: Agreement Tasks Undeployed agreementId={}, response={}", agreementId, response);

        assertThat(response).isNotNull();
        assertThat(response.taskId()).isNotNull();
        assertThat(response.taskId()).isEqualTo(String.join("::", UNDEPLOY_V2.name(), agreementId));
        assertThat(response.taskInput().action()).isEqualTo(UNDEPLOY_V2);
        assertThat(response.taskInput().taskInput().artifactType()).isEqualTo(TRADING_PARTNER_AGREEMENT_V2);
        assertThat(response.taskInput().taskInput().semanticVersion()).isEqualTo("2.0");
        assertThat(response.taskInput().taskParameters().btList()).isEmpty();
    }

    @Test
    @DisplayName("Redeploy Agreement Task")
    void test_redeployAgreementTasks() {
        log.debug("#test_redeployAgreementTasks: agentTestData={}", agentTestData);

        List<String> btList = List.of("bt-id-1", "bt-id-2", "bt-id-3");

        // Redeploy Agreement Tasks
        AgreementTaskResponse response = agreementTasksClient.redeployAgreement(requestContext, agreementId, btList);
        log.debug("#test_redeployAgreementTasks: Agreement Tasks Redeployed  agentTestData={}, deployResponse={}", agentTestData, response);

        assertThat(response).isNotNull();
        assertThat(response.taskId()).isNotNull();
        assertThat(response.taskId()).isEqualTo(String.join("::", REDEPLOY_V2.name(), agreementId));
        assertThat(response.taskInput().action()).isEqualTo(REDEPLOY_V2);
        assertThat(response.taskInput().taskInput().artifactType()).isEqualTo(TRADING_PARTNER_AGREEMENT_V2);
        assertThat(response.taskInput().taskInput().semanticVersion()).isEqualTo("2.0");
        assertThat(response.taskInput().taskParameters().btList()).hasSize(3).containsAll(btList);
    }

    private static String createAgreement(RequestContext requestContext) {
        //         ---------- Transaction option ----------         //
        TransactionOption tx = new TransactionOption();
        tx.setOption("Copy");
        tx.setTransactionIds(Collections.emptyList());

        //         ---------- Company data ----------         //
        CompanyData company = new CompanyData();
        company.setId("5da0cd82220649dd988fc44f44670239");
        company.setRole("INITIATOR");
        company.setSystemInstance(new IdWrapper("4b3a184463314dc9bb3af8a4ae08fad0"));
        company.setTypeSystem(new IdWrapper("SAP_IDoc"));
        company.setTypeSystemVersion("1809_FPS02");
        company.setIdAsSender(new IdWrapper("b8d41d80e7124d70a60c92b0aa2c107d"));
        company.setIdAsReceiver(new IdWrapper("ab9091c750984add80b381321fc62f4e"));
        company.setContactPerson(new IdWrapper(""));
        company.setSelectedProfileType("SUBSIDIARY");
        company.setParentId("myCompany");

        //         ---------- Trading-partner data ----------         //
        AliasWrapper aliasForSysInst = new AliasWrapper();
        AliasWrapper.AliasProperties aliasProps = new AliasWrapper.AliasProperties();
        aliasProps.setAlias("Cloud Cloud Dev");
        aliasForSysInst.setProperties(aliasProps);

        TradingPartnerData tpData = new TradingPartnerData();
        tpData.setRole("REACTOR");
        tpData.setAliasForSystemInstance(aliasForSysInst);
        tpData.setTypeSystem(new IdWrapper("ASC_X12"));
        tpData.setTypeSystemVersion("004010");

        //         ---------- Trading-partner details ----------         //
        TradingPartnerDetails tpDetails = new TradingPartnerDetails();
        tpDetails.setIdForTradingPartner(new IdWrapper("907e1fd04cc84615befcb181418edfb4"));
        tpDetails.setIdForSystemInstance(new IdWrapper("5a9c5c9f000543a584fe50bf15790678"));
        tpDetails.setIdForSenderIdentifier(new IdWrapper("2fb0994a42744361a066245ddb885e5d"));
        tpDetails.setIdForReceiverIdentifier(new IdWrapper("2e5dc475259f4a2b91b777d5261ba2bd"));
        tpDetails.setIdForContactPerson(new IdWrapper(""));

        //         ---------- Root object ----------         //
        AgreementCreationRequest agreementCreationRequest = new AgreementCreationRequest();
        agreementCreationRequest.setName("Hayk Test");
        agreementCreationRequest.setDescription("");
        agreementCreationRequest.setVersion("1.0");
        agreementCreationRequest.setOwnerId("myCompany");
        agreementCreationRequest.setShared(false);
        agreementCreationRequest.setTransactionOption(tx);
        agreementCreationRequest.setCompanyData(company);
        agreementCreationRequest.setTradingPartnerData(tpData);
        agreementCreationRequest.setTradingPartnerDetails(tpDetails);
        agreementCreationRequest.setParentId("a351e33a94b64cd8b273d9236fee4b1f");

        TpmObjectMetadata createdAgreement = agreementClient.createAgreement(requestContext, agreementCreationRequest);
        assertThat(createdAgreement).isNotNull();

        String agreementId = createdAgreement.getObjectId();
        assertThat(agreementId).isNotNull();

        return agreementId;
    }

    private static void deleteAgreement(RequestContext requestContext, String agreementId) {
        agreementClient.deleteAgreement(requestContext, agreementId);
    }
}
