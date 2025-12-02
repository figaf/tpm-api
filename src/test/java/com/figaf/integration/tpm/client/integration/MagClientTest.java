package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.MagClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.*;
import com.figaf.integration.tpm.entity.integrationadvisory.IntegrationAdvisoryObject;
import com.figaf.integration.tpm.entity.integrationadvisory.MagVersion;
import com.figaf.integration.tpm.entity.integrationadvisory.external_api.Mag;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class MagClientTest {

    private static MagClient magClient;

    @BeforeAll
    static void setUp() {
        magClient = new MagClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllLatestMetadata(AgentTestData agentTestData) {
        log.debug("#test_getAllLatestMetadata: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<IntegrationAdvisoryObject> mags = magClient.getAllLatestMetadata(requestContext);

        assertThat(mags).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getMagVersions(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_getMagVersions: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());

        List<IntegrationAdvisoryObject> mags = magClient.getAllLatestMetadata(requestContext);

        TpmObjectMetadata magFirstMetadata = mags.get(0);
        List<MagVersion> magVersions = magClient.getMagVersions(requestContext, magFirstMetadata.getObjectId());
        assertThat(magVersions).isNotEmpty();
        magVersions.forEach(magVersion -> assertThat(magVersion).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getRawById(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_getRawById: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());

        List<IntegrationAdvisoryObject> mags = magClient.getAllLatestMetadata(requestContext);

        IntegrationAdvisoryObject magFirstMetadata = mags.get(0);
        String payload = magClient.getRawById(requestContext, magFirstMetadata.getVersionId());
        assertThat(payload).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getMagVersionInfoById(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_getMagVersionInfoById: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());

        List<IntegrationAdvisoryObject> mags = magClient.getAllLatestMetadata(requestContext);

        IntegrationAdvisoryObject magFirstMetadata = mags.get(0);
        String payload = magClient.getMagVersionInfoById(requestContext, magFirstMetadata.getVersionId());
        assertThat(payload).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllMagsExternalApi(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_getAllMagsExternalApi: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());

        List<Mag> mags = magClient.getAllMagsExternalApi(requestContext);
        assertThat(mags).isNotEmpty();
        mags.forEach(mag -> assertThat(mag).hasNoNullFieldsOrProperties());
    }

}
