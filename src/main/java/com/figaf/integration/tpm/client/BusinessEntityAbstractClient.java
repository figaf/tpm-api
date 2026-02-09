package com.figaf.integration.tpm.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.ArtifactProperties;
import com.figaf.integration.tpm.entity.TpmBusinessEntity;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.*;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;

import static com.figaf.integration.common.utils.Utils.optString;

public abstract class BusinessEntityAbstractClient extends TpmBaseClient {

    public BusinessEntityAbstractClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    protected TpmBusinessEntity buildTpmBusinessEntity(JSONObject jsonObject, TpmObjectType tpmObjectType) {
        TpmBusinessEntity tpmBusinessEntity = new TpmBusinessEntity();
        tpmBusinessEntity.setObjectId(jsonObject.getString("id"));
        tpmBusinessEntity.setTpmObjectType(tpmObjectType);
        tpmBusinessEntity.setDisplayedName(jsonObject.getString("displayName"));

        JSONObject administrativeDataJsonObject = jsonObject.getJSONObject("administrativeData");
        tpmBusinessEntity.setAdministrativeData(buildAdministrativeDataObject(administrativeDataJsonObject));

        JSONObject artifactPropertiesJsonObject = jsonObject.getJSONObject("artifactProperties");
        tpmBusinessEntity.setArtifactProperties(buildArtifactProperties(artifactPropertiesJsonObject));

        tpmBusinessEntity.setPayload(jsonObject.toString());
        return tpmBusinessEntity;
    }

    protected List<System> parseSystemsList(String response) throws JsonProcessingException {
        JSONArray jsonArray = new JSONArray(response);
        List<System> systems = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String rawPayload = jsonObject.toString();
            System system = jsonMapper.readValue(rawPayload, System.class);
            system.setRawPayload(rawPayload);
            systems.add(system);
        }
        return systems.stream()
            .sorted(Comparator.comparing(System::getId))
            .toList();
    }

    protected List<Identifier> parseIdentifiersList(String response) throws JsonProcessingException {
        JSONArray jsonArray = new JSONArray(response);
        List<Identifier> identifiers = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String rawPayload = jsonObject.toString();
            Identifier identifier = jsonMapper.readValue(rawPayload, Identifier.class);
            identifier.setRawPayload(rawPayload);
            identifiers.add(identifier);
        }
        return identifiers.stream()
            .sorted(Comparator.comparing(Identifier::getId))
            .toList();
    }

    protected List<Channel> parseChannelsList(String response) throws JsonProcessingException {
        JSONArray jsonArray = new JSONArray(response);
        List<Channel> channels = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String rawPayload = jsonObject.toString();
            Channel channel = jsonMapper.readValue(rawPayload, Channel.class);
            channel.setRawPayload(rawPayload);
            channels.add(channel);
        }
        return channels.stream()
            .sorted(Comparator.comparing(Channel::getId))
            .toList();
    }

    protected List<Parameter> parseParametersList(String response) throws JsonProcessingException {
        JSONArray jsonArray = new JSONArray(response);
        List<Parameter> parameters = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String rawPayload = jsonObject.toString();
            Parameter parameter = jsonMapper.readValue(rawPayload, Parameter.class);
            parameter.setRawPayload(rawPayload);
            parameters.add(parameter);
        }
        return parameters.stream()
            .sorted(Comparator.comparing(Parameter::getId))
            .toList();
    }

    protected TpmObjectDetails buildTpmObjectDetails(String responseEntityBody) {
        Object object = new JSONTokener(responseEntityBody).nextValue();
        JSONObject tpmObjectDetailsJsonObject;
        if (object instanceof JSONArray jsonArray) {
            tpmObjectDetailsJsonObject = jsonArray.getJSONObject(0);
        } else if (object instanceof JSONObject jsonObject) {
            tpmObjectDetailsJsonObject = jsonObject;
        } else {
            throw new JSONException("Unexpected JSON type: " + object.getClass());
        }
        TpmObjectDetails tpmObjectDetails = new TpmObjectDetails();

        setTpmObjectDetailsProperties(tpmObjectDetailsJsonObject, tpmObjectDetails);

        JSONObject profileJsonObject = tpmObjectDetailsJsonObject.getJSONObject("Profile");
        tpmObjectDetails.setProfile(parseProfileDto(profileJsonObject));

        tpmObjectDetails.setRawPayload(tpmObjectDetailsJsonObject.toString());

        return tpmObjectDetails;
    }

    protected ProfileDto parseProfileDto(JSONObject profileJsonObject) {
        ProfileDto profile = new ProfileDto();

        JSONObject jsonAddress = profileJsonObject.getJSONObject("Address");
        profile.setAddress(parseAddress(jsonAddress));

        JSONArray jsonContactPersonList = profileJsonObject.getJSONArray("ContactPersonList");
        profile.setContactPersonList(parseContactPersons(jsonContactPersonList));

        JSONObject jsonBusinessContext = profileJsonObject.getJSONObject("BusinessContext");
        BusinessContext BusinessContext = new BusinessContext();

        if (jsonBusinessContext.has("BusinessProcess")) {
            JSONObject jsonBusinessProcess = jsonBusinessContext.getJSONObject("BusinessProcess");
            BusinessContext.setBusinessProcess(parseBusinessProcessDTO(jsonBusinessProcess));
        }

        if (jsonBusinessContext.has("BusinessProcessRole")) {
            JSONObject jsonBusinessProcessRole = jsonBusinessContext.getJSONObject("BusinessProcessRole");
            BusinessContext.setBusinessProcessRole(parseBusinessProcessRoleDTO(jsonBusinessProcessRole));
        }

        if (jsonBusinessContext.has("IndustryClassification")) {
            JSONObject jsonIndustryClassification = jsonBusinessContext.getJSONObject("IndustryClassification");
            BusinessContext.setIndustryClassification(parseIndustryClassificationDTO(jsonIndustryClassification));
        }

        if (jsonBusinessContext.has("ProductClassification")) {
            JSONObject jsonProductClassification = jsonBusinessContext.getJSONObject("ProductClassification");
            BusinessContext.setProductClassification(parseProductClassificationDTO(jsonProductClassification));
        }

        if (jsonBusinessContext.has("GeoPolitical")) {
            JSONObject jsonGeoPolitical = jsonBusinessContext.getJSONObject("GeoPolitical");
            BusinessContext.setGeoPolitical(parseGeoPoliticalDTO(jsonGeoPolitical));
        }

        profile.setBusinessContext(BusinessContext);

        return profile;
    }

    protected ProfileConfiguration parseProfileConfiguration(JSONObject profileConfig) {
        ProfileConfiguration profileConfiguration = new ProfileConfiguration();
        profileConfiguration.setParentId(profileConfig.getString("ParentId"));
        profileConfiguration.setParentArtifactType(profileConfig.getString("ParentArtifactType"));
        profileConfiguration.setDisplayName(profileConfig.getString("displayName"));
        profileConfiguration.setId(profileConfig.getString("id"));
        profileConfiguration.setArtifactType(profileConfig.getString("artifactType"));
        profileConfiguration.setAdministrativeData(buildAdministrativeDataObject(profileConfig.getJSONObject("administrativeData")));
        profileConfiguration.setRawPayload(profileConfig.toString());
        return profileConfiguration;
    }

    private void setTpmObjectDetailsProperties(JSONObject jsonTradingPartnerVerbose, TpmObjectDetails tradingPartner) {
        tradingPartner.setName(optString(jsonTradingPartnerVerbose, "Name"));
        tradingPartner.setShortName(optString(jsonTradingPartnerVerbose, "ShortName"));
        tradingPartner.setWebURL(optString(jsonTradingPartnerVerbose, "WebURL"));
        tradingPartner.setLogoId(optString(jsonTradingPartnerVerbose, "LogoId"));
        tradingPartner.setEmailAddress(optString(jsonTradingPartnerVerbose, "EmailAddress"));
        tradingPartner.setPhoneNumber(optString(jsonTradingPartnerVerbose, "PhoneNumber"));
        tradingPartner.setDocumentSchemaVersion(optString(jsonTradingPartnerVerbose, "DocumentSchemaVersion"));
        tradingPartner.setArtifactType(optString(jsonTradingPartnerVerbose, "artifactType"));
        tradingPartner.setArtifactStatus(optString(jsonTradingPartnerVerbose, "artifactStatus"));
        tradingPartner.setDisplayedName(optString(jsonTradingPartnerVerbose, "displayName"));
        tradingPartner.setSemanticVersion(optString(jsonTradingPartnerVerbose, "semanticVersion"));
        tradingPartner.setUniqueId(optString(jsonTradingPartnerVerbose, "uniqueId"));
        tradingPartner.setId(optString(jsonTradingPartnerVerbose, "id"));
        tradingPartner.setSearchableAttributes(parseSearchableAttributes(jsonTradingPartnerVerbose));
        tradingPartner.setAdministrativeData(buildAdministrativeDataObject(jsonTradingPartnerVerbose.getJSONObject("administrativeData")));
    }

    private Map<String, List<String>> parseSearchableAttributes(JSONObject jsonObject) {
        Map<String, List<String>> searchableAttributesMap = new HashMap<>();
        if (jsonObject.has("searchableAttributes")) {
            JSONObject jsonSearchableAttributes = jsonObject.getJSONObject("searchableAttributes");
            for (String key : jsonSearchableAttributes.keySet()) {
                JSONArray jsonArray = jsonSearchableAttributes.getJSONArray(key);
                List<String> values = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    values.add(jsonArray.getString(i));
                }
                searchableAttributesMap.put(key, values);
            }
        }
        return searchableAttributesMap;
    }

    private BusinessProcess parseBusinessProcessDTO(JSONObject jsonBusinessProcess) {
        BusinessProcess businessProcessDTO = new BusinessProcess();
        businessProcessDTO.setBusinessProcessCodes(parseStringList(jsonBusinessProcess.optJSONArray("BusinessProcessCodeList")));
        return businessProcessDTO;
    }

    private List<String> parseStringList(JSONArray jsonArray) {
        List<String> stringList = new ArrayList<>();
        if (jsonArray == null) {
            return stringList;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            stringList.add(jsonArray.optString(i));
        }
        return stringList;
    }

    private BusinessProcessRole parseBusinessProcessRoleDTO(JSONObject jsonBusinessProcessRole) {
        BusinessProcessRole businessProcessRoleDTO = new BusinessProcessRole();
        businessProcessRoleDTO.setBusinessProcessRoleCodes(parseStringList(jsonBusinessProcessRole.optJSONArray("BusinessProcessRoleCodeList")));
        return businessProcessRoleDTO;
    }

    private IndustryClassification parseIndustryClassificationDTO(JSONObject jsonIndustryClassification) {
        IndustryClassification industryClassificationDTO = new IndustryClassification();
        industryClassificationDTO.setIndustryClassificationCodeList(parseStringList(jsonIndustryClassification.optJSONArray("IndustryClassificationCodeList")));
        return industryClassificationDTO;
    }

    private ProductClassification parseProductClassificationDTO(JSONObject jsonProductClassification) {
        ProductClassification productClassificationDTO = new ProductClassification();
        productClassificationDTO.setProductClassificationCodeList(parseStringList(jsonProductClassification.optJSONArray("ProductClassificationCodeList")));
        return productClassificationDTO;
    }

    private Address parseAddress(JSONObject jsonAddress) {
        Address address = new Address();
        address.setCityName(optString(jsonAddress, "CityName"));
        address.setCountryCode(optString(jsonAddress, "CountryCode"));
        address.setCountrySubdivisionCode(optString(jsonAddress, "CountrySubdivisionCode"));
        address.setHouseNumber(optString(jsonAddress, "HouseNumber"));
        address.setPoBox(optString(jsonAddress, "POBox"));
        address.setPoBoxPostalCode(optString(jsonAddress, "POBoxPostalCode"));
        address.setStreetName(optString(jsonAddress, "StreetName"));
        address.setStreetPostalCode(optString(jsonAddress, "StreetPostalCode"));
        return address;
    }

    private GeoPolitical parseGeoPoliticalDTO(JSONObject jsonGeoPolitical) {
        GeoPolitical geoPoliticalDTO = new GeoPolitical();
        JSONArray jsonCountryInfoList = jsonGeoPolitical.optJSONArray("CountryInfoList");
        if (jsonCountryInfoList == null) {
            return geoPoliticalDTO;
        }
        List<CountryInfo> countryInfos = new ArrayList<>();
        for (int i = 0; i < jsonCountryInfoList.length(); i++) {
            JSONObject jsonCountryInfo = jsonCountryInfoList.getJSONObject(i);
            CountryInfo countryInfoDTO = new CountryInfo();
            countryInfoDTO.setCountryCode(jsonCountryInfo.optString("CountryCode"));
            countryInfoDTO.setCountrySubdivisionCode(jsonCountryInfo.optString("CountrySubdivisionCode"));
            countryInfos.add(countryInfoDTO);
        }
        geoPoliticalDTO.setCountryInfoList(countryInfos);
        return geoPoliticalDTO;
    }

    public List<ContactPersonDTO> parseContactPersons(JSONArray jsonContactPersonList) {
        List<ContactPersonDTO> contactPersonDTOs = new ArrayList<>();
        for (int i = 0; i < jsonContactPersonList.length(); i++) {
            JSONObject jsonContactPerson = jsonContactPersonList.getJSONObject(i);
            ContactPersonDTO contactPersonDTO = parseContactPerson(jsonContactPerson);
            contactPersonDTOs.add(contactPersonDTO);
        }
        return contactPersonDTOs;
    }

    private ContactPersonDTO parseContactPerson(final JSONObject jsonContactPerson) {
        ContactPersonDTO contactPersonDTO = new ContactPersonDTO();
        contactPersonDTO.setId(optString(jsonContactPerson, "ID"));
        contactPersonDTO.setFamilyName(optString(jsonContactPerson, "FamilyName"));
        contactPersonDTO.setGivenName(optString(jsonContactPerson, "GivenName"));
        contactPersonDTO.setPrimaryContact(jsonContactPerson.optBoolean("IsPrimaryContact"));
        contactPersonDTO.setNewVersion(jsonContactPerson.optBoolean("IsNewVersion"));
        contactPersonDTO.setDocumentSchemaVersion(optString(jsonContactPerson, "DocumentSchemaVersion"));
        contactPersonDTO.setArtifactStatus(optString(jsonContactPerson, "artifactStatus"));

        if (jsonContactPerson.has("Address")) {
            JSONObject jsonAddress = jsonContactPerson.getJSONObject("Address");
            ContactPersonAddressDTO contactPersonAddressDTO = parseContactPersonAddress(jsonAddress);
            contactPersonDTO.setAddress(contactPersonAddressDTO);
        }

        return contactPersonDTO;
    }

    private ContactPersonAddressDTO parseContactPersonAddress(final JSONObject jsonAddress) {
        ContactPersonAddressDTO contactPersonAddressDTO = new ContactPersonAddressDTO();
        contactPersonAddressDTO.setCountryCode(optString(jsonAddress, "CountryCode"));
        return contactPersonAddressDTO;
    }

    protected ArtifactProperties buildArtifactProperties(JSONObject jsonObject) {
        ArtifactProperties artifactProperties = new ArtifactProperties();
        artifactProperties.setWebURL(jsonObject.optString("webURL"));
        artifactProperties.setShortName(jsonObject.optString("shortName"));
        artifactProperties.setLogoID(jsonObject.optString("logoID"));
        return artifactProperties;
    }

}
