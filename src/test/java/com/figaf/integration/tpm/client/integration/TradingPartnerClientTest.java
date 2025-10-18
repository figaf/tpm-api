package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TradingPartnerClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
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
public class TradingPartnerClientTest {

    private static final String METADATA_NOT_NULL_MSG = "Actual tradingPartnerResponse metadata not to be null.";

    private static final String EXPECTED_NOT_NULL_VERBOSE_MSG = "Actual tradingPartnerVerboseResponse not to be null.";

    private static final String EXPECTED_NOT_NULL_RAW_MSG = "Actual tradingPartnerRawResponse not to be null.";

    private static TradingPartnerClient tradingPartnerClient;

    @BeforeAll
    static void setUp() {
        tradingPartnerClient = new TradingPartnerClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAll(AgentTestData agentTestData) {
        log.debug("#test_getAll: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        assertThat(tradingPartners).as(METADATA_NOT_NULL_MSG).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getById(AgentTestData agentTestData) {
        log.debug("#test_getById: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        tradingPartners.forEach(tradingPartner -> {
            TpmObjectDetails tpmObjectDetails = tradingPartnerClient.getById(requestContext, tradingPartner.getObjectId());
            assertThat(tpmObjectDetails).as(EXPECTED_NOT_NULL_VERBOSE_MSG).isNotNull();
        });
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getRawById(AgentTestData agentTestData) {
        log.debug("#test_getById: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        tradingPartners.forEach(tradingPartner -> {
            String tradingPartnerRawResponse = tradingPartnerClient.getRawById(requestContext, tradingPartner.getObjectId());
            assertThat(tradingPartnerRawResponse).as(EXPECTED_NOT_NULL_RAW_MSG).isNotEmpty();
        });
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAggregatedTradingPartner(AgentTestData agentTestData) {
        log.debug("#test_getAggregatedTradingPartner: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        tradingPartners.forEach(tradingPartner -> {
            //skip test objects
            if (StringUtils.containsAnyIgnoreCase(tradingPartner.getDisplayedName(), "test", "ZZ")) {
                return;
            }
            AggregatedTpmObject aggregatedTradingPartner = tradingPartnerClient.getAggregatedTradingPartner(requestContext, tradingPartner.getObjectId());
            assertThat(aggregatedTradingPartner).isNotNull();
            assertThat(aggregatedTradingPartner.getTpmObjectDetails()).isNotNull();
            assertThat(aggregatedTradingPartner.getSystems()).isNotEmpty();
            assertThat(aggregatedTradingPartner.getIdentifiers()).isNotEmpty();
            assertThat(aggregatedTradingPartner.getSystemIdToChannels()).isNotEmpty();
        });
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getPartnerProfileSystems(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        List<System> allSystems = new ArrayList<>();
        tradingPartners.forEach(tradingPartner -> {
            allSystems.addAll(tradingPartnerClient.getPartnerProfileSystems(requestContext, tradingPartner.getObjectId()));
        });

        assertThat(allSystems).isNotEmpty();
        allSystems.forEach(system -> assertThat(system).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getPartnerProfileIdentifiers(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        List<Identifier> allIdentifiers = new ArrayList<>();
        tradingPartners.forEach(tradingPartner -> {
            allIdentifiers.addAll(tradingPartnerClient.getPartnerProfileIdentifiers(requestContext, tradingPartner.getObjectId()));
        });

        assertThat(allIdentifiers).isNotEmpty();
        allIdentifiers.forEach(identifier -> assertThat(identifier).hasNoNullFieldsOrProperties());

    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getPartnerProfileChannels(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        List<Channel> allPartnerProfileChannels = new ArrayList<>();
        tradingPartners.forEach(tradingPartner -> {
            List<System> partnerProfileSystems = tradingPartnerClient.getPartnerProfileSystems(requestContext, tradingPartner.getObjectId());
            for (System partnerProfileSystem : partnerProfileSystems) {
                allPartnerProfileChannels.addAll(tradingPartnerClient.getPartnerProfileChannels(requestContext, tradingPartner.getObjectId(), partnerProfileSystem.getId()));
            }
        });

        assertThat(allPartnerProfileChannels).isNotEmpty();
        allPartnerProfileChannels.forEach(channel -> assertThat(channel).hasNoNullFieldsOrPropertiesExcept("securityConfigurationMode"));
    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createTradingPartner(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        CreateBusinessEntityRequest request = new CreateBusinessEntityRequest("TRADING_PARTNER");
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

        TpmObjectDetails tradingPartner = tradingPartnerClient.createTradingPartner(requestContext, request);

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

        System system = tradingPartnerClient.createSystem(requestContext, "82bf48ca067645d08c94b0e8cd7fbd19", request);
        assertThat(system).isNotNull();
        assertThat(system).hasNoNullFieldsOrProperties();
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
        tradingPartnerClient.createCommunication(requestContext, "82bf48ca067645d08c94b0e8cd7fbd19", "477c1eb61e9a498d82a3fc695c06f8f4", senderCommunicationRequest);

        CreateCommunicationRequest receiverCommunicationRequest = new CreateCommunicationRequest();
        receiverCommunicationRequest.setDirection("Receiver");
        receiverCommunicationRequest.setAdapterType("AS2");
        receiverCommunicationRequest.setName("AS2 Receiver 2");
        receiverCommunicationRequest.setAlias("AS2 Receiver 2");
        receiverCommunicationRequest.setDescription("This is AS2 Receiver 2 Receiver");
        receiverCommunicationRequest.getConfigurationProperties().getAllAttributes().put("address", new CreateCommunicationRequest.Attribute("", "http://example.com", true));
        tradingPartnerClient.createCommunication(requestContext, "82bf48ca067645d08c94b0e8cd7fbd19", "477c1eb61e9a498d82a3fc695c06f8f4", receiverCommunicationRequest);

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

        tradingPartnerClient.createIdentifier(requestContext, "82bf48ca067645d08c94b0e8cd7fbd19", request);
    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createSignatureVerificationConfiguration(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        CreateSignatureVerificationConfigurationRequest request = new CreateSignatureVerificationConfigurationRequest();
        request.setAs2PartnerId("dummy_Arsenii70");
        request.setAlias("dummy_Arsenii70");

        tradingPartnerClient.createSignatureVerificationConfiguration(requestContext, "82bf48ca067645d08c94b0e8cd7fbd19", request);
    }

}
