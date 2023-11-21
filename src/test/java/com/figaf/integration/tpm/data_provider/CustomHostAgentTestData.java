package com.figaf.integration.tpm.data_provider;

import com.figaf.integration.common.data_provider.AgentTestData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomHostAgentTestData extends AgentTestData {

    private String integrationSuiteHost;

    public CustomHostAgentTestData(AgentTestData originalTestData, String integrationSuiteHost) {
        super(originalTestData.getTitle(), originalTestData.getPlatform(), originalTestData.getCloudPlatformType(), originalTestData.getConnectionProperties());
        this.integrationSuiteHost = integrationSuiteHost;
    }
}
