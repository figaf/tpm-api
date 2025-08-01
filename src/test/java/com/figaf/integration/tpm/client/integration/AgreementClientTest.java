package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.agreement.AgreementClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.agreement.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Collections;
import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class AgreementClientTest {

    private static final String METADATA_NOT_NULL_MSG = "Actual agreementsResponse metadata not to be null.";

    private static AgreementClient agreementClient;

    @BeforeAll
    static void setUp() {
        agreementClient = new AgreementClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllMetadata(AgentTestData agentTestData) {
        log.debug("#test_getAllMetadata: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmObjectMetadata> agreementsMetadata = agreementClient.getAllMetadata(requestContext);

        assertThat(agreementsMetadata).as(METADATA_NOT_NULL_MSG).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteAgreement(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());
//         ---------- Transaction option ----------
        TransactionOption tx = new TransactionOption();
        tx.setOption("Copy");
        tx.setTransactionIds(Collections.emptyList());

//         ---------- Company data ----------
        CompanyData company = new CompanyData();
        company.setId("5da0cd82220649dd988fc44f44670239");
        company.setRole("INITIATOR");
        company.setSystemInstance(new IdWrapper("4b3a184463314dc9bb3af8a4ae08fad0"));
        company.setTypeSystem(new IdWrapper("SAP_IDoc"));
        company.setTypeSystemVersion("1809_FPS02");
        company.setIdAsSender(new IdWrapper("b8d41d80e7124d70a60c92b0aa2c107d"));
        company.setIdAsReceiver(new IdWrapper("ab9091c750984add80b381321fc62f4e"));
        company.setContactPerson(new IdWrapper(""));               // empty Id
        company.setSelectedProfileType("SUBSIDIARY");
        company.setParentId("myCompany");

//         ---------- Trading-partner data ----------
        AliasWrapper aliasForSysInst = new AliasWrapper();
        AliasWrapper.AliasProperties aliasProps = new AliasWrapper.AliasProperties();
        aliasProps.setAlias("Cloud Cloud Dev");
        aliasForSysInst.setProperties(aliasProps);

        TradingPartnerData tpData = new TradingPartnerData();
        tpData.setRole("REACTOR");
        tpData.setAliasForSystemInstance(aliasForSysInst);
        tpData.setTypeSystem(new IdWrapper("ASC_X12"));
        tpData.setTypeSystemVersion("004010");

//         ---------- Trading-partner details ----------
        TradingPartnerDetails tpDetails = new TradingPartnerDetails();
        tpDetails.setIdForTradingPartner(new IdWrapper("907e1fd04cc84615befcb181418edfb4"));
        tpDetails.setIdForSystemInstance(new IdWrapper("5a9c5c9f000543a584fe50bf15790678"));
        tpDetails.setIdForSenderIdentifier(new IdWrapper("2fb0994a42744361a066245ddb885e5d"));
        tpDetails.setIdForReceiverIdentifier(new IdWrapper("2e5dc475259f4a2b91b777d5261ba2bd"));
        tpDetails.setIdForContactPerson(new IdWrapper(""));        // empty Id

//         ---------- Root object ----------
        AgreementCreationRequest req = new AgreementCreationRequest();
        req.setName("ZZ Arsenii Test 7");
        req.setDescription("");
        req.setVersion("1.0");
        req.setOwnerId("myCompany");
        req.setShared(false);
        req.setTransactionOption(tx);
        req.setCompanyData(company);
        req.setTradingPartnerData(tpData);
        req.setTradingPartnerDetails(tpDetails);
        req.setParentId("81a36697276f4695860705388ad6b262");

        String agreementId = agreementClient.createAgreement(requestContext, req);
        assertThat(agreementId).isNotEmpty();

        agreementClient.deleteAgreement(requestContext, agreementId);

    }
}
