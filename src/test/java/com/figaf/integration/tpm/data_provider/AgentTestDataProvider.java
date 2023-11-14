package com.figaf.integration.tpm.data_provider;

import com.figaf.integration.common.data_provider.AbstractAgentTestDataProvider;
import com.figaf.integration.common.data_provider.AgentTestData;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author Kostas Charalambous
 */
public class AgentTestDataProvider extends AbstractAgentTestDataProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(Arguments.of(buildAgentTestDataForCfIntegrationSuite()));
    }

    public static CustomHostAgentTestData buildAgentTestDataForCfIntegrationSuite() {
        AgentTestData agentTestData = buildAgentTestData(Paths.get("src/test/resources/agent-test-data/cpi-cf-integration-suite"));
        String alternativeHost = System.getProperty("agentTestData.alternativeHost");
        return new CustomHostAgentTestData(agentTestData, alternativeHost);
    }
}
