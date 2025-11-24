package com.figaf.integration.tpm.data_provider;

import com.figaf.integration.common.data_provider.AgentTestData;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AgentDataExtension implements BeforeAllCallback {

    private AgentTestData agentTestData;

    @Override
    public void beforeAll(ExtensionContext context) {
        agentTestData = AgentTestDataProvider.buildAgentTestDataForCfIntegrationSuite();
    }

    public AgentTestData get() {
        return agentTestData;
    }
}