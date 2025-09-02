package com.figaf.integration.tpm.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.trading.verbose.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.figaf.integration.common.utils.Utils.optString;

/**
 * @author Kostas Charalambous
 */
public abstract class TpmBaseClient extends BaseClient {

    protected static final String MIG_RESOURCE = "/api/1.0/migs";
    public static final String MIG_RESOURCE_BY_ID = "/api/1.0/migs/%s";
    public static final String MIG_CREATE_DRAFT_RESOURCE = "/api/1.0/migs/%s/migVersions?source=%s&status=draft";
    protected static final String COMPANY_PROFILE_RESOURCE = "/itspaces/tpm/company";
    protected static final String COMPANY_SUBSIDIARIES_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries";
    protected static final String COMPANY_SYSTEMS_RESOURCE = "/itspaces/tpm/company/%s/systems";
    protected static final String SUBSIDIARY_SYSTEMS_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/systems";
    protected static final String COMPANY_IDENTIFIERS_RESOURCE = "/itspaces/tpm/company/%s/identifiers";
    protected static final String SUBSIDIARY_IDENTIFIERS_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/identifiers";
    protected static final String COMPANY_CHANNELS_RESOURCE = "/itspaces/tpm/company/%s/systems/%s/channels";
    protected static final String SUBSIDIARY_CHANNELS_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/systems/%s/channels";

    protected static final String TRADING_PARTNER_RESOURCE = "/itspaces/tpm/tradingpartners";
    protected static final String TRADING_PARTNER_RESOURCE_BY_ID = "/itspaces/tpm/tradingpartners/%s";
    protected static final String AGREEMENT_TEMPLATE_RESOURCE = "/itspaces/tpm/api/2.0/agreementtemplates";
    protected static final String AGREEMENT_TEMPLATE_B2B_SCENARIOS_RESOURCE = "/itspaces/tpm/api/2.0/agreementtemplates/%s/b2bscenario/%s";
    public static final String MIG_DELETE_DRAFT_RESOURCE = "/api/1.0/migs/%s/migVersions/%s";
    protected static final String AGREEMENTS_RESOURCE = "/itspaces/tpm/api/2.0/tradingpartneragreements";
    protected static final String AGREEMENT_RESOURCE = "/itspaces/tpm/api/2.0/tradingpartneragreements/%s";
    protected static final String B2B_SCENARIOS_RESOURCE = "/itspaces/tpm/api/2.0/tradingpartneragreements/%s/b2bscenario";
    protected static final String B2B_SCENARIO_RESOURCE = "/itspaces/tpm/api/2.0/tradingpartneragreements/%s/b2bscenario/%s";
    protected static final String SYSTEM_TYPES_RESOURCE = "/itspaces/tpm/systemtypes";
    protected static final String TRADING_PARTNER_SYSTEMS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/systems";
    protected static final String TYPE_SYSTEMS_RESOURCE = "/itspaces/tpm/bootstrap/?type=typesystems";
    protected static final String TYPE_SYSTEM_VERSIONS_RESOURCE = "/itspaces/tpm/api/2.0/typesystems/%s?artifacttype=TypeSystemVersion";
    protected static final String SENDER_ADAPTER_LIST_RESOURCE = "/itspaces/tpm/bootstrap?type=adapterlist&direction=Sender";
    protected static final String RECEIVER_ADAPTER_LIST_RESOURCE = "/itspaces/tpm/bootstrap?type=adapterlist&direction=Receiver";
    protected static final String PRODUCTS_RESOURCE = "/itspaces/tpm/bootstrap/?type=products";
    protected static final String TRADING_PARTNER_IDENTIFIERS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/identifiers";
    protected static final String COMMUNICATIONS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/systems/%s/channels";
    protected static final String SIGNATURE_VERIFICATION_CONFIGURATIONS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/config.signval";

    protected static final String CROSS_ACTIONS_UPLOAD_ARCHIVE_RESOURCE = "/itspaces/tpm/resourcefile";
    protected static final String CROSS_ACTIONS_EXECUTE_IMPORT_RESOURCE = "/itspaces/tpm/api/2.0/tasks.bulk";
    protected static final String CROSS_ACTIONS_TASK_STATUS_RESOURCE = "/itspaces/tpm/api/2.0/task.logs/%s";

    protected final ObjectMapper jsonMapper;

    public TpmBaseClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
        jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    protected AdministrativeData buildAdministrativeDataObject(JSONObject administrativeDataJsonObject) {
        AdministrativeData administrativeData = new AdministrativeData();
        administrativeData.setCreatedAt(new Date(administrativeDataJsonObject.getLong("createdAt")));
        administrativeData.setModifiedAt(new Date(administrativeDataJsonObject.getLong("modifiedAt")));
        administrativeData.setCreatedBy(administrativeDataJsonObject.optString("createdBy"));
        administrativeData.setModifiedBy(administrativeDataJsonObject.optString("modifiedBy"));
        return administrativeData;
    }

    protected ArtifactProperties parseArtifactProperties(final JSONObject jsonArtifactProperties) {
        ArtifactProperties artifactProperties = new ArtifactProperties();
        artifactProperties.setWebURL(optString(jsonArtifactProperties, "webURL"));
        artifactProperties.setShortName(optString(jsonArtifactProperties, "shortName"));
        artifactProperties.setLogoID(optString(jsonArtifactProperties, "logoID"));
        return artifactProperties;
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
        address.setPOBox(optString(jsonAddress, "POBox"));
        address.setPOBoxPostalCode(optString(jsonAddress, "POBoxPostalCode"));
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
        contactPersonDTO.setNewVersion(jsonContactPerson.optBoolean( "IsNewVersion"));
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

}
