package com.figaf.integration.tpm.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.AdministrativeData;
import org.json.JSONObject;

import java.util.Date;

/**
 * @author Kostas Charalambous
 */
public abstract class TpmBaseClient extends BaseClient {

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

}
