package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmInfrastructureClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.trading.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class TpmInfrastructureClientTest {

    private static TpmInfrastructureClient tpmInfrastructureClient;

    @BeforeAll
    static void setUp() {
        tpmInfrastructureClient = new TpmInfrastructureClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllSystemTypes(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<SystemType> systemTypes = tpmInfrastructureClient.getAllSystemTypes(requestContext);

        assertThat(systemTypes).isNotEmpty();
        systemTypes.forEach(systemType -> assertThat(systemType.getName()).isNotNull());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllTypeSystems(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TypeSystem> typeSystems = tpmInfrastructureClient.getAllTypeSystems(requestContext);

        assertThat(typeSystems).isNotEmpty();
        typeSystems.forEach(systemType -> assertThat(systemType).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getTypeSystemVersions(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TypeSystemVersion> typeSystemVersions = tpmInfrastructureClient.getTypeSystemVersions(requestContext, "ASC_X12");

        assertThat(typeSystemVersions).isNotEmpty();
        typeSystemVersions.forEach(typeSystemVersion -> assertThat(typeSystemVersion).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getProducts(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Product> products = tpmInfrastructureClient.getAllProducts(requestContext);

        assertThat(products).isNotEmpty();
        products.forEach(product -> assertThat(product).hasNoNullFieldsOrPropertiesExcept("parent"));
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSenderAdapters(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Adapter> senderAdapters = tpmInfrastructureClient.getSenderAdapters(requestContext);

        assertThat(senderAdapters).isNotEmpty();
        senderAdapters.forEach(senderAdapter -> assertThat(senderAdapter).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getReceiverAdapters(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Adapter> receiverAdapters = tpmInfrastructureClient.getReceiverAdapters(requestContext);

        assertThat(receiverAdapters).isNotEmpty();
        receiverAdapters.forEach(receiverAdapter -> assertThat(receiverAdapter).hasNoNullFieldsOrProperties());
    }

    @Disabled
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createSystemType(CustomHostAgentTestData customHostAgentTestData) {
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        List<Product> allProducts = tpmInfrastructureClient.getAllProducts(requestContext);
        String metadataId = allProducts.get(0).getMetadataId();

        CreateSystemTypeRequest request = new CreateSystemTypeRequest();
        request.setDeploymentType("Cloud");
        request.setDescription("This is Arsenii's System Type");
        request.setName("Arsenii System Type 2");
        request.setSapProduct(metadataId);

        tpmInfrastructureClient.createSystemType(requestContext, request);

    }

}
