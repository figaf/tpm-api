package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.data_provider.AgentTestData;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.company.CompanyProfileClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.entity.Company;
import com.figaf.integration.tpm.entity.Subsidiary;
import com.figaf.integration.tpm.entity.trading.Channel;
import com.figaf.integration.tpm.entity.trading.Identifier;
import com.figaf.integration.tpm.entity.trading.System;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;
import java.util.List;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class CompanyProfileClientTest {

    private static final String METADATA_NOT_NULL_MSG = "Actual companyProfilesResponse metadata not to be null.";
    private static final String COMPANY_ID = "myCompany";

    private static CompanyProfileClient companyProfileClient;

    @BeforeAll
    static void setUp() {
        companyProfileClient = new CompanyProfileClient(new HttpClientsFactory());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllMetadata(AgentTestData agentTestData) {
        log.debug("#test_getAllMetadata: agentTestData={}", agentTestData);
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Company> companyProfiles = companyProfileClient.getAllMetadata(requestContext);

        assertThat(companyProfiles).as(METADATA_NOT_NULL_MSG).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSubsidiaries(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Subsidiary> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);

        assertThat(subsidiaries).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getCompanySystems(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<System> systems = companyProfileClient.getCompanySystems(requestContext, COMPANY_ID);

        assertThat(systems).isNotEmpty();
        systems.forEach(system -> assertThat(system).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSubsidiarySystems(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<System> allSystems = new ArrayList<>();
        List<Subsidiary> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);
        for (Subsidiary subsidiary : subsidiaries) {
            allSystems.addAll(companyProfileClient.getSubsidiarySystems(requestContext, COMPANY_ID, subsidiary.getObjectId()));
        }

        assertThat(allSystems).isNotEmpty();
        allSystems.forEach(system -> assertThat(system).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getCompanyIdentifiers(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Identifier> identifiers = companyProfileClient.getCompanyIdentifiers(requestContext, COMPANY_ID);

        assertThat(identifiers).isNotEmpty();
        identifiers.forEach(identifier -> assertThat(identifier).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSubsidiaryIdentifiers(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Identifier> allIdentifiers = new ArrayList<>();
        List<Subsidiary> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);
        for (Subsidiary subsidiary : subsidiaries) {
            allIdentifiers.addAll(companyProfileClient.getSubsidiaryIdentifiers(requestContext, COMPANY_ID, subsidiary.getObjectId()));
        }

        assertThat(allIdentifiers).isNotEmpty();
        allIdentifiers.forEach(identifier -> assertThat(identifier).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getCompanyChannels(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Channel> allChannels = new ArrayList<>();
        List<System> systems = companyProfileClient.getCompanySystems(requestContext, COMPANY_ID);
        for (System system : systems) {
            List<Channel> channels = companyProfileClient.getCompanyChannels(requestContext, COMPANY_ID, system.getId());
            allChannels.addAll(channels);
        }

        assertThat(allChannels).isNotEmpty();
        allChannels.forEach(channel -> assertThat(channel).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getSubsidiaryChannels(AgentTestData agentTestData) {
        RequestContext requestContext = agentTestData.createRequestContext(agentTestData.getTitle());

        List<Channel> allChannels = new ArrayList<>();
        List<Subsidiary> subsidiaries = companyProfileClient.getSubsidiaries(requestContext, COMPANY_ID);
        for (Subsidiary subsidiary : subsidiaries) {
            List<System> systems = companyProfileClient.getSubsidiarySystems(requestContext, COMPANY_ID, subsidiary.getObjectId());
            for (System system : systems) {
                List<Channel> channels = companyProfileClient.getSubsidiaryChannels(requestContext, COMPANY_ID, subsidiary.getObjectId(), system.getId());
                allChannels.addAll(channels);
            }
        }

        assertThat(allChannels).isNotEmpty();
        allChannels.forEach(channel -> assertThat(channel).hasNoNullFieldsOrProperties());
    }
    
}
