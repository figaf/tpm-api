package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.CommunicationPartnerPartnerClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.CreateBusinessEntityRequest;
import com.figaf.integration.tpm.entity.TpmBusinessEntity;
import com.figaf.integration.tpm.entity.trading.AggregatedTpmObject;
import com.figaf.integration.tpm.entity.trading.Channel;
import com.figaf.integration.tpm.entity.trading.CreateCommunicationRequest;
import com.figaf.integration.tpm.entity.trading.CreateSignatureVerificationConfigurationRequest;
import com.figaf.integration.tpm.entity.trading.CreateSystemRequest;
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
import java.util.UUID;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class CommunicationPartnerClientTest {

    private static CommunicationPartnerPartnerClient communicationPartnerPartnerClient;

    @BeforeAll
    static void setUp() {
        communicationPartnerPartnerClient = new CommunicationPartnerPartnerClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAll(AgentTestData agentTestData) {
        log.debug("#test_getAll: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = communicationPartnerPartnerClient.getAllMetadata(requestContext);

        assertThat(tradingPartners).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getById(AgentTestData agentTestData) {
        log.debug("#test_getById: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = communicationPartnerPartnerClient.getAllMetadata(requestContext);

        tradingPartners.forEach(tradingPartner -> {
            TpmObjectDetails tpmObjectDetails = communicationPartnerPartnerClient.getById(requestContext, tradingPartner.getObjectId());
            assertThat(tpmObjectDetails).isNotNull();
        });
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getRawById(AgentTestData agentTestData) {
        log.debug("#test_getById: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = communicationPartnerPartnerClient.getAllMetadata(requestContext);

        tradingPartners.forEach(tradingPartner -> {
            String tradingPartnerRawResponse = communicationPartnerPartnerClient.getRawById(requestContext, tradingPartner.getObjectId());
            assertThat(tradingPartnerRawResponse).isNotEmpty();
        });
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAggregatedTradingPartner(AgentTestData agentTestData) {
        log.debug("#test_getAggregatedTradingPartner: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = communicationPartnerPartnerClient.getAllMetadata(requestContext);

        tradingPartners.forEach(tradingPartner -> {
            //skip test objects
            if (StringUtils.containsAnyIgnoreCase(tradingPartner.getDisplayedName(), "test", "ZZ")) {
                return;
            }
            AggregatedTpmObject aggregatedTradingPartner = communicationPartnerPartnerClient.getAggregatedPartnerProfile(requestContext, tradingPartner.getObjectId());
            assertThat(aggregatedTradingPartner).isNotNull();
            assertThat(aggregatedTradingPartner.getTpmObjectDetails()).isNotNull();
            assertThat(aggregatedTradingPartner.getSystems()).isNotEmpty();
            assertThat(aggregatedTradingPartner.getSystemIdToChannels()).isNotEmpty();
        });
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getPartnerProfileSystems(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = communicationPartnerPartnerClient.getAllMetadata(requestContext);

        List<System> allSystems = new ArrayList<>();
        tradingPartners.forEach(tradingPartner -> {
            allSystems.addAll(communicationPartnerPartnerClient.getPartnerProfileSystems(requestContext, tradingPartner.getObjectId()));
        });

        assertThat(allSystems).isNotEmpty();
        allSystems.forEach(system -> assertThat(system).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getPartnerProfileChannels(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmBusinessEntity> tradingPartners = communicationPartnerPartnerClient.getAllMetadata(requestContext);

        List<Channel> allPartnerProfileChannels = new ArrayList<>();
        tradingPartners.forEach(tradingPartner -> {
            List<System> partnerProfileSystems = communicationPartnerPartnerClient.getPartnerProfileSystems(requestContext, tradingPartner.getObjectId());
            for (System partnerProfileSystem : partnerProfileSystems) {
                allPartnerProfileChannels.addAll(communicationPartnerPartnerClient.getPartnerProfileChannels(requestContext, tradingPartner.getObjectId(), partnerProfileSystem.getId()));
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

        CreateBusinessEntityRequest request = new CreateBusinessEntityRequest("COMMUNICATION_PARTNER");
        request.setName("Arsenii 6 Communication Partner");
        request.setShortName("Ars6 Communication Partner");
        request.setWebURL("http://example.com");
        request.getProfile().setPartnerType("COMMUNICATION_PARTNER");
        request.getProfile().getAddress().setCityName("Glostrup");
        request.getProfile().getAddress().setCountryCode("DK");
        request.getProfile().getAddress().setHouseNumber("33333");
        request.getProfile().getAddress().setPoBox("123");
        request.getProfile().getAddress().setPoBoxPostalCode("345");
        request.getProfile().getAddress().setStreetName("My Street");
        request.getProfile().getAddress().setStreetPostalCode("567");

        TpmObjectDetails tradingPartner = communicationPartnerPartnerClient.createPartnerProfile(requestContext, request);

        assertThat(tradingPartner).isNotNull();
    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createAndDeleteCommunicationTradingPartner(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_createAndDeleteCommunicationTradingPartner: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        CreateBusinessEntityRequest request = new CreateBusinessEntityRequest("COMMUNICATION_PARTNER");
        request.setName("Arsenii 7 Communication Partner");
        request.setShortName("Ars7 Communication Partner");
        request.setWebURL("http://example.com");
        request.getProfile().setPartnerType("COMMUNICATION_PARTNER");
        request.getProfile().getAddress().setCityName("Glostrup");
        request.getProfile().getAddress().setCountryCode("DK");
        request.getProfile().getAddress().setHouseNumber("33333");
        request.getProfile().getAddress().setPoBox("123");
        request.getProfile().getAddress().setPoBoxPostalCode("345");
        request.getProfile().getAddress().setStreetName("My Street");
        request.getProfile().getAddress().setStreetPostalCode("567");

        TpmObjectDetails communicationTradingPartner = communicationPartnerPartnerClient.createPartnerProfile(requestContext, request);

        String communicationTradingPartnerId = communicationTradingPartner.getId();
        String wrongCommunicationTradingPartner = communicationTradingPartnerId + UUID.randomUUID();

        assertThat(communicationTradingPartner).isNotNull();

        // Shouldn't delete because the wrong id
        ClientIntegrationException exception = assertThrows(ClientIntegrationException.class, () -> communicationPartnerPartnerClient.deletePartnerProfile(requestContext, wrongCommunicationTradingPartner, request));
        assertTrue(exception.getMessage().contains("Could not delete artifact, because Cannot read resource with ID"));

        // Delete created communication trading partner
        assertDoesNotThrow(() -> communicationPartnerPartnerClient.deletePartnerProfile(requestContext, communicationTradingPartnerId, request));
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

        System system = communicationPartnerPartnerClient.createSystem(requestContext, "526a865dca40421185bcb33310648b42", request);
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
        communicationPartnerPartnerClient.createCommunication(requestContext, "526a865dca40421185bcb33310648b42", "c36a6aa957124c378df81a46df995cea", senderCommunicationRequest);

        CreateCommunicationRequest receiverCommunicationRequest = new CreateCommunicationRequest();
        receiverCommunicationRequest.setDirection("Receiver");
        receiverCommunicationRequest.setAdapterType("AS2");
        receiverCommunicationRequest.setName("AS2 Receiver 2");
        receiverCommunicationRequest.setAlias("AS2 Receiver 2");
        receiverCommunicationRequest.setDescription("This is AS2 Receiver 2 Receiver");
        receiverCommunicationRequest.getConfigurationProperties().getAllAttributes().put("address", new CreateCommunicationRequest.Attribute("", "http://example.com", true));
        communicationPartnerPartnerClient.createCommunication(requestContext, "526a865dca40421185bcb33310648b42", "c36a6aa957124c378df81a46df995cea", receiverCommunicationRequest);

    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createSignatureVerificationConfiguration(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        CreateSignatureVerificationConfigurationRequest request = new CreateSignatureVerificationConfigurationRequest();
        request.setAs2PartnerId("dummy_Arsenii701");
        request.setAlias("dummy_Arsenii701");

        communicationPartnerPartnerClient.createSignatureVerificationConfiguration(requestContext, "526a865dca40421185bcb33310648b42", request);
    }

}
