package com.figaf.integration.tpm.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.common.utils.Utils;
import com.figaf.integration.tpm.entity.*;
import com.figaf.integration.tpm.entity.integrationadvisory.IntegrationAdvisoryObject;
import com.figaf.integration.tpm.entity.integrationadvisory.MagVersion;
import com.figaf.integration.tpm.entity.integrationadvisory.external_api.Mag;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.figaf.integration.tpm.enumtypes.TpmObjectType.CLOUD_MAG;
import static com.figaf.integration.tpm.enumtypes.TpmObjectType.CLOUD_MIG;

@Slf4j
public class MagClient extends TpmBaseClient {

    private static final String MAG_RESOURCE = "/api/1.0/mags";
    private static final String MAG_VERSIONS_RESOURCE = "/api/1.0/mags/%s/magVersions";
    private static final String MAG_VERSION_RESOURCE = "/api/1.0/mags/%s";
    private static final String MAG_EXTERNAL_API_RESOURCE = "/externalApi/1.0/mags";
    private static final String MAG_VERSIONS_EXTERNAL_API_RESOURCE = "/externalApi/1.0/mags/%s";

    public MagClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<IntegrationAdvisoryObject> getAllLatestMetadata(RequestContext requestContext) {
        log.debug("#getAllLatestMetadata: requestContext = {}", requestContext);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            MAG_RESOURCE,
           response -> {
               List<IntegrationAdvisoryObject> mags = new ArrayList<>();
               JSONArray jsonArray = new JSONArray(response);
               for (int i = 0; i < jsonArray.length(); i++) {
                   JSONObject jsonObject = jsonArray.getJSONObject(i);
                   IntegrationAdvisoryObject mag = new IntegrationAdvisoryObject();
                   mag.setObjectId(jsonObject.getString("MAGGUID"));
                   mag.setTpmObjectType(CLOUD_MAG);
                   mag.setVersionId(jsonObject.getString("ObjectGUID"));
                   mag.setDisplayedName(jsonObject.getString("Name"));
                   mag.setVersion(jsonObject.getString("Version"));
                   mag.setStatus(jsonObject.getString("Status"));
                   mag.setImportCorrelationGroupId(jsonObject.getString("ImportCorrelationGroupId"));
                   mag.setImportCorrelationObjectId(jsonObject.getString("ImportCorrelationObjectId"));

                   AdministrativeData adminData = new AdministrativeData();
                   adminData.setCreatedAt(new Date(jsonObject.getLong("CreationDate")));
                   adminData.setModifiedAt(new Date(jsonObject.getLong("ModifiedDate")));
                   adminData.setCreatedBy(jsonObject.getString("CreatedBy"));
                   adminData.setModifiedBy(jsonObject.getString("ModifiedBy"));
                   mag.setAdministrativeData(adminData);

                   List<TpmObjectReference> tpmObjectReferences = new ArrayList<>();
                   String sourceMigGUID = Utils.optString(jsonObject, "SourceMigGUID");
                   if (sourceMigGUID != null) {
                       TpmObjectReference tpmObjectReference = new TpmObjectReference();
                       tpmObjectReference.setTpmObjectType(CLOUD_MIG);
                       tpmObjectReference.setObjectVersionId(sourceMigGUID);
                       tpmObjectReferences.add(tpmObjectReference);
                   }

                   String targetMigGUID = Utils.optString(jsonObject, "TargetMigGUID");
                   if (targetMigGUID != null) {
                       TpmObjectReference tpmObjectReference = new TpmObjectReference();
                       tpmObjectReference.setTpmObjectType(CLOUD_MIG);
                       tpmObjectReference.setObjectVersionId(targetMigGUID);
                       tpmObjectReferences.add(tpmObjectReference);
                   }
                   mag.setTpmObjectReferences(tpmObjectReferences);

                   mags.add(mag);

               }

               return mags;
           }
        );
    }

    public List<MagVersion> getMagVersions(RequestContext requestContext, String magId) {
        log.debug("#getMagVersions: requestContext = {}, magId = {}", requestContext, magId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            String.format(MAG_VERSIONS_RESOURCE, magId),
            response -> jsonMapper.readValue(response, new TypeReference<>() {
            })
        );
    }

    public String getRawById(RequestContext requestContext, String magVersionId) {
        log.debug("#getRawById: requestContext = {}, magVersionId = {}", requestContext, magVersionId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            String.format(MAG_VERSION_RESOURCE, magVersionId),
            response -> response
        );
    }

    public String getMagVersionInfoById(RequestContext requestContext, String magVersionId) {
        log.debug("#getMagVersionInfoById: requestContext = {}, magVersionId = {}", requestContext, magVersionId);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            String.format(MAG_VERSIONS_EXTERNAL_API_RESOURCE, magVersionId)
        );
    }

    public List<Mag> getAllMagsExternalApi(RequestContext requestContext) {
        log.debug("#getAllMagsExternalApi: requestContext = {}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            MAG_EXTERNAL_API_RESOURCE,
            response -> {
                JSONObject jsonObjectResponse = new JSONObject(response);
                JSONArray magsJsonArray = jsonObjectResponse.getJSONArray("value");
                List<Mag> mags = new ArrayList<>();
                for (int i = 0; i < magsJsonArray.length(); i++) {
                    JSONObject magJsonObject = magsJsonArray.getJSONObject(i);
                    Mag mag = new Mag();
                    mag.setMagGuid(magJsonObject.getString("MAGGUID"));
                    mag.setMagVersions(magJsonObject.getJSONArray("MAGVersions"));
                    mags.add(mag);
                }

                return mags;
            }
        );
    }

}
