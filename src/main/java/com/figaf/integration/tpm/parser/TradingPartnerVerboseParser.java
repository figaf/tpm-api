package com.figaf.integration.tpm.parser;

import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.trading.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class TradingPartnerVerboseParser {

    public TradingPartnerRawResponse createTradingPartnerRawResponse(String jsonResponse){
        TradingPartnerRawResponse tradingPartnerRawResponse = new TradingPartnerRawResponse();
        tradingPartnerRawResponse.setJsonPayload(jsonResponse);
        return tradingPartnerRawResponse;
    }

    public TradingPartner parse(String jsonResponse) {
        JSONObject tradingPartnerVerboseResponse = new JSONObject(jsonResponse);
        TradingPartner tradingPartner = new TradingPartner();

        setTradingPartnerVerboseProperties(tradingPartnerVerboseResponse, tradingPartner);

        JSONObject jsonArtifactProperties = tradingPartnerVerboseResponse.getJSONObject("artifactProperties");
        tradingPartner.setArtifactProperties(parseArtifactProperties(jsonArtifactProperties));

        JSONObject jsonProfile = tradingPartnerVerboseResponse.getJSONObject("Profile");
        ProfileDto profile = new ProfileDto();

        JSONObject jsonAddress = jsonProfile.getJSONObject("Address");
        profile.setAddress(parseAddress(jsonAddress));

        JSONArray jsonContactPersonList = jsonProfile.getJSONArray("ContactPersonList");
        profile.setContactPersonList(parseContactPersons(jsonContactPersonList));

        JSONObject jsonBusinessContext = jsonProfile.getJSONObject("BusinessContext");
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
        tradingPartner.setProfile(profile);
        return tradingPartner;
    }

    public void setTradingPartnerVerboseProperties(JSONObject jsonTradingPartnerVerbose, TradingPartner tradingPartner) {
        tradingPartner.setName(getString(jsonTradingPartnerVerbose, "Name"));
        tradingPartner.setShortName(getString(jsonTradingPartnerVerbose, "ShortName"));
        tradingPartner.setWebURL(getString(jsonTradingPartnerVerbose, "WebURL"));
        tradingPartner.setLogoId(getString(jsonTradingPartnerVerbose, "LogoId"));
        tradingPartner.setEmailAddress(getString(jsonTradingPartnerVerbose, "EmailAddress"));
        tradingPartner.setPhoneNumber(getString(jsonTradingPartnerVerbose, "PhoneNumber"));
        tradingPartner.setDocumentSchemaVersion(getString(jsonTradingPartnerVerbose, "DocumentSchemaVersion"));
        tradingPartner.setArtifactType(getString(jsonTradingPartnerVerbose, "artifactType"));
        tradingPartner.setArtifactStatus(getString(jsonTradingPartnerVerbose, "artifactStatus"));
        tradingPartner.setDisplayedName(getString(jsonTradingPartnerVerbose, "displayName"));
        tradingPartner.setSemanticVersion(getString(jsonTradingPartnerVerbose, "semanticVersion"));
        tradingPartner.setUniqueId(getString(jsonTradingPartnerVerbose, "uniqueId"));
        tradingPartner.setId(getString(jsonTradingPartnerVerbose, "id"));
        tradingPartner.setSearchableAttributes(parseSearchableAttributes(jsonTradingPartnerVerbose));
        tradingPartner.setAdministrativeData(parseAdministrativeData(jsonTradingPartnerVerbose));
    }

    public AdministrativeData parseAdministrativeData(JSONObject jsonTradingPartnerVerbose) {
        AdministrativeData administrativeDataDTO = new AdministrativeData();
        if (jsonTradingPartnerVerbose.has("administrativeData")) {
            JSONObject jsonAdministrativeData = jsonTradingPartnerVerbose.getJSONObject("administrativeData");
            administrativeDataDTO.setCreatedAt(new Date(jsonAdministrativeData.getLong("createdAt")));
            administrativeDataDTO.setCreatedBy(jsonAdministrativeData.getString("createdBy"));
            administrativeDataDTO.setModifiedAt(new Date(jsonAdministrativeData.getLong("modifiedAt")));
            administrativeDataDTO.setModifiedBy(jsonAdministrativeData.getString("modifiedBy"));
        }
        return administrativeDataDTO;
    }

    private ArtifactProperties parseArtifactProperties(final JSONObject jsonArtifactProperties) {
        ArtifactProperties artifactProperties = new ArtifactProperties();
        artifactProperties.setWebURL(getString(jsonArtifactProperties, "webURL"));
        artifactProperties.setShortName(getString(jsonArtifactProperties, "shortName"));
        artifactProperties.setLogoID(getString(jsonArtifactProperties, "logoID"));
        return artifactProperties;
    }

    private BusinessProcess parseBusinessProcessDTO(JSONObject jsonBusinessProcess) {
        BusinessProcess businessProcessDTO = new BusinessProcess();
        businessProcessDTO.setBusinessProcessCodes(parseStringList(jsonBusinessProcess.getJSONArray("BusinessProcessCodeList")));
        return businessProcessDTO;
    }

    private List<String> parseStringList(JSONArray jsonArray) {
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            stringList.add(jsonArray.getString(i));
        }
        return stringList;
    }

    private BusinessProcessRole parseBusinessProcessRoleDTO(JSONObject jsonBusinessProcessRole) {
        BusinessProcessRole businessProcessRoleDTO = new BusinessProcessRole();
        businessProcessRoleDTO.setBusinessProcessRoleCodes(parseStringList(jsonBusinessProcessRole.getJSONArray("BusinessProcessRoleCodeList")));
        return businessProcessRoleDTO;
    }

    private IndustryClassification parseIndustryClassificationDTO(JSONObject jsonIndustryClassification) {
        IndustryClassification industryClassificationDTO = new IndustryClassification();
        industryClassificationDTO.setIndustryClassificationCodeList(parseStringList(jsonIndustryClassification.getJSONArray("IndustryClassificationCodeList")));
        return industryClassificationDTO;
    }

    private ProductClassification parseProductClassificationDTO(JSONObject jsonProductClassification) {
        ProductClassification productClassificationDTO = new ProductClassification();
        productClassificationDTO.setProductClassificationCodeList(parseStringList(jsonProductClassification.getJSONArray("ProductClassificationCodeList")));
        return productClassificationDTO;
    }

    private Address parseAddress(JSONObject jsonAddress) {
        Address address = new Address();
        address.setCityName(getString(jsonAddress, "CityName"));
        address.setCountryCode(getString(jsonAddress, "CountryCode"));
        address.setCountrySubdivisionCode(getString(jsonAddress, "CountrySubdivisionCode"));
        address.setHouseNumber(getString(jsonAddress, "HouseNumber"));
        address.setPOBox(getString(jsonAddress, "POBox"));
        address.setPOBoxPostalCode(getString(jsonAddress, "POBoxPostalCode"));
        address.setStreetName(getString(jsonAddress, "StreetName"));
        address.setStreetPostalCode(getString(jsonAddress, "StreetPostalCode"));
        return address;
    }

    private GeoPolitical parseGeoPoliticalDTO(JSONObject jsonGeoPolitical) {
        GeoPolitical geoPoliticalDTO = new GeoPolitical();
        JSONArray jsonCountryInfoList = jsonGeoPolitical.getJSONArray("CountryInfoList");
        List<CountryInfo> countryInfos = new ArrayList<>();
        for (int i = 0; i < jsonCountryInfoList.length(); i++) {
            JSONObject jsonCountryInfo = jsonCountryInfoList.getJSONObject(i);
            CountryInfo countryInfoDTO = new CountryInfo();
            countryInfoDTO.setCountryCode(jsonCountryInfo.getString("CountryCode"));
            countryInfoDTO.setCountrySubdivisionCode(jsonCountryInfo.getString("CountrySubdivisionCode"));
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
        contactPersonDTO.setId(getString(jsonContactPerson, "ID"));
        contactPersonDTO.setFamilyName(getString(jsonContactPerson, "FamilyName"));
        contactPersonDTO.setGivenName(getString(jsonContactPerson, "GivenName"));
        contactPersonDTO.setPrimaryContact(getBoolean(jsonContactPerson, "IsPrimaryContact"));
        contactPersonDTO.setNewVersion(getBoolean(jsonContactPerson, "IsNewVersion"));
        contactPersonDTO.setDocumentSchemaVersion(getString(jsonContactPerson, "DocumentSchemaVersion"));
        contactPersonDTO.setArtifactStatus(getString(jsonContactPerson, "artifactStatus"));

        if (jsonContactPerson.has("Address")) {
            JSONObject jsonAddress = jsonContactPerson.getJSONObject("Address");
            ContactPersonAddressDTO contactPersonAddressDTO = parseContactPersonAddress(jsonAddress);
            contactPersonDTO.setAddress(contactPersonAddressDTO);
        }

        return contactPersonDTO;
    }

    private ContactPersonAddressDTO parseContactPersonAddress(final JSONObject jsonAddress) {
        ContactPersonAddressDTO contactPersonAddressDTO = new ContactPersonAddressDTO();
        contactPersonAddressDTO.setCountryCode(getString(jsonAddress, "CountryCode"));
        return contactPersonAddressDTO;
    }

    private String getString(final JSONObject jsonObject, final String key) {
        return jsonObject.has(key) ? jsonObject.getString(key) : null;
    }

    private boolean getBoolean(final JSONObject jsonObject, final String key) {
        return jsonObject.has(key) && jsonObject.getBoolean(key);
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
}
