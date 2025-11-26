package com.figaf.integration.tpm.client.integration;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.MigClient;
import com.figaf.integration.tpm.data_provider.AgentTestDataProvider;
import com.figaf.integration.tpm.data_provider.CustomHostAgentTestData;
import com.figaf.integration.tpm.entity.integrationadvisory.IntegrationAdvisoryObject;
import com.figaf.integration.tpm.entity.integrationadvisory.MigVersion;
import com.figaf.integration.tpm.entity.mig.DraftCreationResponse;
import com.figaf.integration.tpm.exception.TpmException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.figaf.integration.tpm.utils.Constants.PARAMETERIZED_TEST_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class MigClientTest {

    private static Map<String, String> properties;
    private static final String MIG_OBJECT_FOR_DRAFT_CREATION = "mig-object-for-draft-creation";
    private static final String METADATA_NOT_NULL_MSG = "Actual MigResponse metadata not to be null.";
    private static final String EXPECTED_NOT_NULL_RAW_MSG = "Actual MigResponseRawResponse not to be null.";
    private static final String EXPECTED_MIG_FOR_DRAFT_VERSION_CREATION_NOT_PRESENT = "Mig with name %s should exist with status active.";
    private static final String EXPECTED_DRAFT_VERSION_AFTER_CREATION_NOT_PRESENT = "Mig with name %s should exist with status draft.";
    private static final String OBJECT_NAME_CANNOT_BE_EMPTY = "Mig object name for draft creation cannot be empty";
    private static MigClient migClient;

    @BeforeAll
    static void setUp() throws TpmException {
        migClient = new MigClient(new HttpClientsFactory());
        Yaml yaml = new Yaml();
        try (InputStream inputStream = MigClientTest.class.getClassLoader().getResourceAsStream("application-test.yml")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: application-test.yml");
            }
            properties = yaml.load(inputStream);
        } catch (Exception e) {
            throw new TpmException(e.getMessage(), e);
        }
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getAllLatestMetadata(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_getAllLatestMetadata: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());

        List<IntegrationAdvisoryObject> messageImplementationGuides = migClient.getAllLatestMetadata(requestContext);

        assertThat(messageImplementationGuides).as(METADATA_NOT_NULL_MSG).isNotNull();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getRawById(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_getRawById: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());

        List<IntegrationAdvisoryObject> migs = migClient.getAllLatestMetadata(requestContext);

        assertFalse(CollectionUtils.isEmpty(migs), METADATA_NOT_NULL_MSG);

        //its too heavy test to trigger getRawById for all migs
        IntegrationAdvisoryObject migFirstMetadata = migs.get(0);
        String migRawResponse = migClient.getRawById(requestContext, migFirstMetadata.getVersionId());
        assertThat(migRawResponse).as(EXPECTED_NOT_NULL_RAW_MSG).isNotEmpty();
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getMigVersions(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_getMigVersions: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());

        List<IntegrationAdvisoryObject> migs = migClient.getAllLatestMetadata(requestContext);
        IntegrationAdvisoryObject migFirstMetadata = migs.get(0);

        List<MigVersion> migVersions = migClient.getMigVersions(requestContext, migFirstMetadata.getObjectId());
        assertThat(migVersions).isNotEmpty();
        migVersions.forEach(migVersion -> assertThat(migVersion).hasNoNullFieldsOrProperties());
    }

    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_getMigVersionInfoById(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#getMigVersionInfoById: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());

        List<IntegrationAdvisoryObject> migs = migClient.getAllLatestMetadata(requestContext);

        assertFalse(CollectionUtils.isEmpty(migs), METADATA_NOT_NULL_MSG);

        IntegrationAdvisoryObject migFirstMetadata = migs.get(0);
        String migRawResponse = migClient.getMigVersionInfoById(requestContext, migFirstMetadata.getVersionId());
        assertThat(migRawResponse).as(EXPECTED_NOT_NULL_RAW_MSG).isNotEmpty();
    }


    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME)
    @ArgumentsSource(AgentTestDataProvider.class)
    void test_createDraftWithAllSegmentsAndFieldsSelected(CustomHostAgentTestData customHostAgentTestData) {
        log.debug("#test_createDraftWithAllSegmentsAndFieldsSelected: customHostAgentTestData={}", customHostAgentTestData);
        RequestContext requestContext = customHostAgentTestData.createRequestContext(customHostAgentTestData.getTitle());
        String migObjectNameForDraftCreation = properties.get(MIG_OBJECT_FOR_DRAFT_CREATION);

        assertTrue(Optional.ofNullable(properties).isPresent() && StringUtils.isNotBlank(migObjectNameForDraftCreation), OBJECT_NAME_CANNOT_BE_EMPTY);

        List<IntegrationAdvisoryObject> migs = migClient.getAllLatestMetadata(requestContext);
        Optional<IntegrationAdvisoryObject> tpmObjectMetadataForDraftCreation = migs
            .stream()
            .filter(mig -> mig.getDisplayedName().equals(migObjectNameForDraftCreation) && mig.getStatus().equals("Active"))
            .findAny();
        assertTrue(tpmObjectMetadataForDraftCreation.isPresent(), String.format(EXPECTED_MIG_FOR_DRAFT_VERSION_CREATION_NOT_PRESENT, migObjectNameForDraftCreation));

        DraftCreationResponse draftCreationResponse = migClient.createDraftWithAllSegmentsAndFieldsSelected(
            requestContext,
            tpmObjectMetadataForDraftCreation.get().getObjectId(),
            tpmObjectMetadataForDraftCreation.get().getVersionId()
        );

        assertTrue(Optional.ofNullable(draftCreationResponse).isPresent(), String.format(EXPECTED_DRAFT_VERSION_AFTER_CREATION_NOT_PRESENT, migObjectNameForDraftCreation));
        migClient.deleteDraftMig(
            requestContext,
            draftCreationResponse.getMigguid(),
            draftCreationResponse.getId()
        );
    }
}
