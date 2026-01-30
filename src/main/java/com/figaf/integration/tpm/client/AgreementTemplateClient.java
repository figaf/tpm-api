package com.figaf.integration.tpm.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.*;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import com.figaf.integration.tpm.parser.B2BScenarioResponseParser;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.figaf.integration.common.utils.Utils.optString;
import static java.lang.String.format;

@Slf4j
public class AgreementTemplateClient extends TpmBaseClient {

    private static final String AGREEMENT_TEMPLATE_RESOURCE = "/itspaces/tpm/api/2.0/agreementtemplates/%s";
    private static final String AGREEMENT_TEMPLATES_RESOURCE = "/itspaces/tpm/api/2.0/agreementtemplates";
    private static final String AGREEMENT_TEMPLATE_B2B_SCENARIOS_RESOURCE = "/itspaces/tpm/api/2.0/agreementtemplates/%s/b2bscenario/%s";

    public AgreementTemplateClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<AgreementTemplateMetadata> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext={}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            AGREEMENT_TEMPLATES_RESOURCE,
            response -> {
                JSONArray agreementTemplates = new JSONArray(response);
                List<AgreementTemplateMetadata> agreementTemplateMetadataList = new ArrayList<>();
                for (int i = 0; i < agreementTemplates.length(); i++) {
                    JSONObject agreementTemplateJSONObject = agreementTemplates.getJSONObject(i);
                    AgreementTemplateMetadata agreementTemplateMetadata = new AgreementTemplateMetadata();
                    String agreementTemplateId = agreementTemplateJSONObject.getString("id");
                    agreementTemplateMetadata.setObjectId(agreementTemplateId);
                    agreementTemplateMetadata.setTpmObjectType(TpmObjectType.CLOUD_AGREEMENT_TEMPLATE);
                    agreementTemplateMetadata.setDisplayedName(agreementTemplateJSONObject.getString("displayName"));
                    JSONObject administrativeDataJsonObject = agreementTemplateJSONObject.getJSONObject("administrativeData");
                    AdministrativeData administrativeData = buildAdministrativeDataObject(administrativeDataJsonObject);
                    agreementTemplateMetadata.setAdministrativeData(administrativeData);
                    agreementTemplateMetadata.setPayload(agreementTemplateJSONObject.toString());
                    String b2BScenarioDetailsId = optString(agreementTemplateJSONObject, "B2BScenarioDetailsId");
                    agreementTemplateMetadata.setB2bScenarioDetailsId(b2BScenarioDetailsId);

                    AdministrativeData b2bScenarioAdministrativeData = getB2bScenarioDetailsAdministrativeData(requestContext, agreementTemplateId, b2BScenarioDetailsId);
                    agreementTemplateMetadata.setB2bScenarioDetailsAdministrativeData(b2bScenarioAdministrativeData);

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

    public TpmObjectMetadata getSingleMetadata(RequestContext requestContext, String agreementTemplateId) {
        log.debug("#getSingleMetadata: requestContext = {}, agreementTemplateId = {}", requestContext, agreementTemplateId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(AGREEMENT_TEMPLATE_RESOURCE, agreementTemplateId),
            response -> new GenericTpmResponseParser().parseSingleObject(response, TpmObjectType.CLOUD_AGREEMENT_TEMPLATE)
        );
    }

    public List<B2BScenarioInAgreementTemplate> getB2BScenariosForAgreementTemplate(RequestContext requestContext, String agreementTemplateId, String b2BScenarioDetailsId) {
        log.debug("#getB2BScenariosForAgreementTemplate: requestContext = {}, agreementTemplateId = {}, b2BScenarioDetailsId = {}", requestContext, agreementTemplateId, b2BScenarioDetailsId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
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

    public String getB2BScenariosForAgreementTemplateAsRawPayload(RequestContext requestContext, String agreementTemplateId, String b2BScenarioDetailsId) {
        log.debug("#getB2BScenariosForAgreementTemplateAsRawPayload: requestContext = {}, agreementTemplateId = {}, b2BScenarioDetailsId = {}", requestContext, agreementTemplateId, b2BScenarioDetailsId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            String.format(AGREEMENT_TEMPLATE_B2B_SCENARIOS_RESOURCE, agreementTemplateId, b2BScenarioDetailsId)
        );
    }

    public List<TpmObjectReference> getAgreementTemplateIntegrationAdvisoryLinks(RequestContext requestContext, String agreementTemplateId, String b2bScenarioDetailsId) {
        log.debug("#getAgreementTemplateIntegrationAdvisoryLinks: requestContext = {}, agreementTemplateId = {}, b2bScenarioDetailsId = {}", requestContext, agreementTemplateId, b2bScenarioDetailsId);
        try {
            return executeGet(
                requestContext.withPreservingIntegrationSuiteUrl(),
                format(AGREEMENT_TEMPLATE_B2B_SCENARIOS_RESOURCE, agreementTemplateId, b2bScenarioDetailsId),
                (response) -> new B2BScenarioResponseParser().fetchIntegrationAdvisoryLinks(response)
            );
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Can't get B2B Scenarios for Agreement Template {}. This Agreement Template is broken", agreementTemplateId);
            return Collections.emptyList();
        }
    }

    public AdministrativeData getB2bScenarioDetailsAdministrativeData(RequestContext requestContext, String agreementTemplateId, String b2BScenarioDetailsId) {
        log.debug("#getB2bScenarioDetailsAdministrativeData: requestContext = {}, agreementTemplateId = {}, b2BScenarioDetailsId = {}", requestContext, agreementTemplateId, b2BScenarioDetailsId);

        if (StringUtils.isBlank(b2BScenarioDetailsId)) {
            return null;
        }

        try {
            return executeGet(
                requestContext.withPreservingIntegrationSuiteUrl(),
                String.format(AGREEMENT_TEMPLATE_B2B_SCENARIOS_RESOURCE, agreementTemplateId, b2BScenarioDetailsId),
                response -> {
                    JSONObject jsonObject = new JSONObject(response);
                    return buildAdministrativeDataObject(jsonObject.getJSONObject("administrativeData"));
                }
            );
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("B2B Scenario not found for agreementTemplateId={} b2BScenarioDetailsId={}", agreementTemplateId, b2BScenarioDetailsId);
            return null;
        }
    }

}