package com.figaf.integration.tpm.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.TpmObjectReference;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GenericTpmResponseParser {

    private final ObjectMapper objectMapper;

    public GenericTpmResponseParser() {
        this.objectMapper = new ObjectMapper();

    }

    public List<TpmObjectMetadata> parseResponse(String response, TpmObjectType tpmObjectType) throws IOException {
        JsonNode rootNode = objectMapper.readTree(response);
        List<TpmObjectMetadata> tpmObjects = new ArrayList<>();

        for (JsonNode node : rootNode) {
            TpmObjectMetadata tpmObject = new TpmObjectMetadata();

            tpmObject.setObjectId(node.path("id").asText());
            tpmObject.setVersionId(node.path("versionId").asText());
            tpmObject.setDisplayedName(node.path("displayName").asText());
            tpmObject.setVersion(node.path("semanticVersion").asText());
            tpmObject.setStatus(node.path("artifactStatus").asText());
            tpmObject.setTpmObjectType(tpmObjectType);

            // Parse AdministrativeData
            JsonNode administrativeDataNode = node.path("administrativeData");
            if (!administrativeDataNode.isMissingNode()) {
                AdministrativeData administrativeData = new AdministrativeData();
                administrativeData.setCreatedAt(new Date(administrativeDataNode.path("createdAt").asLong()));
                administrativeData.setCreatedBy(administrativeDataNode.path("createdBy").asText());
                administrativeData.setModifiedAt(new Date(administrativeDataNode.path("modifiedAt").asLong()));
                administrativeData.setModifiedBy(administrativeDataNode.path("modifiedBy").asText());
                tpmObject.setAdministrativeData(administrativeData);
            }
            setTpmObjectReferences(
                node,
                tpmObject
            );

            tpmObject.setJsonPayload(node.toString());
            tpmObjects.add(tpmObject);
        }
        return tpmObjects;
    }

    private void setTpmObjectReferences(JsonNode node, TpmObjectMetadata tpmObjectMetadata) {
        List<TpmObjectReference> tpmObjectReferences = new ArrayList<>();
        boolean isCompanyDataNodePresent = !node.path("CompanyData").path("Id").isMissingNode();
        if (isCompanyDataNodePresent) {
            tpmObjectReferences.add(createTpmObjectReference(node.path("CompanyData").path("Id").asText(), TpmObjectType.CLOUD_COMPANY_PROFILE));
        }
        boolean isTradingPartnerDataNodePresent = !node.path("TradingPartnerData").path("Id").isMissingNode();

        if (isTradingPartnerDataNodePresent) {
            tpmObjectReferences.add(createTpmObjectReference(node.path("TradingPartnerData").path("Id").asText(), TpmObjectType.CLOUD_TRADING_PARTNER));
        }
        boolean isParentIdNodePresent = !node.path("ParentId").isMissingNode();
        if (isParentIdNodePresent) {
            tpmObjectReferences.add(createTpmObjectReference(node.path("ParentId").asText(), TpmObjectType.CLOUD_AGREEMENT_TEMPLATE));
        }
        collectMigReferences(node, tpmObjectReferences);
        tpmObjectMetadata.setTpmObjectReferences(tpmObjectReferences);
    }

    private void collectMigReferences(JsonNode rootNode, List<TpmObjectReference> tpmObjectReferences) {
        JsonNode businessTransactionsNode = rootNode.path("BusinessTransactions");
        if (businessTransactionsNode.isMissingNode() || !businessTransactionsNode.isArray()) {
            return;
        }

        for (JsonNode businessTransaction : businessTransactionsNode) {
            collectMigsFromActivities(businessTransaction, tpmObjectReferences);
        }
    }

    private void collectMigsFromActivities(JsonNode businessTransaction, List<TpmObjectReference> tpmObjectReferences) {
        JsonNode activitiesNode = businessTransaction.path("BusinessTransactionActivities");
        for (JsonNode activity : activitiesNode) {
            collectMigsFromChoreography(activity, tpmObjectReferences);
        }
    }

    private void collectMigsFromChoreography(JsonNode activity, List<TpmObjectReference> tpmObjectReferences) {
        JsonNode choreographyPropertiesNode = activity.path("ChoreographyProperties");
        for (JsonNode choreographyProperty : choreographyPropertiesNode) {
            collectMigsFromProperties(choreographyProperty, tpmObjectReferences);
        }
    }

    private void collectMigsFromProperties(JsonNode choreographyProperty, List<TpmObjectReference> tpmObjectReferences) {
        JsonNode propertiesNode = choreographyProperty.path("Properties");
        for (JsonNode property : propertiesNode) {
            String key = property.path("key").asText();
            String value = property.path("value").asText();
            if ("MIGGUID".equals(key) && !value.isEmpty()) {
                tpmObjectReferences.add(createTpmObjectReference(value, TpmObjectType.CLOUD_MIG));
            }
        }
    }

    private TpmObjectReference createTpmObjectReference(String id, TpmObjectType tpmObjectType) {
        TpmObjectReference tpmObjectReference = new TpmObjectReference();
        tpmObjectReference.setObjectId(id);
        tpmObjectReference.setTpmObjectType(tpmObjectType);
        return tpmObjectReference;
    }
}
