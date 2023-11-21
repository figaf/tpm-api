package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.mig.MessageImplementationGuidelinesClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
public class MessageImplementationGuidelinesClientTest {

    private static final String METADATA_NOT_NULL_MSG = "Actual MigResponse metadata not to be null.";
    private static final String EXPECTED_NOT_NULL_RAW_MSG = "Actual MigResponseRawResponse not to be null.";
    private static MessageImplementationGuidelinesClient messageImplementationGuidelinesClient;

    @BeforeAll
    static void setUp() {
        messageImplementationGuidelinesClient = new MessageImplementationGuidelinesClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllLatestMetadata(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_getAllLatestMetadata: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        List<TpmObjectMetadata> messageImplementationGuides = messageImplementationGuidelinesClient.getAllLatestMetadata(requestContext);

        assertThat(messageImplementationGuides).as(METADATA_NOT_NULL_MSG).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getRawById(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_getRawById: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        List<TpmObjectMetadata> migs = messageImplementationGuidelinesClient.getAllLatestMetadata(requestContext);

        assertFalse(CollectionUtils.isEmpty(migs), METADATA_NOT_NULL_MSG);

        //its too heavy test to trigger getRawById for all migs
        TpmObjectMetadata migFirstMetadata = migs.get(0);
        String tradingPartnerRawResponse = messageImplementationGuidelinesClient.getRawById(migFirstMetadata.getVersionId(), requestContext);
        assertThat(tradingPartnerRawResponse).as(EXPECTED_NOT_NULL_RAW_MSG).isNotEmpty();

    }
}
