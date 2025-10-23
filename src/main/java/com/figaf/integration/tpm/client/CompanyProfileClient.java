package com.figaf.integration.tpm.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.*;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.TpmObjectDetails;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;

@Slf4j
public class CompanyProfileClient extends BusinessEntityAbstractClient {

    private static final String COMPANY_PROFILE_RESOURCE = "/itspaces/tpm/company";
    private static final String COMPANY_SUBSIDIARIES_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries";
    private static final String SUBSIDIARY_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s";
    private static final String COMPANY_SYSTEMS_RESOURCE = "/itspaces/tpm/company/%s/systems";
    private static final String SUBSIDIARY_SYSTEMS_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/systems";
    private static final String COMPANY_IDENTIFIERS_RESOURCE = "/itspaces/tpm/company/%s/identifiers";
    private static final String SUBSIDIARY_IDENTIFIERS_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/identifiers";
    private static final String COMPANY_CHANNELS_RESOURCE = "/itspaces/tpm/company/%s/systems/%s/channels";
    private static final String SUBSIDIARY_CHANNELS_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/systems/%s/channels";
    private static final String COMPANY_PROFILE_CONFIGURATION_RESOURCE = "/itspaces/tpm/company/%s::profileConfiguration";
    private static final String COMPANY_CONFIG_DECRYPT_RESOURCE = "/itspaces/tpm/company/%s/config.decrypt";
    private static final String SUBSIDIARY_PROFILE_CONFIGURATION_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s::profileConfiguration";
    private static final String SUBSIDIARY_CONFIG_DECRYPT_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/config.decrypt";

    public CompanyProfileClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TpmBusinessEntity> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            COMPANY_PROFILE_RESOURCE,
            response -> {
                JSONArray companiesJsonArray = new JSONArray(response);
                List<TpmBusinessEntity> companies = new ArrayList<>();
                for (int i = 0; i < companiesJsonArray.length(); i++) {
                    JSONObject companyJsonObject = companiesJsonArray.getJSONObject(i);
                    TpmBusinessEntity tpmBusinessEntity = buildTpmBusinessEntity(companyJsonObject, TpmObjectType.CLOUD_COMPANY_PROFILE);
                    companies.add(tpmBusinessEntity);
                }

                for (TpmObjectMetadata company : companies) {
                    List<TpmBusinessEntity> subsidiaries = getSubsidiaries(requestContext, company.getObjectId());
                    List<TpmObjectReference> tpmObjectReferences = new ArrayList<>();
                    for (TpmBusinessEntity subsidiary : subsidiaries) {
                        TpmObjectReference tpmObjectReference = new TpmObjectReference();
                        tpmObjectReference.setObjectId(subsidiary.getObjectId());
                        tpmObjectReference.setTpmObjectType(TpmObjectType.CLOUD_SUBSIDIARY);
                        tpmObjectReferences.add(tpmObjectReference);
                    }
                    company.setTpmObjectReferences(tpmObjectReferences);
                }
                return companies;
            }
        );
    }

    public List<TpmBusinessEntity> getSubsidiaries(RequestContext requestContext, String companyId) {
        log.debug("#getSubsidiaries: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext,
            format(COMPANY_SUBSIDIARIES_RESOURCE, companyId),
            response -> {
                JSONArray subsidiariesJsonArray = new JSONArray(response);
                List<TpmBusinessEntity> subsidiaries = new ArrayList<>();
                for (int i = 0; i < subsidiariesJsonArray.length(); i++) {
                    JSONObject subsidiaryJsonObject = subsidiariesJsonArray.getJSONObject(i);
                    TpmBusinessEntity subsidiary = buildTpmBusinessEntity(subsidiaryJsonObject, TpmObjectType.CLOUD_SUBSIDIARY);
                    subsidiary.setParentId(companyId);

                    subsidiaries.add(subsidiary);
                }
                return subsidiaries;
            }
        );
    }

    public TpmObjectDetails getCompanyDetails(RequestContext requestContext) {
        log.debug("#getCompanyDetails: requestContext={}", requestContext);

        return executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext,
            format(COMPANY_PROFILE_RESOURCE),
            this::buildTpmObjectDetails
        );
    }

    public TpmObjectDetails getSubsidiaryDetails(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getSubsidiaryDetails: requestContext = {}", requestContext);

        return executeGet(
            requestContext,
            format(SUBSIDIARY_RESOURCE, parentCompanyId, subsidiaryId),
                responseEntityBody -> {
                    TpmObjectDetails tpmObjectDetails = buildTpmObjectDetails(responseEntityBody);
                    tpmObjectDetails.setParentCompanyId(parentCompanyId);
                    return tpmObjectDetails;
                }
        );
    }

    public AggregatedTpmObject getAggregatedCompany(RequestContext requestContext) {
        log.debug("#getAggregatedCompany: requestContext = {}", requestContext);

        TpmObjectDetails tpmObjectDetails = getCompanyDetails(requestContext);
        if (tpmObjectDetails == null) {
            return null;
        }

        List<System> systems = getCompanySystems(requestContext, tpmObjectDetails.getId());
        List<Identifier> identifiers = getCompanyIdentifiers(requestContext, tpmObjectDetails.getId());
        Map<String, List<Channel>> systemIdToChannels = new LinkedHashMap<>();
        for (System system : systems) {
            List<Channel> channels = getCompanyChannels(requestContext, tpmObjectDetails.getId(), system.getId());
            systemIdToChannels.put(system.getId(), channels);
        }

        ProfileConfiguration profileConfiguration = resolveCompanyProfileConfiguration(requestContext, tpmObjectDetails.getId());

        return new AggregatedTpmObject(tpmObjectDetails, systems, identifiers, systemIdToChannels, profileConfiguration);
    }

    public AggregatedTpmObject getAggregatedSubsidiary(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getAggregatedSubsidiary: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}", requestContext, parentCompanyId, subsidiaryId);

        TpmObjectDetails tpmObjectDetails = getSubsidiaryDetails(requestContext, parentCompanyId, subsidiaryId);
        if (tpmObjectDetails == null) {
            return null;
        }

        List<System> systems = getSubsidiarySystems(requestContext, parentCompanyId, subsidiaryId);
        List<Identifier> identifiers = getSubsidiaryIdentifiers(requestContext, parentCompanyId, subsidiaryId);
        Map<String, List<Channel>> systemIdToChannels = new LinkedHashMap<>();
        for (System system : systems) {
            List<Channel> channels = getSubsidiaryChannels(requestContext, parentCompanyId, subsidiaryId, system.getId());
            systemIdToChannels.put(system.getId(), channels);
        }

        ProfileConfiguration profileConfiguration = resolveSubsidiaryProfileConfiguration(requestContext, parentCompanyId, subsidiaryId);

        return new AggregatedTpmObject(tpmObjectDetails, systems, identifiers, systemIdToChannels, profileConfiguration);
    }

    public List<System> getCompanySystems(RequestContext requestContext, String companyId) {
        log.debug("#getCompanySystems: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(COMPANY_SYSTEMS_RESOURCE, companyId),
            this::parseSystemsList
        );
    }

    public List<System> getSubsidiarySystems(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getSubsidiarySystems: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}", requestContext, parentCompanyId, subsidiaryId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(SUBSIDIARY_SYSTEMS_RESOURCE, parentCompanyId, subsidiaryId),
            this::parseSystemsList
        );
    }

    public List<Identifier> getCompanyIdentifiers(RequestContext requestContext, String companyId) {
        log.debug("#getCompanyIdentifiers: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(COMPANY_IDENTIFIERS_RESOURCE, companyId),
            this::parseIdentifiersList
        );
    }

    public List<Identifier> getSubsidiaryIdentifiers(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getSubsidiaryIdentifiers: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}", requestContext, parentCompanyId, subsidiaryId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(SUBSIDIARY_IDENTIFIERS_RESOURCE, parentCompanyId, subsidiaryId),
            this::parseIdentifiersList
        );
    }

    public List<Channel> getCompanyChannels(RequestContext requestContext, String companyId, String systemId) {
        log.debug("#getCompanyChannels: requestContext = {}, companyId = {}, systemId = {}", requestContext, companyId, systemId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(COMPANY_CHANNELS_RESOURCE, companyId, systemId),
            this::parseChannelsList
        );
    }

    public List<Channel> getSubsidiaryChannels(RequestContext requestContext, String parentCompanyId, String subsidiaryId, String systemId) {
        log.debug("#getSubsidiaryChannels: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}, systemId = {}", requestContext, parentCompanyId, subsidiaryId, systemId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(SUBSIDIARY_CHANNELS_RESOURCE, parentCompanyId, subsidiaryId, systemId),
            this::parseChannelsList
        );
    }

    public TpmObjectDetails createSubsidiary(RequestContext requestContext, String parentCompanyId, CreateBusinessEntityRequest createBusinessEntityRequest) {
        log.debug("#createSubsidiary: requestContext = {}, createBusinessEntityRequest = {}", requestContext, createBusinessEntityRequest);

        return executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            format(COMPANY_SUBSIDIARIES_RESOURCE,parentCompanyId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateBusinessEntityRequest> requestEntity = new HttpEntity<>(createBusinessEntityRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create trading partner. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }

                TpmObjectDetails tpmObjectDetails = buildTpmObjectDetails(responseEntity.getBody());
                tpmObjectDetails.setParentCompanyId(parentCompanyId);
                return tpmObjectDetails;
            }
        );
    }

    public System createCompanySystem(RequestContext requestContext, String companyId, CreateSystemRequest createSystemRequest) {
        log.debug("#createCompanySystem: requestContext = {}, companyId = {}, createSystemRequest = {}", requestContext, companyId, createSystemRequest);
        return createSystem(
            requestContext,
            format(COMPANY_SYSTEMS_RESOURCE, companyId),
            createSystemRequest
        );
    }

    public System createSubsidiarySystem(RequestContext requestContext, String parentCompanyId, String subsidiaryId, CreateSystemRequest createSystemRequest) {
        log.debug("#createSubsidiarySystem: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}, createSystemRequest = {}", requestContext, parentCompanyId, subsidiaryId, createSystemRequest);
        return createSystem(
            requestContext,
            format(SUBSIDIARY_SYSTEMS_RESOURCE, parentCompanyId, subsidiaryId),
            createSystemRequest
        );
    }

    private System createSystem(RequestContext requestContext, String path, CreateSystemRequest createSystemRequest) {
        return executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            path,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateSystemRequest> requestEntity = new HttpEntity<>(createSystemRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create system. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                try {
                    return jsonMapper.readValue(responseEntity.getBody(), System.class);
                } catch (JsonProcessingException ex) {
                    throw new ClientIntegrationException("Can't parse System creation response: ", ex);
                }
            }
        );
    }

    public void createCompanyCommunication(RequestContext requestContext, String companyId, String systemId, CreateCommunicationRequest createCommunicationRequest) {
        log.debug("#createCompanyCommunication: requestContext = {}, companyId = {}, createCommunicationRequest = {}", requestContext, companyId, createCommunicationRequest);
        createCommunication(requestContext, format(COMPANY_CHANNELS_RESOURCE, companyId, systemId), createCommunicationRequest);
    }

    public void createSubsidiaryCommunication(RequestContext requestContext, String parentCompanyId, String subsidiaryId, String systemId, CreateCommunicationRequest createCommunicationRequest) {
        log.debug("#createSubsidiaryCommunication: requestContext = {}, parentCompanyId = {}, systemId = {}, createCommunicationRequest = {}", requestContext, parentCompanyId, systemId, createCommunicationRequest);
        createCommunication(requestContext, format(SUBSIDIARY_CHANNELS_RESOURCE, parentCompanyId, subsidiaryId, systemId), createCommunicationRequest);
    }

    private void createCommunication(RequestContext requestContext, String path, CreateCommunicationRequest createCommunicationRequest) {
         executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            path,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateCommunicationRequest> requestEntity = new HttpEntity<>(createCommunicationRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create communication. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

    public void createCompanyIdentifier(RequestContext requestContext, String companyId, CreateIdentifierRequest createIdentifierRequest) {
        log.debug("#createCompanyIdentifier: requestContext = {}, companyId = {}, createIdentifierRequest = {}", requestContext, companyId, createIdentifierRequest);
        createIdentifier(requestContext, format(COMPANY_IDENTIFIERS_RESOURCE, companyId), createIdentifierRequest);
    }

    public void createSubsidiaryIdentifier(RequestContext requestContext, String parentCompanyId, String subsidiaryId, CreateIdentifierRequest createIdentifierRequest) {
        log.debug("#createSubsidiaryIdentifier: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}, createIdentifierRequest = {}", requestContext, parentCompanyId, subsidiaryId, createIdentifierRequest);
        createIdentifier(requestContext, format(SUBSIDIARY_IDENTIFIERS_RESOURCE, parentCompanyId, subsidiaryId), createIdentifierRequest);
    }

    private void createIdentifier(RequestContext requestContext, String path, CreateIdentifierRequest createIdentifierRequest) {
        executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            path,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateIdentifierRequest> requestEntity = new HttpEntity<>(createIdentifierRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create identifier. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

    public void createCompanyAs2InboundDecryptionConfiguration(
            RequestContext requestContext,
            String companyId,
            CreateAs2InboundDecryptionConfigurationRequest createSignatureVerificationConfigurationRequest
    ) {
        log.debug("createCompanyAs2InboundDecryptionConfiguration: requestContext = {}, companyId = {}, createSignatureVerificationConfigurationRequest = {}", requestContext, companyId, createSignatureVerificationConfigurationRequest);
        createAs2InboundDecryptionConfiguration(requestContext, format(COMPANY_CONFIG_DECRYPT_RESOURCE, companyId), createSignatureVerificationConfigurationRequest);
    }

    public void createSubsidiaryAs2InboundDecryptionConfiguration(
            RequestContext requestContext,
            String parentCompanyId,
            String subsidiaryId,
            CreateAs2InboundDecryptionConfigurationRequest createSignatureVerificationConfigurationRequest
    ) {
        log.debug("createSubsidiaryAs2InboundDecryptionConfiguration: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}, createSignatureVerificationConfigurationRequest = {}", requestContext, parentCompanyId, subsidiaryId, createSignatureVerificationConfigurationRequest);
        createAs2InboundDecryptionConfiguration(requestContext, format(SUBSIDIARY_CONFIG_DECRYPT_RESOURCE, parentCompanyId, subsidiaryId), createSignatureVerificationConfigurationRequest);
    }

    private void createAs2InboundDecryptionConfiguration(RequestContext requestContext, String path, CreateAs2InboundDecryptionConfigurationRequest createSignatureVerificationConfigurationRequest) {

        executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            path,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateAs2InboundDecryptionConfigurationRequest> requestEntity = new HttpEntity<>(createSignatureVerificationConfigurationRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create signature verification configurations. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

    private ProfileConfiguration resolveCompanyProfileConfiguration(RequestContext requestContext, String companyId) {
        JSONObject profileConfigurationJsonObject = executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext,
            format(COMPANY_PROFILE_CONFIGURATION_RESOURCE, companyId),
            JSONObject::new
        );
        if (profileConfigurationJsonObject == null) {
            return null;
        }

        JSONArray decryptionConfigJsonArray = executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext,
            format(COMPANY_CONFIG_DECRYPT_RESOURCE, companyId),
            JSONArray::new
        );
        if (IterableUtils.isEmpty(decryptionConfigJsonArray)) {
            return null;
        }

        return mergeProfileConfigurationParts(decryptionConfigJsonArray, profileConfigurationJsonObject);
    }

    private ProfileConfiguration resolveSubsidiaryProfileConfiguration(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        JSONObject profileConfigurationJsonObject = executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext,
            format(SUBSIDIARY_PROFILE_CONFIGURATION_RESOURCE, parentCompanyId, subsidiaryId),
            JSONObject::new
        );
        if (profileConfigurationJsonObject == null) {
            return null;
        }

        JSONArray decryptionConfigJsonArray = executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext,
            format(SUBSIDIARY_CONFIG_DECRYPT_RESOURCE, parentCompanyId, subsidiaryId),
            JSONArray::new
        );
        if (IterableUtils.isEmpty(decryptionConfigJsonArray)) {
            return null;
        }

        return mergeProfileConfigurationParts(decryptionConfigJsonArray, profileConfigurationJsonObject);
    }

    private ProfileConfiguration mergeProfileConfigurationParts(JSONArray decryptionConfigJsonArray, JSONObject profileConfigurationJsonObject) {
        JSONObject signatureValidationConfig = new JSONObject();
        JSONObject configurationEntries = new JSONObject();

        signatureValidationConfig.put("ConfigurationType", "DECRYPTION_CONFIG");
        signatureValidationConfig.put("ConfigurationEntries", configurationEntries);

        for (int i = 0; i < decryptionConfigJsonArray.length(); i++) {
            JSONObject signatureVerificationConfigurationsJSONObject = decryptionConfigJsonArray.getJSONObject(i);
            configurationEntries.put(signatureVerificationConfigurationsJSONObject.getString("Alias"), signatureVerificationConfigurationsJSONObject);
        }

        profileConfigurationJsonObject.put("DecryptionConfigurations", signatureValidationConfig);

        return parseProfileConfiguration(profileConfigurationJsonObject);
    }

}
