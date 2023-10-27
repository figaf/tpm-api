package com.figaf.integration.tpm.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.BaseTpmObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class GenericTpmResponseParser<T extends BaseTpmObject> {

    private final ObjectMapper objectMapper;
    private final Supplier<T> tpmObjectCreationMethod;

    public GenericTpmResponseParser(Supplier<T> tpmObjectCreationMethod) {
        this.objectMapper = new ObjectMapper();
        this.tpmObjectCreationMethod = tpmObjectCreationMethod;
    }

    public List<T> parseResponse(String response) throws IOException {
        JsonNode rootNode = objectMapper.readTree(response);
        List<T> tpmObjects = new ArrayList<>();

        for (JsonNode node : rootNode) {
            T tpmObject = tpmObjectCreationMethod.get();

            tpmObject.setId(node.path("id").asText());
            tpmObject.setUniqueId(node.path("uniqueId").asText());
            tpmObject.setDisplayName(node.path("displayName").asText());
            tpmObject.setSemanticVersion(node.path("semanticVersion").asText());
            tpmObject.setArtifactStatus(node.path("artifactStatus").asText());

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
            tpmObject.setJsonPayload(node.toString());
            tpmObjects.add(tpmObject);
        }
        return tpmObjects;
    }
}
