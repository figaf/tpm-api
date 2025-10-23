package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.CompanyProfileClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.CreateAs2InboundDecryptionConfigurationRequest;
import com.figaf.integration.tpm.entity.CreateBusinessEntityRequest;
import com.figaf.integration.tpm.entity.TpmBusinessEntity;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.TpmObjectDetails;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class CompanyProfileClientTest {

    private static final String METADATA_NOT_NULL_MSG = "Actual companyProfilesResponse metadata not to be null.";
    private static final String COMPANY_ID = "myCompany";

    private static CompanyProfileClient companyProfileClient;

    @BeforeAll
    static void setUp() {
        companyProfileClient = new CompanyProfileClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllMetadata(AgentTestData agentTestData) {
        log.debug("#test_getAllMetadata: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> companyProfiles = companyProfileClient.getAllMetadata(requestContext);

        assertThat(companyProfiles).as(METADATA_NOT_NULL_MSG).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getCompanyDetails(AgentTestData agentTestData) {
        log.debug("#test_getCompanyDetails: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        TpmObjectDetails tpmObjectDetails = companyProfileClient.getCompanyDetails(requestContext);

        assertThat(tpmObjectDetails).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSubsidiaryDetails(AgentTestData agentTestData) {
        log.debug("#test_getSubsidiaryDetails: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);
        for (TpmBusinessEntity subsidiary : subsidiaries) {
            TpmObjectDetails subsidiaryDetails = companyProfileClient.getSubsidiaryDetails(requestContext, COMPANY_ID, subsidiary.getObjectId());
            assertThat(subsidiaryDetails).isNotNull();
        }
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAggregatedCompany(AgentTestData agentTestData) {
        log.debug("#test_getAggregatedCompany: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        AggregatedTpmObject aggregatedCompany = companyProfileClient.getAggregatedCompany(requestContext);
        assertThat(aggregatedCompany).isNotNull();
        assertThat(aggregatedCompany.getTpmObjectDetails()).isNotNull();
        assertThat(aggregatedCompany.getSystems()).isNotEmpty();
        assertThat(aggregatedCompany.getIdentifiers()).isNotEmpty();
        assertThat(aggregatedCompany.getSystemIdToChannels()).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAggregatedSubsidiary(AgentTestData agentTestData) {
        log.debug("#test_getAggregatedSubsidiary: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);
        for (TpmBusinessEntity subsidiary : subsidiaries) {
            if (StringUtils.containsAnyIgnoreCase(subsidiary.getDisplayedName(), "test", "ZZ")) {
                continue;
            }
            AggregatedTpmObject aggregatedSubsidiary = companyProfileClient.getAggregatedSubsidiary(requestContext, COMPANY_ID, subsidiary.getObjectId());
            assertThat(aggregatedSubsidiary).isNotNull();
            assertThat(aggregatedSubsidiary.getTpmObjectDetails()).isNotNull();
            assertThat(aggregatedSubsidiary.getSystems()).isNotEmpty();
            assertThat(aggregatedSubsidiary.getIdentifiers()).isNotEmpty();
            assertThat(aggregatedSubsidiary.getSystemIdToChannels()).isNotEmpty();
        }
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSubsidiaries(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);

        assertThat(subsidiaries).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getCompanySystems(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<System> systems = companyProfileClient.getCompanySystems(requestContext, COMPANY_ID);

        assertThat(systems).isNotEmpty();
        systems.forEach(system -> assertThat(system).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSubsidiarySystems(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<System> allSystems = new ArrayList<>();
        List<TpmBusinessEntity> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);
        for (TpmBusinessEntity subsidiary : subsidiaries) {
            allSystems.addAll(companyProfileClient.getSubsidiarySystems(requestContext, COMPANY_ID, subsidiary.getObjectId()));
        }

        assertThat(allSystems).isNotEmpty();
        allSystems.forEach(system -> assertThat(system).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getCompanyIdentifiers(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Identifier> identifiers = companyProfileClient.getCompanyIdentifiers(requestContext, COMPANY_ID);

        assertThat(identifiers).isNotEmpty();
        identifiers.forEach(identifier -> assertThat(identifier).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSubsidiaryIdentifiers(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Identifier> allIdentifiers = new ArrayList<>();
        List<TpmBusinessEntity> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);
        for (TpmBusinessEntity subsidiary : subsidiaries) {
            allIdentifiers.addAll(companyProfileClient.getSubsidiaryIdentifiers(requestContext, COMPANY_ID, subsidiary.getObjectId()));
        }

        assertThat(allIdentifiers).isNotEmpty();
        allIdentifiers.forEach(identifier -> assertThat(identifier).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getCompanyChannels(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Channel> allChannels = new ArrayList<>();
        List<System> systems = companyProfileClient.getCompanySystems(requestContext, COMPANY_ID);
        for (System system : systems) {
            List<Channel> channels = companyProfileClient.getCompanyChannels(requestContext, COMPANY_ID, system.getId());
            allChannels.addAll(channels);
        }

        assertThat(allChannels).isNotEmpty();
        allChannels.forEach(channel -> assertThat(channel).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSubsidiaryChannels(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Channel> allChannels = new ArrayList<>();
        List<TpmBusinessEntity> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);
        for (TpmBusinessEntity subsidiary : subsidiaries) {
            List<System> systems = companyProfileClient.getSubsidiarySystems(requestContext, COMPANY_ID, subsidiary.getObjectId());
            for (System system : systems) {
                List<Channel> channels = companyProfileClient.getSubsidiaryChannels(requestContext, COMPANY_ID, subsidiary.getObjectId(), system.getId());
                allChannels.addAll(channels);
            }
        }

        assertThat(allChannels).isNotEmpty();
        allChannels.forEach(channel -> assertThat(channel).hasNoNullFieldsOrPropertiesExcept("securityConfigurationMode"));
    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createSubsidiary(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        CreateBusinessEntityRequest request = new CreateBusinessEntityRequest("SUBSIDIARY");
        request.setName("Arsenii 6");
        request.setShortName("Ars6");
        request.setWebURL("http://example.com");
        request.getProfile().getAddress().setCityName("Glostrup");
        request.getProfile().getAddress().setCountryCode("DK");
        request.getProfile().getAddress().setHouseNumber("33333");
        request.getProfile().getAddress().setPoBox("123");
        request.getProfile().getAddress().setPoBoxPostalCode("345");
        request.getProfile().getAddress().setStreetName("My Street");
        request.getProfile().getAddress().setStreetPostalCode("567");

        TpmObjectDetails tradingPartner = companyProfileClient.createSubsidiary(requestContext, "myCompany", request);

        assertThat(tradingPartner).as(METADATA_NOT_NULL_MSG).isNotNull();
    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createSystem(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        CreateSystemRequest request = new CreateSystemRequest();
        request.setName("Arsenii Test 7");
        request.setAlias("Arsenii 7 Alias");
        request.setSystemType("2cef5ae5c1324d5bb0d08643d87abd84");
        request.setPurpose("Dev");

        TypeSystemWithVersions typeSystem = new TypeSystemWithVersions();
        typeSystem.setId("GS1_XML");
        typeSystem.setName("GS1 XML");
        typeSystem.getVersions().add(new TypeSystemWithVersions.TypeSystemVersion("3.0", "3.0"));
        typeSystem.getVersions().add(new TypeSystemWithVersions.TypeSystemVersion("3.2", "3.2"));
        request.getTypeSystems().add(typeSystem);

        System system = companyProfileClient.createSubsidiarySystem(requestContext, "myCompany", "94bd5bd99e394728a8363de9c2b692eb", request);
        assertThat(system).isNotNull();
        assertThat(system).hasNoNullFieldsOrProperties();
    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createIdentifiers(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        CreateIdentifierRequest request = new CreateIdentifierRequest();
        request.setTypeSystemId("UNEDIFACT");
        request.setSchemeCode("ZZ");
        request.setSchemeName("Mutually defined");
        request.setIdentifierId("dummy_Arsenii8ShortName_3");
        request.setAlias("Arsenii Alias Test 100");
        request.setAlias("ZZ");

        companyProfileClient.createSubsidiaryIdentifier(requestContext, "myCompany", "94bd5bd99e394728a8363de9c2b692eb", request);
    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createCommunication(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        CreateCommunicationRequest senderCommunicationRequest = new CreateCommunicationRequest();
        senderCommunicationRequest.setDirection("Sender");
        senderCommunicationRequest.setAdapterType("SOAP 1.x");
        senderCommunicationRequest.setName("SOAP Sender 5");
        senderCommunicationRequest.setAlias("SOAP_SENDER_5");
        senderCommunicationRequest.setDescription("This is SOAP Sender");
        companyProfileClient.createSubsidiaryCommunication(requestContext, "myCompany", "94bd5bd99e394728a8363de9c2b692eb", "8ae91791cfe7487a9da65d8616baddab", senderCommunicationRequest);

        CreateCommunicationRequest receiverCommunicationRequest = new CreateCommunicationRequest();
        receiverCommunicationRequest.setDirection("Receiver");
        receiverCommunicationRequest.setAdapterType("AS2");
        receiverCommunicationRequest.setName("AS2 Receiver 2");
        receiverCommunicationRequest.setAlias("AS2 Receiver 2");
        receiverCommunicationRequest.setDescription("This is AS2 Receiver 2 Receiver");
        receiverCommunicationRequest.getConfigurationProperties().getAllAttributes().put("address", new CreateCommunicationRequest.Attribute("", "http://example.com", true));
        companyProfileClient.createSubsidiaryCommunication(requestContext, "myCompany", "94bd5bd99e394728a8363de9c2b692eb", "8ae91791cfe7487a9da65d8616baddab", receiverCommunicationRequest);
    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAs2InboundDecryptionConfiguration(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        CreateAs2InboundDecryptionConfigurationRequest request = new CreateAs2InboundDecryptionConfigurationRequest();
        request.setUserAccount("dummy_Arsenii70");
        request.setAlias("dummy_Arsenii70");

        companyProfileClient.createSubsidiaryAs2InboundDecryptionConfiguration(requestContext, "myCompany","94bd5bd99e394728a8363de9c2b692eb", request);
    }

}
