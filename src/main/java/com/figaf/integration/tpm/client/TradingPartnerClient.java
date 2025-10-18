package com.figaf.integration.tpm.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.CreateBusinessEntityRequest;
import com.figaf.integration.tpm.entity.TpmBusinessEntity;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.TpmObjectDetails;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;

import java.util.*;

import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;

@Slf4j
public class TradingPartnerClient extends BusinessEntityAbstractClient {

    private static final String TRADING_PARTNER_RESOURCE = "/itspaces/tpm/tradingpartners";
    private static final String TRADING_PARTNER_RESOURCE_BY_ID = "/itspaces/tpm/tradingpartners/%s";
    private static final String TRADING_PARTNER_SYSTEMS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/systems";
    private static final String TRADING_PARTNER_IDENTIFIERS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/identifiers";
    private static final String COMMUNICATIONS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/systems/%s/channels";

    private static final String TRADING_PARTNER_CONFIGURATION_RESOURCE = "/itspaces/tpm/tradingpartners/%s::profileConfiguration";
    private static final String TRADING_PARTNER_CONFIG_SIGNVAL_RESOURCE = "/itspaces/tpm/tradingpartners/%s/config.signval";

    public TradingPartnerClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TpmBusinessEntity> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext = {}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            TRADING_PARTNER_RESOURCE,
            response -> {
                JSONArray companiesJsonArray = new JSONArray(response);
                List<TpmBusinessEntity> tradingPartners = new ArrayList<>();
                for (int i = 0; i < companiesJsonArray.length(); i++) {
                    JSONObject companyJsonObject = companiesJsonArray.getJSONObject(i);
                    TpmBusinessEntity tpmBusinessEntity = buildTpmBusinessEntity(companyJsonObject, TpmObjectType.CLOUD_TRADING_PARTNER);
                    tradingPartners.add(tpmBusinessEntity);
                }
                return tradingPartners;
            }
        );
    }

    public TpmObjectDetails getById(RequestContext requestContext, String tradingPartnerId) {
        log.debug("#getById: requestContext = {}, tradingPartnerId = {}", requestContext, tradingPartnerId);

        return executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TRADING_PARTNER_RESOURCE_BY_ID, tradingPartnerId),
            this::buildTpmObjectDetails
        );
    }

    public AggregatedTpmObject getAggregatedTradingPartner(RequestContext requestContext, String tradingPartnerId) {
        log.debug("#getAggregatedTradingPartner: requestContext = {}, tradingPartnerId = {}", requestContext, tradingPartnerId);
        TpmObjectDetails tpmObjectDetails = getById(requestContext, tradingPartnerId);
        if (tpmObjectDetails == null) {
            return null;
        }

        List<System> systems = getPartnerProfileSystems(requestContext, tradingPartnerId);
        List<Identifier> identifiers = getPartnerProfileIdentifiers(requestContext, tradingPartnerId);
        Map<String, List<Channel>> systemIdToChannels = new LinkedHashMap<>();
        for (System system : systems) {
            List<Channel> partnerProfileChannels = getPartnerProfileChannels(requestContext, tradingPartnerId, system.getId());
            systemIdToChannels.put(system.getId(), partnerProfileChannels);
        }

        ProfileConfiguration profileConfiguration = resolveProfileConfiguration(requestContext, tradingPartnerId);

        return new AggregatedTpmObject(tpmObjectDetails, systems, identifiers, systemIdToChannels, profileConfiguration);
    }

    public String getRawById(RequestContext requestContext, String tradingPartnerId) {
        log.debug("#getRawById: requestContext = {}, tradingPartnerId = {}", requestContext, tradingPartnerId);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TRADING_PARTNER_RESOURCE_BY_ID, tradingPartnerId),
            (response) -> response
        );
    }

    public List<System> getPartnerProfileSystems(RequestContext requestContext, String tradingPartnerId) {
        log.debug("#getPartnerProfileSystems: requestContext = {}, tradingPartnerId = {}", requestContext, tradingPartnerId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TRADING_PARTNER_SYSTEMS_RESOURCE, tradingPartnerId),
            this::parseSystemsList
        );
    }

    public List<Identifier> getPartnerProfileIdentifiers(RequestContext requestContext, String tradingPartnerId) {
        log.debug("#getPartnerProfileIdentifiers: requestContext = {}, tradingPartnerId = {}", requestContext, tradingPartnerId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TRADING_PARTNER_IDENTIFIERS_RESOURCE, tradingPartnerId),
            this::parseIdentifiersList
        );
    }

    public List<Channel> getPartnerProfileChannels(RequestContext requestContext, String tradingPartnerId, String systemId) {
        log.debug("#getPartnerProfileChannels: requestContext = {}, tradingPartnerId = {}, systemId = {}", requestContext, tradingPartnerId, systemId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(COMMUNICATIONS_RESOURCE, tradingPartnerId, systemId),
            this::parseChannelsList
        );
    }

    public ProfileConfiguration resolveProfileConfiguration(RequestContext requestContext, String tradingPartnerId) {
        log.debug("#resolveProfileConfiguration: requestContext = {}, tradingPartnerId = {}", requestContext, tradingPartnerId);
        JSONObject partnerProfileConfig = executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TRADING_PARTNER_CONFIGURATION_RESOURCE, tradingPartnerId),
            JSONObject::new
        );
        if (partnerProfileConfig == null) {
            return null;
        }

        JSONArray signatureVerificationConfigurations = executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext,
            format(TRADING_PARTNER_CONFIG_SIGNVAL_RESOURCE, tradingPartnerId),
            JSONArray::new
        );
        if (IterableUtils.isEmpty(signatureVerificationConfigurations)) {
            return null;
        }

        JSONObject signatureValidationConfig = new JSONObject();
        JSONObject configurationEntries = new JSONObject();

        signatureValidationConfig.put("ConfigurationType", "SIGNATURE_VALIDATION_CONFIG");
        signatureValidationConfig.put("ConfigurationEntries", configurationEntries);

        for (int i = 0; i < signatureVerificationConfigurations.length(); i++) {
            JSONObject signatureVerificationConfigurationsJSONObject = signatureVerificationConfigurations.getJSONObject(i);
            configurationEntries.put(signatureVerificationConfigurationsJSONObject.getString("Alias"), signatureVerificationConfigurationsJSONObject);
        }

        partnerProfileConfig.put("SignatureValidationConfigurations", signatureValidationConfig);

        return parseProfileConfiguration(partnerProfileConfig);
    }

    public TpmObjectDetails createTradingPartner(RequestContext requestContext, CreateBusinessEntityRequest createBusinessEntityRequest) {
        log.debug("#createTradingPartner: requestContext = {}, createBusinessEntityRequest = {}", requestContext, createBusinessEntityRequest);

        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            TRADING_PARTNER_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateBusinessEntityRequest> requestEntity = new HttpEntity<>(createBusinessEntityRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create trading partner. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }

                return buildTpmObjectDetails(responseEntity.getBody());
            }
        );
    }

    public System createSystem(RequestContext requestContext, String tradingPartnerId, CreateSystemRequest createSystemRequest) {
        log.debug("#createSystem: requestContext = {}, tradingPartnerId = {}, createSystemRequest = {}", requestContext, tradingPartnerId, createSystemRequest);

        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            format(TRADING_PARTNER_SYSTEMS_RESOURCE, tradingPartnerId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateSystemRequest> requestEntity = new HttpEntity<>(createSystemRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create system. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                try {
                    return jsonMapper.readValue(responseEntity.getBody(), System.class);
                } catch (JsonProcessingException ex) {
                    throw new ClientIntegrationException("Can't parse System creation response: ", ex);
                }
            }
        );
    }

    public void createCommunication(RequestContext requestContext, String tradingPartnerId, String systemId, CreateCommunicationRequest createCommunicationRequest) {
        log.debug("#createCommunication: requestContext = {}, tradingPartnerId = {}, systemId = {}, createCommunicationRequest = {}", requestContext, tradingPartnerId, systemId, createCommunicationRequest);

        executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            format(COMMUNICATIONS_RESOURCE, tradingPartnerId, systemId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateCommunicationRequest> requestEntity = new HttpEntity<>(createCommunicationRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create communication. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

    public void createIdentifier(RequestContext requestContext, String tradingPartnerId, CreateIdentifierRequest createIdentifierRequest) {
        log.debug("#createIdentifier: requestContext = {}, tradingPartnerId = {}, createIdentifierRequest = {}", requestContext, tradingPartnerId, createIdentifierRequest);

        executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            format(TRADING_PARTNER_IDENTIFIERS_RESOURCE, tradingPartnerId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateIdentifierRequest> requestEntity = new HttpEntity<>(createIdentifierRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create identifier. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

    public void createSignatureVerificationConfiguration(RequestContext requestContext, String tradingPartnerId, CreateSignatureVerificationConfigurationRequest createSignatureVerificationConfigurationRequest) {
        log.debug("#createSignatureVerificationConfigurationRequest:  requestContext = {}, tradingPartnerId = {}, createSignatureVerificationConfigurationRequest = {}", requestContext, tradingPartnerId, createSignatureVerificationConfigurationRequest);

        executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            format(TRADING_PARTNER_CONFIG_SIGNVAL_RESOURCE, tradingPartnerId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateSignatureVerificationConfigurationRequest> requestEntity = new HttpEntity<>(createSignatureVerificationConfigurationRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create signature verification configurations. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return null;
            }
        );
    }

}
