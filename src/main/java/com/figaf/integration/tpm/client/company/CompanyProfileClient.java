package com.figaf.integration.tpm.client.company;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClientForTradingPartnerOrCompanyOrSubsidiary;
import com.figaf.integration.tpm.entity.*;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.TpmObjectDetails;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.figaf.integration.common.utils.Utils.optString;
import static java.lang.String.format;

@Slf4j
public class CompanyProfileClient extends TpmBaseClientForTradingPartnerOrCompanyOrSubsidiary {

    public CompanyProfileClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<Company> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            COMPANY_PROFILE_RESOURCE,
            response -> {
                JSONArray companiesJsonArray = new JSONArray(response);
                List<Company> companies = new ArrayList<>();
                for (int i = 0; i < companiesJsonArray.length(); i++) {
                    JSONObject companyJsonObject = companiesJsonArray.getJSONObject(i);
                    Company company = new Company();
                    company.setObjectId(companyJsonObject.getString("id"));
                    company.setTpmObjectType(TpmObjectType.CLOUD_COMPANY_PROFILE);
                    company.setDisplayedName(companyJsonObject.getString("displayName"));

                    JSONObject administrativeDataJsonObject = companyJsonObject.getJSONObject("administrativeData");
                    company.setAdministrativeData(buildAdministrativeDataObject(administrativeDataJsonObject));

                    JSONObject profileJsonObject = companyJsonObject.getJSONObject("Profile");
                    company.setProfile(parseProfileDto(profileJsonObject));

                    company.setPayload(companyJsonObject.toString());

                    companies.add(company);
                }

                for (TpmObjectMetadata company : companies) {
                    List<Subsidiary> subsidiaries = getSubsidiaries(requestContext, company.getObjectId());
                    List<TpmObjectReference> tpmObjectReferences = new ArrayList<>();
                    for (Subsidiary subsidiary : subsidiaries) {
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

    public List<Subsidiary> getSubsidiaries(RequestContext requestContext, String companyId) {
        log.debug("#getSubsidiaries: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext,
            format(COMPANY_SUBSIDIARIES_RESOURCE, companyId),
            response -> {
                JSONArray subsidiariesJsonArray = new JSONArray(response);
                List<Subsidiary> subsidiaries = new ArrayList<>();
                for (int i = 0; i < subsidiariesJsonArray.length(); i++) {
                    JSONObject subsidiaryJsonObject = subsidiariesJsonArray.getJSONObject(i);
                    Subsidiary subsidiary = new Subsidiary();
                    subsidiary.setObjectId(subsidiaryJsonObject.getString("id"));
                    subsidiary.setTpmObjectType(TpmObjectType.CLOUD_SUBSIDIARY);
                    subsidiary.setDisplayedName(subsidiaryJsonObject.getString("displayName"));
                    JSONObject administrativeDataJsonObject = subsidiaryJsonObject.getJSONObject("administrativeData");
                    AdministrativeData administrativeData = buildAdministrativeDataObject(administrativeDataJsonObject);
                    subsidiary.setAdministrativeData(administrativeData);
                    subsidiary.setPayload(subsidiaryJsonObject.toString());

                    subsidiary.setShortName(optString(subsidiaryJsonObject, "ShortName"));
                    subsidiary.setWebUrl(optString(subsidiaryJsonObject, "WebURL"));
                    subsidiary.setLogoId(optString(subsidiaryJsonObject, "LogoId"));
                    subsidiary.setEmailAddress(optString(subsidiaryJsonObject, "EmailAddress"));
                    subsidiary.setPhoneNumber(optString(subsidiaryJsonObject, "PhoneNumber"));

                    JSONObject profileJsonObject = subsidiaryJsonObject.getJSONObject("Profile");
                    subsidiary.setProfile(parseProfileDto(profileJsonObject));

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
            this::buildTpmObjectDetails
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

        ProfileConfiguration profileConfiguration = resolveCompanyProfileConfiguration(tpmObjectDetails.getId(), requestContext);

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
            requestContext,
            format(COMPANY_SYSTEMS_RESOURCE, companyId),
            this::parseSystemsList
        );
    }

    public List<System> getSubsidiarySystems(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getSubsidiarySystems: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}", requestContext, parentCompanyId, subsidiaryId);
        return executeGet(
            requestContext,
            format(SUBSIDIARY_SYSTEMS_RESOURCE, parentCompanyId, subsidiaryId),
            this::parseSystemsList
        );
    }

    public List<Identifier> getCompanyIdentifiers(RequestContext requestContext, String companyId) {
        log.debug("#getCompanyIdentifiers: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext,
            format(COMPANY_IDENTIFIERS_RESOURCE, companyId),
            this::parseIdentifiersList
        );
    }

    public List<Identifier> getSubsidiaryIdentifiers(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getSubsidiaryIdentifiers: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}", requestContext, parentCompanyId, subsidiaryId);
        return executeGet(
            requestContext,
            format(SUBSIDIARY_IDENTIFIERS_RESOURCE, parentCompanyId, subsidiaryId),
            this::parseIdentifiersList
        );
    }

    public List<Channel> getCompanyChannels(RequestContext requestContext, String companyId, String systemId) {
        log.debug("#getCompanyChannels: requestContext = {}, companyId = {}, systemId = {}", requestContext, companyId, systemId);
        return executeGet(
            requestContext,
            format(COMPANY_CHANNELS_RESOURCE, companyId, systemId),
            this::parseChannelsList
        );
    }

    public List<Channel> getSubsidiaryChannels(RequestContext requestContext, String parentCompanyId, String subsidiaryId, String systemId) {
        log.debug("#getSubsidiaryChannels: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}, systemId = {}", requestContext, parentCompanyId, subsidiaryId, systemId);
        return executeGet(
            requestContext,
            format(SUBSIDIARY_CHANNELS_RESOURCE, parentCompanyId, subsidiaryId, systemId),
            this::parseChannelsList
        );
    }

    private ProfileConfiguration resolveCompanyProfileConfiguration(String companyId, RequestContext requestContext) {
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
