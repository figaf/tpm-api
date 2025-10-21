package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.BusinessDocumentsClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.ErrorDetails;
import com.figaf.integration.tpm.entity.Interchange;
import com.figaf.integration.tpm.entity.InterchangePayloadData;
import com.figaf.integration.tpm.entity.InterchangeRequest;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.*;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class BusinessDocumentsClientTest {

    private static BusinessDocumentsClient businessDocumentsClient;

    @BeforeAll
    static void setUp() {
        businessDocumentsClient = new BusinessDocumentsClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_searchInterchanges(CustomHostAgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        Date leftBoundDate = DateUtils.addDays(new Date(), -10);
        Date rightBoundDate = new Date();

        InterchangeRequest interchangeRequest = new InterchangeRequest(leftBoundDate);
        interchangeRequest.setRightBoundDate(rightBoundDate);
        interchangeRequest.setOverallStatuses(Arrays.asList("COMPLETED", "WAITING_FOR_ACKNOWLEDGEMENT", "ACKNOWLEDGEMENT_OVERDUE"));
        interchangeRequest.setProcessingStatuses(Collections.singletonList("COMPLETED"));
        interchangeRequest.setAgreedSenderIdentiferAtSenderSide("GIRAFT");
        interchangeRequest.setAgreedSenderIdentiferQualifierAtSenderSide("14");
        interchangeRequest.setAgreedReceiverIdentiferAtSenderSide("KUNDENET");
        interchangeRequest.setAgreedReceiverIdentiferQualifierAtSenderSide("ZZ");
        interchangeRequest.setAgreedSenderIdentiferAtReceiverSide("GIRAF_IDOC_FILE");
        interchangeRequest.setAgreedSenderIdentiferQualifierAtReceiverSide("GS1");
        interchangeRequest.setAgreedReceiverIdentiferAtReceiverSide("FIGAF_IDOC_FILE");
        interchangeRequest.setAgreedReceiverIdentiferQualifierAtReceiverSide("GS1");
        interchangeRequest.setSenderAdapterType("Process_Direct");
        interchangeRequest.setSenderDocumentStandard("ASC-X12");
        interchangeRequest.setSenderMessageType("850");
        interchangeRequest.setReceiverDocumentStandard("GS1_XML");
        interchangeRequest.setReceiverMessageType("ORDERS.ORDERS05");

        List<Interchange> interchanges = businessDocumentsClient.searchInterchanges(requestContext, interchangeRequest);
        // we can't really rely on presence of these interchanges. No guarantees that somebody triggers them.
        // ideally we need to send message from tests but even then, it may fail due to a problem with runtime
        //assertThat(interchanges).isNotEmpty();

        for (Interchange interchange : interchanges) {
            List<byte[]> payloads = new ArrayList<>();
            for (InterchangePayloadData interchangePayloadData : interchange.getInterchangePayloadDataList()) {
                payloads.add(businessDocumentsClient.downloadPayload(requestContext, interchangePayloadData.getPayloadId()));
            }
            assertThat(payloads).isNotEmpty();
            assertThat(interchange.getInterchangePayloadDataList().get(0).getEventType()).isEqualTo("BUSINESSDOCUMENT_CREATE_EVENT");
        }

    }

    @Test
    void test_getLastErrorDetailsByInterchangeId() {
        CustomHostAgentTestData agentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(agentTestData.getIntegrationSuiteHost());

        ErrorDetails errorDetails = businessDocumentsClient.getLastErrorDetailsByInterchangeId(requestContext, "4017ab06cafccefc2f0643aaead8e1d4");
        assertThat(errorDetails).isNotNull();
    }

}