package com.figaf.integration.tpm.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.TpmObjectReference;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;

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

            tpmObject.setId(node.path("id").asText());
            tpmObject.setUniqueId(node.path("uniqueId").asText());
            tpmObject.setDisplayName(node.path("displayName").asText());
            tpmObject.setSemanticVersion(node.path("semanticVersion").asText());
            tpmObject.setArtifactStatus(node.path("artifactStatus").asText());
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

            boolean isCompanyDataNodePresent = !node.path("CompanyData").path("Id").isMissingNode();
            boolean isTradingPartnerDataNodePresent = !node.path("TradingPartnerData").path("Id").isMissingNode();
            boolean isParentIdNodePresent = !node.path("ParentId").isMissingNode();

            if (isCompanyDataNodePresent || isTradingPartnerDataNodePresent || isParentIdNodePresent) {
                TpmObjectReference tpmObjectReference = new TpmObjectReference();
                tpmObject.setTpmObjectReference(tpmObjectReference);
                if (isCompanyDataNodePresent) {
                    tpmObjectReference.setCompanyProfileId(node.path("CompanyData").path("Id").asText());
                }
                if (isTradingPartnerDataNodePresent) {
                    tpmObjectReference.setTradingPartnerId(node.path("TradingPartnerData").path("Id").asText());
                }
                if (isParentIdNodePresent) {
                    tpmObjectReference.setAgreementTemplateId(node.path("ParentId").asText());
                }
            }
            tpmObject.setJsonPayload(node.toString());
            tpmObjects.add(tpmObject);
        }
        return tpmObjects;
    }
}
