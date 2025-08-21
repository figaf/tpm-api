package com.figaf.integration.tpm.client.company;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.*;
import com.figaf.integration.tpm.entity.trading.Channel;
import com.figaf.integration.tpm.entity.trading.Identifier;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.extern.slf4j.Slf4j;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.figaf.integration.common.utils.Utils.optString;
import static java.lang.String.format;

@Slf4j
public class CompanyProfileClient extends TpmBaseClient {

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

    public List<System> getCompanySystems(RequestContext requestContext, String companyId) {
        log.debug("#getCompanySystems: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext,
            format(COMPANY_SYSTEMS_RESOURCE, companyId),
            response -> {
                System[] systems = jsonMapper.readValue(response, System[].class);
                return Arrays.asList(systems);
            }
        );
    }

    public List<System> getSubsidiarySystems(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getSubsidiarySystems: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}", requestContext, parentCompanyId, subsidiaryId);
        return executeGet(
            requestContext,
            format(SUBSIDIARY_SYSTEMS_RESOURCE, parentCompanyId, subsidiaryId),
            response -> {
                System[] systems = jsonMapper.readValue(response, System[].class);
                return Arrays.asList(systems);
            }
        );
    }

    public List<Identifier> getCompanyIdentifiers(RequestContext requestContext, String companyId) {
        log.debug("#getCompanyIdentifiers: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext,
            format(COMPANY_IDENTIFIERS_RESOURCE, companyId),
            response -> {
                Identifier[] identifiers = jsonMapper.readValue(response, Identifier[].class);
                return Arrays.asList(identifiers);
            }
        );
    }

    public List<Identifier> getSubsidiaryIdentifiers(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getSubsidiaryIdentifiers: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}", requestContext, parentCompanyId, subsidiaryId);
        return executeGet(
            requestContext,
            format(SUBSIDIARY_IDENTIFIERS_RESOURCE, parentCompanyId, subsidiaryId),
            response -> {
                Identifier[] identifiers = jsonMapper.readValue(response, Identifier[].class);
                return Arrays.asList(identifiers);
            }
        );
    }

    public List<Channel> getCompanyChannels(RequestContext requestContext, String companyId, String systemId) {
        log.debug("#getCompanyChannels: requestContext = {}, companyId = {}, systemId = {}", requestContext, companyId, systemId);
        return executeGet(
            requestContext,
            format(COMPANY_CHANNELS_RESOURCE, companyId, systemId),
            response -> {
                Channel[] channels = jsonMapper.readValue(response, Channel[].class);
                return Arrays.asList(channels);
            }
        );
    }

    public List<Channel> getSubsidiaryChannels(RequestContext requestContext, String parentCompanyId, String subsidiaryId, String systemId) {
        log.debug("#getSubsidiaryChannels: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}, systemId = {}", requestContext, parentCompanyId, subsidiaryId, systemId);
        return executeGet(
            requestContext,
            format(SUBSIDIARY_CHANNELS_RESOURCE, parentCompanyId, subsidiaryId, systemId),
            response -> {
                Channel[] channels = jsonMapper.readValue(response, Channel[].class);
                return Arrays.asList(channels);
            }
        );
    }

}
