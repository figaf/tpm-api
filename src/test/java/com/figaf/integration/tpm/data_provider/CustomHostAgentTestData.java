package com.figaf.integration.tpm.data_provider;

import com.figaf.integration.common.data_provider.AgentTestData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomHostAgentTestData extends AgentTestData {

    private String alternativeHost;

    public CustomHostAgentTestData(AgentTestData originalTestData, String alternativeHost) {
        super(originalTestData.getTitle(), originalTestData.getPlatform(), originalTestData.getCloudPlatformType(), originalTestData.getConnectionProperties());
        this.alternativeHost = alternativeHost;
    }
}
