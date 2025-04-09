package com.figaf.integration.tpm.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.figaf.integration.tpm.entity.B2BScenarioMetadata;
import com.figaf.integration.tpm.entity.TpmObjectReference;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import com.figaf.integration.tpm.exception.TpmException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;


@Slf4j
public class B2BScenarioResponseParser extends GenericTpmResponseParser {

    public List<B2BScenarioMetadata> parseResponse(String response, String agreementId) throws IOException, TpmException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);

        List<B2BScenarioMetadata> b2BScenarioMetadataList = new ArrayList<>();

        if (rootNode.isArray()) {
            for (JsonNode businessScenarioDetailsNode : rootNode) {
                b2BScenarioMetadataList.addAll(parseBusinessScenarioResponse(businessScenarioDetailsNode, agreementId));
            }
        } else if (rootNode.isObject()) {
            b2BScenarioMetadataList.addAll(parseBusinessScenarioResponse(rootNode, agreementId));
        } else {
            throw new TpmException(format("B2B Scenario response has unexpected format: %s", response));
        }

        return b2BScenarioMetadataList;
    }

    private List<B2BScenarioMetadata> parseBusinessScenarioResponse(JsonNode businessScenarioDetailsNode, String agreementId) throws JsonProcessingException {
        List<B2BScenarioMetadata> b2BScenarioMetadataList = new ArrayList<>();

        XmlMapper xmlMapper = new XmlMapper();
        String b2BScenarioDetailsId = businessScenarioDetailsNode.path("id").asText();
        String semanticVersion = businessScenarioDetailsNode.path("semanticVersion").asText();
        String artifactStatus = businessScenarioDetailsNode.path("artifactStatus").asText();

        JsonNode businessTransactions = businessScenarioDetailsNode.get("BusinessTransactions");
        for (JsonNode businessTransaction : businessTransactions) {
            B2BScenarioMetadata b2bScenarioMetadata = new B2BScenarioMetadata();
            b2bScenarioMetadata.setObjectId(format("%s|%s", b2BScenarioDetailsId, businessTransaction.get("Id").asText()));
            b2bScenarioMetadata.setTpmObjectType(TpmObjectType.CLOUD_B2B_SCENARIO);
            b2bScenarioMetadata.setVersion(semanticVersion);
            b2bScenarioMetadata.setStatus(artifactStatus);
            b2bScenarioMetadata.setAgreementId(agreementId);

            initDisplayedName(b2bScenarioMetadata, businessTransaction);
            initAdministrativeData(b2bScenarioMetadata, businessScenarioDetailsNode);

            List<TpmObjectReference> tpmObjectReferences = new ArrayList<>();
            collectPartnerDirectoryReferences(tpmObjectReferences, businessTransaction);
            collectMigReferencesAndInitCustomMappingIflowUrl(b2bScenarioMetadata, tpmObjectReferences, businessTransaction);
            b2bScenarioMetadata.setTpmObjectReferences(tpmObjectReferences);

            b2bScenarioMetadata.setPayload(xmlMapper.writeValueAsString(businessTransaction));
            b2BScenarioMetadataList.add(b2bScenarioMetadata);
        }

        return b2BScenarioMetadataList;
    }

    private void initDisplayedName(B2BScenarioMetadata b2bScenarioMetadata, JsonNode businessTransaction) {
        JsonNode transactionProperties = businessTransaction.get("TransactionProperties");
        JsonNode properties = transactionProperties.get("Properties");
        b2bScenarioMetadata.setDisplayedName(properties.get("Name").asText());
    }

    private void collectPartnerDirectoryReferences(List<TpmObjectReference> tpmObjectReferences, JsonNode businessTransaction) {
        JsonNode businessTransactionDetails = businessTransaction.path("BusinessTransactionDetails");
        if (businessTransactionDetails.isMissingNode()) {
            return;
        }

        JsonNode computedPids = businessTransactionDetails.path("ComputedPids");
        if (computedPids.isMissingNode()) {
            return;
        }

        for (JsonNode computedPid : computedPids) {
            String partnerDirectoryId = computedPid.asText();
            TpmObjectReference tpmObjectReference = new TpmObjectReference();
            tpmObjectReference.setObjectId(partnerDirectoryId);
            tpmObjectReference.setTpmObjectType(TpmObjectType.PD_PARTNER);
            tpmObjectReferences.add(tpmObjectReference);
        }
    }


    private void collectMigReferencesAndInitCustomMappingIflowUrl(B2BScenarioMetadata b2bScenarioMetadata, List<TpmObjectReference> tpmObjectReferences, JsonNode businessTransaction) {
        JsonNode businessTransactionActivities = businessTransaction.path("BusinessTransactionActivities");
        if (businessTransactionActivities.isMissingNode()) {
            return;
        }

        for (JsonNode businessTransactionActivity : businessTransactionActivities) {
            JsonNode choreographyProperties = businessTransactionActivity.path("ChoreographyProperties");
            if (choreographyProperties.isMissingNode()) {
                continue;
            }
            JsonNode propertiesNode = choreographyProperties.path("Properties");
            if (propertiesNode.isMissingNode()) {
                continue;
            }

            for (Map.Entry<String, JsonNode> property : propertiesNode.properties()) {
                String propertyKey = property.getKey();
                JsonNode propertiesInnerNode = property.getValue().get("Properties");
                if (propertyKey.equals("MAPPING")) {
                    String customMappingUrl = propertiesInnerNode.path("CUSTOM_MAPPING").asText();
                    b2bScenarioMetadata.setCustomMappingIFlowUrl(customMappingUrl);
                }

                String migGuid = propertiesInnerNode.path("MIGGUID").asText();
                String migVersionId = propertiesInnerNode.path("MIGVersionId").asText();
                String objectGuid = propertiesInnerNode.path("ObjectGUID").asText();
                if (StringUtils.isEmpty(migGuid) || StringUtils.isEmpty(migVersionId) || StringUtils.isEmpty(objectGuid)) {
                    continue;
                }

                TpmObjectReference tpmObjectReference = createTpmObjectReference(migGuid, migVersionId, objectGuid, TpmObjectType.CLOUD_MIG);
                tpmObjectReferences.add(tpmObjectReference);
            }
        }
    }

}
