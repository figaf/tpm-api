package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.CrossActionsClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.crossactions.ImportRequest;
import com.figaf.integration.tpm.entity.crossactions.ImportTaskResult;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CrossActionsClientTest {

    private static CrossActionsClient crossActionsClient;

    @BeforeAll
    static void setUp() {
        crossActionsClient = new CrossActionsClient(new HttpClientsFactory());
    }

    @Test
    void testCrossActions() throws InterruptedException, IOException {
        CustomHostAgentTestData customHostAgentTestData = AgentTestDataProvider.buildAgentTestDataForTestSystem();
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        requestContext.getConnectionProperties().setHost(customHostAgentTestData.getIntegrationSuiteHost());

        File file = new File("src/test/resources/import-archive.zip");

        String importRequestId = crossActionsClient.uploadAgreementsArchive(requestContext, FileUtils.readFileToByteArray(file));
        assertThat(importRequestId).isNotEmpty();

        ImportRequest importRequest = buildImportRequest(importRequestId);
        String taskId = crossActionsClient.executeAgreementsArchiveImport(requestContext, importRequest);
        assertThat(taskId).isNotEmpty();

        ImportTaskResult taskStatus;
        long start = System.currentTimeMillis();
        do {
            TimeUnit.SECONDS.sleep(3);
            taskStatus = crossActionsClient.getTaskStatus(requestContext, taskId);
        } while ("IN_PROCESS".equals(taskStatus.getOverallTaskStatus()) && System.currentTimeMillis() - start < 60_000);

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getOverallTaskStatus()).isEqualTo("COMPLETED");
    }

    private ImportRequest buildImportRequest(String importRequestId) {
        ImportRequest importRequest = new ImportRequest();
        importRequest.setAction("IMPORT");
        importRequest.setDescription("Import Test");

        ImportRequest.TaskInputItem taskInputItem = new ImportRequest.TaskInputItem();
        taskInputItem.setId(importRequestId);
        taskInputItem.setUniqueId("");
        taskInputItem.setArtifactType("RESOURCE_FILE");
        taskInputItem.setDisplayName("archive.zip");
        taskInputItem.setSemanticVersion("1.0");
        importRequest.setTaskInput(Collections.singletonList(taskInputItem));

        Map<String, String> taskParameters = new LinkedHashMap<>();
        taskParameters.put("IDENTIFIER", "{\"ImportMode\":\"Overwrite\"}");
        taskParameters.put("GROUP_IDENTIFIER", "{\"ImportMode\":\"Overwrite\",\"ChildrenImportMode\":\"SkipGroupItems\"}");
        taskParameters.put("COMMUNICATION_CHANNEL_TEMPLATE", "{\"ImportMode\":\"Overwrite\"}");
        taskParameters.put("PARAMETERS", "{\"ImportMode\":\"Overwrite\"}");
        taskParameters.put("ACTIVITY_PARAMETERS", "{\"ImportMode\":\"Overwrite\"}");
        taskParameters.put("COMPANY_PROFILE_CONFIG", "{\"ImportMode\":\"Overwrite\"}");
        taskParameters.put("PARTNER_PROFILE_CONFIG", "{\"ImportMode\":\"Overwrite\"}");
        taskParameters.put("MIG", "{\"ImportMode\":\"Overwrite\"}");
        taskParameters.put("MAG", "{\"ImportMode\":\"Overwrite\"}");
        importRequest.setTaskParameters(taskParameters);
        return importRequest;
    }

}