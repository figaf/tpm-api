package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.trading.TradingPartnerClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.trading.TradingPartner;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

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

        List<TpmObjectMetadata> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        assertThat(tradingPartners).as(METADATA_NOT_NULL_MSG).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getById(AgentTestData agentTestData) {
        log.debug("#test_getById: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmObjectMetadata> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        tradingPartners.forEach(tradingPartner -> {
            TradingPartner tradingPartnerVerboseDto = tradingPartnerClient.getById(tradingPartner.getId(), requestContext);
            assertThat(tradingPartnerVerboseDto).as(EXPECTED_NOT_NULL_VERBOSE_MSG).isNotNull();
        });
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getRawById(AgentTestData agentTestData) {
        log.debug("#test_getById: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<TpmObjectMetadata> tradingPartners = tradingPartnerClient.getAllMetadata(requestContext);

        tradingPartners.forEach(tradingPartner -> {
            String tradingPartnerRawResponse = tradingPartnerClient.getRawById(tradingPartner.getId(), requestContext);
            assertThat(tradingPartnerRawResponse).as(EXPECTED_NOT_NULL_RAW_MSG).isNotEmpty();
        });
    }
}
