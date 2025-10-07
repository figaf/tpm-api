package com.figaf.integration.tpm.data_provider;

import com.figaf.integration.common.data_provider.AgentTestData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomHostAgentTestData extends AgentTestData {

    private String integrationSuiteHost;

    public CustomHostAgentTestData(AgentTestData originalTestData, String integrationSuiteHost) {
        super(
            originalTestData.getTitle(),
            originalTestData.getPlatform(),
            originalTestData.getCloudPlatformType(),
            originalTestData.getLoginPageUrl(),
            originalTestData.getSsoUrl(),
            originalTestData.getWebApiAccessMode(),
            originalTestData.getSamlUrl(),
            originalTestData.getIdpName(),
            originalTestData.getIdpApiClientId(),
            originalTestData.getIdpApiClientSecret(),
            originalTestData.getClientId(),
            originalTestData.getClientSecret(),
            originalTestData.getTokenUrl(),
            originalTestData.getAuthenticationType(),
            originalTestData.getCertificatePath(),
            originalTestData.getCertificatePassword(),
            originalTestData.getPublicApiUrl(),
            originalTestData.getHost(),
            originalTestData.getPort(),
            originalTestData.getProtocol(),
            originalTestData.getUsername(),
            originalTestData.getPassword(),
            originalTestData.isIntegrationSuite()
        );
        this.integrationSuiteHost = integrationSuiteHost;
    }
}
