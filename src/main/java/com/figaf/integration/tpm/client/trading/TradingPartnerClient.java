package com.figaf.integration.tpm.client.trading;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClientForTradingPartnerOrCompanyOrSubsidiary;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.TpmObjectDetails;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;

import java.util.*;

import static com.figaf.integration.common.utils.Utils.optString;
import static com.figaf.integration.common.utils.Utils.parseDate;
import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;

@Slf4j
public class TradingPartnerClient extends TpmBaseClientForTradingPartnerOrCompanyOrSubsidiary {

    public TradingPartnerClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TpmObjectMetadata> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext={}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            TRADING_PARTNER_RESOURCE,
            (response) -> new GenericTpmResponseParser().parseResponse(response, TpmObjectType.CLOUD_TRADING_PARTNER)
        );
    }

    public TpmObjectDetails getById(String tradingPartnerId, RequestContext requestContext) {
        log.debug("#getById: requestContext={}, tradingPartnerId={}", requestContext, tradingPartnerId);

        return executeGetAndReturnNullIfNotFoundErrorOccurs(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TRADING_PARTNER_RESOURCE_BY_ID, tradingPartnerId),
            this::buildTpmObjectDetails
        );
    }

    public AggregatedTpmObject getAggregatedTradingPartner(String tradingPartnerId, RequestContext requestContext) {
        log.debug("#getAggregatedTradingPartner: requestContext={}, tradingPartnerId={}", requestContext, tradingPartnerId);
        TpmObjectDetails tpmObjectDetails = getById(tradingPartnerId, requestContext);
        if (tpmObjectDetails == null) {
            return null;
        }

        List<System> systems = getPartnerProfileSystems(tradingPartnerId, requestContext);
        List<Identifier> identifiers = getPartnerProfileIdentifiers(tradingPartnerId, requestContext);
        Map<String, List<Channel>> systemIdToChannels = new LinkedHashMap<>();
        for (System system : systems) {
            List<Channel> partnerProfileChannels = getPartnerProfileChannels(tradingPartnerId, system.getId(), requestContext);
            systemIdToChannels.put(system.getId(), partnerProfileChannels);
        }

        ProfileConfiguration profileConfiguration = resolveProfileConfiguration(tradingPartnerId, requestContext);

        return new AggregatedTpmObject(tpmObjectDetails, systems, identifiers, systemIdToChannels, profileConfiguration);
    }

    public String getRawById(String tradingPartnerId, RequestContext requestContext) {
        log.debug("#getRawById: tradingPartnerId={}, requestContext={}", tradingPartnerId, requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TRADING_PARTNER_RESOURCE_BY_ID, tradingPartnerId),
            (response) -> response
        );
    }

    public List<System> getPartnerProfileSystems(String tradingPartnerId, RequestContext requestContext) {
        log.debug("#getPartnerProfileSystems: tradingPartnerId = {}, requestContext = {}", tradingPartnerId, requestContext);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TRADING_PARTNER_SYSTEMS_RESOURCE, tradingPartnerId),
            this::parseSystemsList
        );
    }

    public List<Identifier> getPartnerProfileIdentifiers(String tradingPartnerId, RequestContext requestContext) {
        log.debug("#getPartnerProfileIdentifiers: tradingPartnerId = {}, requestContext = {}", tradingPartnerId, requestContext);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TRADING_PARTNER_IDENTIFIERS_RESOURCE, tradingPartnerId),
            this::parseIdentifiersList
        );
    }

    public List<Channel> getPartnerProfileChannels(String tradingPartnerId, String systemId, RequestContext requestContext) {
        log.debug("#getPartnerProfileChannels: tradingPartnerId = {}, systemId = {}, requestContext = {}", tradingPartnerId, systemId, requestContext);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(COMMUNICATIONS_RESOURCE, tradingPartnerId, systemId),
            this::parseChannelsList
        );
    }

    public List<SystemType> getAllSystemTypes(RequestContext requestContext) {
        log.debug("#getAllSystemTypes: requestContext = {}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            SYSTEM_TYPES_RESOURCE,
            response -> {
                SystemType[] systemTypes = jsonMapper.readValue(response, SystemType[].class);
                return Arrays.asList(systemTypes);
            }
        );
    }

    public String getAllSystemTypesAsRawPayload(RequestContext requestContext) {
        log.debug("#getAllSystemTypesAsRawPayload: requestContext = {}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            SYSTEM_TYPES_RESOURCE
        );
    }

    public List<TypeSystem> getAllTypeSystems(RequestContext requestContext) {
        log.debug("#getAllTypeSystems: requestContext = {}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            TYPE_SYSTEMS_RESOURCE,
            response -> {
                JSONArray jsonArray = new JSONArray(response);
                JSONArray typeSystemList = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    typeSystemList = jsonObject.optJSONArray("typeSystemList");
                    if (typeSystemList != null) {
                        break;
                    }
                }
                if (typeSystemList == null) {
                    return Collections.emptyList();
                }

                List<TypeSystem> typeSystems = new ArrayList<>();
                for (int i = 0; i < typeSystemList.length(); i++) {
                    JSONObject typeSystemJsonObject = typeSystemList.getJSONObject(i);
                    TypeSystem typeSystem = new TypeSystem();
                    typeSystem.setId(typeSystemJsonObject.getString("id"));
                    typeSystem.setAcronym(typeSystem.getId());
                    typeSystems.add(typeSystem);

                    JSONObject annotation = typeSystemJsonObject.optJSONObject("annotation");
                    if (annotation == null) {
                        continue;
                    }

                    JSONArray documentation = annotation.optJSONArray("documentation");
                    if (documentation == null) {
                        continue;
                    }

                    for (int j = 0; j < documentation.length(); j++) {
                        JSONObject documentationEntry = documentation.getJSONObject(j);
                        String source = documentationEntry.optString("source");
                        if ("Acronym".equals(source)) {
                            String text = documentationEntry.optString("#text");
                            if (StringUtils.isNotEmpty(text)) {
                                typeSystem.setAcronym(text);
                            }
                            break;
                        }
                    }
                }
                return typeSystems;
            }
        );
    }

    public List<TypeSystemVersion> getTypeSystemVersions(String typeSystem, RequestContext requestContext) {
        log.debug("#getTypeSystemVersions: typeSystem = {}, requestContext = {}", typeSystem, requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(TYPE_SYSTEM_VERSIONS_RESOURCE, typeSystem),
            response -> {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray valueArray = jsonObject.getJSONArray("value");
                List<TypeSystemVersion> typeSystemVersions = new ArrayList<>();
                for (int i = 0; i < valueArray.length(); i++) {
                    JSONObject valueObject = valueArray.getJSONObject(i);
                    TypeSystemVersion typeSystemVersion = new TypeSystemVersion();
                    typeSystemVersion.setId(valueObject.getString("Id"));
                    typeSystemVersion.setDisplayId(valueObject.getString("DisplayId"));
                    typeSystemVersions.add(typeSystemVersion);
                }
                return typeSystemVersions;
            }
        );
    }

    public List<Product> getAllProducts(RequestContext requestContext) {
        log.debug("#getAllProducts: requestContext = {}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PRODUCTS_RESOURCE,
            response -> {
                JSONArray jsonArray = new JSONArray(response);
                JSONArray productsArray = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject productJsonObject = jsonObject.optJSONObject("product");
                    if (productJsonObject != null) {
                        productsArray = productJsonObject.optJSONArray("products");
                        break;
                    }
                }
                if (productsArray == null) {
                    return Collections.emptyList();
                }


                List<Product> products = new ArrayList<>();
                for (int i = 0; i < productsArray.length(); i++) {
                    JSONObject productJsonObject = productsArray.getJSONObject(i);
                    Product product = new Product();
                    product.setName(optString(productJsonObject, "Name"));
                    product.setTitle(optString(productJsonObject, "Title"));
                    product.setThirdParty(productJsonObject.optBoolean("ThirdParty"));
                    product.setParent(optString(productJsonObject, "Parent"));
                    product.setCreatedBy(optString(productJsonObject, "CreatedBy"));
                    product.setCreatedAt(parseDate(optString(productJsonObject, "CreatedAt")));
                    product.setModifiedBy(optString(productJsonObject, "ModifiedBy"));
                    product.setModifiedAt(parseDate(optString(productJsonObject, "ModifiedAt")));
                    JSONObject metadata = productJsonObject.optJSONObject("__metadata");
                    if (metadata != null) {
                        product.setMetadataId(optString(metadata, "id"));
                    }

                    products.add(product);
                }
                return products;
            }
        );
    }

    public List<Adapter> getSenderAdapters(RequestContext requestContext) {
        log.debug("#getSenderAdapters: requestContext = {}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            SENDER_ADAPTER_LIST_RESOURCE,
            response -> {
                Adapter[] adapters = jsonMapper.readValue(response, Adapter[].class);
                return Arrays.asList(adapters);
            }
        );
    }

    public List<Adapter> getReceiverAdapters(RequestContext requestContext) {
        log.debug("#getReceiverAdapters: requestContext = {}", requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            RECEIVER_ADAPTER_LIST_RESOURCE,
            response -> {
                Adapter[] adapters = jsonMapper.readValue(response, Adapter[].class);
                return Arrays.asList(adapters);
            }
        );
    }

    public ProfileConfiguration resolveProfileConfiguration(String tradingPartnerId, RequestContext requestContext) {
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

    public TpmObjectDetails createTradingPartner(CreateTradingPartnerRequest createTradingPartnerRequest, RequestContext requestContext) {
        log.debug("#createTradingPartner: createTradingPartnerRequest = {}, requestContext = {}", createTradingPartnerRequest, requestContext);

        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            TRADING_PARTNER_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateTradingPartnerRequest> requestEntity = new HttpEntity<>(createTradingPartnerRequest, httpHeaders);
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

    public String createSystemType(CreateSystemTypeRequest createSystemTypeRequest, RequestContext requestContext) {
        log.debug("#createSystem: createSystemTypeRequest = {}, requestContext = {}", createSystemTypeRequest, requestContext);

        return executeMethod(
            requestContext.withPreservingIntegrationSuiteUrl(),
            PATH_FOR_TOKEN,
            SYSTEM_TYPES_RESOURCE,
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateSystemTypeRequest> requestEntity = new HttpEntity<>(createSystemTypeRequest, httpHeaders);
                ResponseEntity<String> responseEntity = restTemplateWrapper.getRestTemplate().exchange(url, HttpMethod.POST, requestEntity, String.class);
                if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                    throw new ClientIntegrationException(format(
                        "Couldn't create system type. Code: %d, Message: %s",
                        responseEntity.getStatusCode().value(),
                        requestEntity.getBody())
                    );
                }
                return new JSONObject(responseEntity.getBody()).getString("id");
            }
        );
    }

    public System createSystem(String tradingPartnerId, CreateSystemRequest createSystemRequest, RequestContext requestContext) {
        log.debug("#createSystem: tradingPartnerId = {}, createSystemRequest = {}, requestContext = {}", tradingPartnerId, createSystemRequest, requestContext);

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

    public void createCommunication(String tradingPartnerId, String systemId, CreateCommunicationRequest createCommunicationRequest, RequestContext requestContext) {
        log.debug("#createCommunication: tradingPartnerId = {}, systemId = {}, createCommunicationRequest = {}, requestContext = {}", tradingPartnerId, systemId, createCommunicationRequest, requestContext);

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

    public void createIdentifier(String tradingPartnerId, CreateIdentifierRequest createIdentifierRequest, RequestContext requestContext) {
        log.debug("#createIdentifier: tradingPartnerId = {}, createIdentifierRequest = {}, requestContext = {}", tradingPartnerId, createIdentifierRequest, requestContext);

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

    public void createSignatureVerificationConfiguration(String tradingPartnerId, CreateSignatureVerificationConfigurationRequest createSignatureVerificationConfigurationRequest, RequestContext requestContext) {
        log.debug("#createSignatureVerificationConfigurationRequest: tradingPartnerId = {}, createSignatureVerificationConfigurationRequest = {}, requestContext = {}", tradingPartnerId, createSignatureVerificationConfigurationRequest, requestContext);

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
