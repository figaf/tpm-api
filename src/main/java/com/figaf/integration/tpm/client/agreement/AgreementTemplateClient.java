package com.figaf.integration.tpm.client.agreement;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.*;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.figaf.integration.common.utils.Utils.optString;

@Slf4j
public class AgreementTemplateClient extends TpmBaseClient {

    public AgreementTemplateClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<AgreementTemplateMetadata> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            AGREEMENT_TEMPLATE_RESOURCE,
            response -> {
                JSONArray agreementTemplates = new JSONArray(response);
                List<AgreementTemplateMetadata> agreementTemplateMetadataList = new ArrayList<>();
                for (int i = 0; i < agreementTemplates.length(); i++) {
                    JSONObject agreementTemplateJSONObject = agreementTemplates.getJSONObject(i);
                    AgreementTemplateMetadata agreementTemplateMetadata = new AgreementTemplateMetadata();
                    agreementTemplateMetadata.setObjectId(agreementTemplateJSONObject.getString("id"));
                    agreementTemplateMetadata.setTpmObjectType(TpmObjectType.CLOUD_AGREEMENT_TEMPLATE);
                    agreementTemplateMetadata.setDisplayedName(agreementTemplateJSONObject.getString("displayName"));
                    JSONObject administrativeDataJsonObject = agreementTemplateJSONObject.getJSONObject("administrativeData");
                    AdministrativeData administrativeData = buildAdministrativeDataObject(administrativeDataJsonObject);
                    agreementTemplateMetadata.setAdministrativeData(administrativeData);
                    agreementTemplateMetadata.setPayload(agreementTemplateJSONObject.toString());
                    agreementTemplateMetadata.setB2bScenarioDetailsId(optString(agreementTemplateJSONObject, "B2BScenarioDetailsId"));

                    JSONObject companyDataJsonObject = agreementTemplateJSONObject.getJSONObject("CompanyData");
                    TpmObjectReference tpmObjectReference = new TpmObjectReference();
                    tpmObjectReference.setObjectId(companyDataJsonObject.getString("Id"));
                    String selectedProfileType = optString(companyDataJsonObject, "SelectedProfileType");
                    if ("SUBSIDIARY".equals(selectedProfileType)) {
                        tpmObjectReference.setTpmObjectType(TpmObjectType.CLOUD_SUBSIDIARY);
                    } else {
                        tpmObjectReference.setTpmObjectType(TpmObjectType.CLOUD_COMPANY_PROFILE);
                    }
                    agreementTemplateMetadata.setTpmObjectReferences(Collections.singletonList(tpmObjectReference));

                    agreementTemplateMetadataList.add(agreementTemplateMetadata);
                }

                return agreementTemplateMetadataList;
            }

        );
    }

    public List<B2BScenarioInAgreementTemplate> getB2BScenariosForAgreementTemplate(String agreementTemplateId, String b2BScenarioDetailsId, RequestContext requestContext) {
        log.debug("#getB2BScenariosForAgreementTemplate: agreementTemplateId = {}, b2BScenarioDetailsId = {}, requestContext = {}", agreementTemplateId, b2BScenarioDetailsId, requestContext);
        return executeGet(
            requestContext,
            String.format(AGREEMENT_TEMPLATE_B2B_SCENARIOS_RESOURCE, agreementTemplateId, b2BScenarioDetailsId),
            response -> {
                List<B2BScenarioInAgreementTemplate> b2BScenarioInAgreementTemplates = new ArrayList<>();

                JSONObject jsonObject = new JSONObject(response);
                JSONArray businessTransactions = jsonObject.getJSONArray("BusinessTransactions");
                for (int i = 0; i < businessTransactions.length(); i++) {
                    JSONObject businessTransaction = businessTransactions.getJSONObject(i);

                    B2BScenarioInAgreementTemplate b2BScenarioInAgreementTemplate = new B2BScenarioInAgreementTemplate();
                    b2BScenarioInAgreementTemplate.setAgreementTemplateId(agreementTemplateId);

                    b2BScenarioInAgreementTemplate.setObjectId(businessTransaction.getString("Id"));

                    JSONObject businessTransactionProperties = businessTransaction.getJSONObject("TransactionProperties").getJSONObject("Properties");
                    b2BScenarioInAgreementTemplate.setName(businessTransactionProperties.getString("Name"));
                    String initiator = businessTransactionProperties.optString("Initiator");
                    b2BScenarioInAgreementTemplate.setDirection(StringUtils.isNotEmpty(initiator) ? B2BScenarioInAgreementTemplate.Direction.OUTBOUND : B2BScenarioInAgreementTemplate.Direction.INBOUND);

                    b2BScenarioInAgreementTemplates.add(b2BScenarioInAgreementTemplate);
                }
                return b2BScenarioInAgreementTemplates;
            }
        );
    }

}
