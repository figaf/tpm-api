package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.OrphanedInterchangesClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.OrphanedInterchange;
import com.figaf.integration.tpm.entity.OrphanedInterchangeRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Date;
import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class OrphanedInterchangesClientTest {

    private static OrphanedInterchangesClient orphanedInterchangesClient;

    @BeforeAll
    static void setUp() {
        orphanedInterchangesClient = new OrphanedInterchangesClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_searchOrphanedInterchanges(CustomHostAgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        Date leftBoundDate = DateUtils.addDays(new Date(), -7);
        Date rightBoundDate = new Date();

        OrphanedInterchangeRequest orphanedInterchangeRequest = new OrphanedInterchangeRequest(leftBoundDate);
        orphanedInterchangeRequest.setRightBoundDate(rightBoundDate);

        List<OrphanedInterchange> orphanedInterchanges = orphanedInterchangesClient.searchOrphanedInterchanges(requestContext, orphanedInterchangeRequest);
        assertThat(orphanedInterchanges).isNotEmpty();
    }

}