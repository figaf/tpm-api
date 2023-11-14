package com.figaf.integration.tpm.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import com.figaf.integration.tpm.exception.TpmException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class MigResponseParser {

    private static final String MIG_DOES_NOT_CONTAIN_VALID_STRUCTURE = "Mig response doesnt contain a valid structure";
    private static final String MIGS = "Migs";

    public List<TpmObjectMetadata> parseJsonToTpmObjectMetadata(String json) throws IOException, TpmException {
        log.debug("#parseJsonToTpmObjectMetadata: json={}", json);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(json);
        if (!rootNode.isArray()) {
            throw new TpmException(MIG_DOES_NOT_CONTAIN_VALID_STRUCTURE);
        }
        List<TpmObjectMetadata> tpmObjects = new ArrayList<>();

        for (JsonNode rootElement : rootNode) {
            JsonNode migs = rootElement.get(MIGS);
            for (JsonNode node : migs) {
                TpmObjectMetadata metadata = new TpmObjectMetadata();

                metadata.setTpmObjectType(TpmObjectType.CLOUD_MIG);
                metadata.setObjectId(node.path("MIGGUID").asText());
                metadata.setVersionId(node.path("ObjectGUID").asText());
                metadata.setDisplayedName(node.path("Documentation").path("Name").path("ArtifactValue").path("Id").asText());
                metadata.setStatus(node.path("Status").asText());
                metadata.setVersion(node.path("MIGVersionId").asText());

                AdministrativeData adminData = new AdministrativeData();
                adminData.setCreatedAt(new Date(node.path("CreationDate").asLong()));
                adminData.setModifiedAt(new Date(node.path("ModifiedDate").asLong()));
                adminData.setCreatedBy(node.path("CreatedBy").asText());
                adminData.setModifiedBy(node.path("ModifiedBy").asText());
                metadata.setAdministrativeData(adminData);

                metadata.setJsonPayload(rootElement.toString());
                tpmObjects.add(metadata);
            }
        }
        return tpmObjects;
    }
}
