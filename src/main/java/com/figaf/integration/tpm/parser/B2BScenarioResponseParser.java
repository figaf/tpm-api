package com.figaf.integration.tpm.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.figaf.integration.tpm.entity.B2BScenarioMetadata;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
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

    public List<B2BScenarioMetadata> parseResponse(String response, TpmObjectMetadata agreementMetadata) throws IOException, TpmException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);

        List<B2BScenarioMetadata> b2BScenarioMetadataList = new ArrayList<>();

        if (rootNode.isArray()) {
            for (JsonNode businessScenarioDetailsNode : rootNode) {
                b2BScenarioMetadataList.addAll(parseBusinessScenarioResponse(businessScenarioDetailsNode, agreementMetadata));
            }
        } else if (rootNode.isObject()) {
            b2BScenarioMetadataList.addAll(parseBusinessScenarioResponse(rootNode, agreementMetadata));
        } else {
            throw new TpmException(format("B2B Scenario response has unexpected format: %s", response));
        }

        return b2BScenarioMetadataList;
    }

    private List<B2BScenarioMetadata> parseBusinessScenarioResponse(JsonNode businessScenarioDetailsNode, TpmObjectMetadata agreementMetadata) throws JsonProcessingException {
        List<B2BScenarioMetadata> b2BScenarioMetadataList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode agreementMetadataRootNode = objectMapper.readTree(agreementMetadata.getPayload());

        XmlMapper xmlMapper = new XmlMapper();
        String b2BScenarioDetailsId = businessScenarioDetailsNode.path("id").asText();
        String semanticVersion = businessScenarioDetailsNode.path("semanticVersion").asText();
        String artifactStatus = businessScenarioDetailsNode.path("artifactStatus").asText();

        JsonNode businessTransactions = businessScenarioDetailsNode.get("BusinessTransactions");
        for (JsonNode businessTransaction : businessTransactions) {
            String businessTransactionId = businessTransaction.get("Id").asText();
            B2BScenarioMetadata b2bScenarioMetadata = new B2BScenarioMetadata();
            b2bScenarioMetadata.setObjectId(format("%s|%s", b2BScenarioDetailsId, businessTransactionId));
            b2bScenarioMetadata.setTpmObjectType(TpmObjectType.CLOUD_B2B_SCENARIO);
            b2bScenarioMetadata.setVersion(semanticVersion);
            b2bScenarioMetadata.setStatus(artifactStatus);
            b2bScenarioMetadata.setAgreementId(agreementMetadata.getObjectId());

            initDisplayedName(b2bScenarioMetadata, businessTransaction);
            initAdministrativeData(b2bScenarioMetadata, businessScenarioDetailsNode);

            List<TpmObjectReference> tpmObjectReferences = new ArrayList<>();
            collectPartnerDirectoryReferences(tpmObjectReferences, agreementMetadataRootNode, businessTransactionId);
            determineActivatedStatus(b2bScenarioMetadata, agreementMetadataRootNode, businessTransactionId);
            collectMigReferencesAndHandleChoreographyProperties(b2bScenarioMetadata, tpmObjectReferences, businessTransaction, agreementMetadataRootNode);
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

    private void collectPartnerDirectoryReferences(List<TpmObjectReference> tpmObjectReferences, JsonNode agreementMetadataRootNode, String businessTransactionId) {
        JsonNode concreteTransactionDetails = agreementMetadataRootNode.path("ConcreteTransactionDetails");
        if (concreteTransactionDetails.isMissingNode()) {
            return;
        }
        JsonNode concreteBusinessTransaction = concreteTransactionDetails.path(businessTransactionId);
        if (concreteBusinessTransaction.isMissingNode()) {
            return;
        }

        JsonNode computedPids = concreteBusinessTransaction.path("ComputedPids");
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

    private void determineActivatedStatus(B2BScenarioMetadata b2BScenarioMetadata, JsonNode agreementMetadataRootNode, String businessTransactionId) {
        JsonNode concreteTransactionDetails = agreementMetadataRootNode.path("ConcreteTransactionDetails");
        if (concreteTransactionDetails.isMissingNode()) {
            return;
        }
        JsonNode concreteBusinessTransaction = concreteTransactionDetails.path(businessTransactionId);
        if (concreteBusinessTransaction.isMissingNode()) {
            return;
        }

        JsonNode businessTransactionLifeCycleStatus = concreteBusinessTransaction.path("BusinessTransactionLifeCycleStatus");
        if (businessTransactionLifeCycleStatus.isMissingNode()) {
            return;
        }

        b2BScenarioMetadata.setActivated(businessTransactionLifeCycleStatus.get("Activated").asBoolean());
    }


    private void collectMigReferencesAndHandleChoreographyProperties(B2BScenarioMetadata b2bScenarioMetadata, List<TpmObjectReference> tpmObjectReferences, JsonNode businessTransaction, JsonNode agreementMetadataRootNode) {
        JsonNode businessTransactionActivities = businessTransaction.path("BusinessTransactionActivities");
        if (businessTransactionActivities.isMissingNode()) {
            return;
        }

        String senderSystemId = null;
        String receiverSystemId = null;

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
                switch (propertyKey) {
                    case "MAPPING" ->
                        b2bScenarioMetadata.setCustomMappingIFlowUrl(propertiesInnerNode.path("CUSTOM_MAPPING").asText());
                    case "SENDER_INTERCHANGE" ->
                        b2bScenarioMetadata.setPreIFlowUrl(propertiesInnerNode.path("CUSTOM_PRE_PROC").asText());
                    case "RECEIVER_INTERCHANGE" ->
                        b2bScenarioMetadata.setPostIFlowUrl(propertiesInnerNode.path("CUSTOM_POST_PROC").asText());
                    case "SENDER_SYSTEM" -> {
                        senderSystemId = propertiesInnerNode.path("Id").asText();
                        b2bScenarioMetadata.setInitiator(format("%s|%s", propertiesInnerNode.path("Label_Name").asText(), propertiesInnerNode.path("Label_SystemInstanceName").asText()));
                    }
                    case "RECEIVER_SYSTEM" -> {
                        receiverSystemId = propertiesInnerNode.path("Id").asText();
                        b2bScenarioMetadata.setReactor(format("%s|%s", propertiesInnerNode.path("Label_Name").asText(), propertiesInnerNode.path("Label_SystemInstanceName").asText()));
                    }
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

        defineDirection(b2bScenarioMetadata, agreementMetadataRootNode, senderSystemId, receiverSystemId);

    }

    private void defineDirection(B2BScenarioMetadata b2bScenarioMetadata, JsonNode agreementMetadataRootNode, String senderSystemId, String receiverSystemId) {
        JsonNode tradingPartnerDetails = agreementMetadataRootNode.path("TradingPartnerDetails");
        if (tradingPartnerDetails.isMissingNode()) {
            return;
        }

        JsonNode idForTradingPartner = tradingPartnerDetails.path("IdForTradingPartner");
        if (idForTradingPartner.isMissingNode()) {
            return;
        }

        JsonNode properties = idForTradingPartner.path("Properties");
        if (properties.isMissingNode()) {
            return;
        }

        String tradingPartnerId = properties.path("Id").asText();
        if (tradingPartnerId.equals(senderSystemId)) {
            b2bScenarioMetadata.setDirection(B2BScenarioMetadata.Direction.INBOUND);
            return;
        }

        if (tradingPartnerId.equals(receiverSystemId)) {
            b2bScenarioMetadata.setDirection(B2BScenarioMetadata.Direction.OUTBOUND);
            return;
        }
    }

}
