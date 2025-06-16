package com.figaf.integration.tpm.client.trading;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.*;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.TradingPartnerVerboseDto;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;
import com.figaf.integration.tpm.parser.TradingPartnerVerboseParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.figaf.integration.common.utils.Utils.optString;
import static com.figaf.integration.common.utils.Utils.parseDate;
import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;

@Slf4j
public class TradingPartnerClient extends TpmBaseClient {

    public TradingPartnerClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TpmObjectMetadata> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            TRADING_PARTNER_RESOURCE,
            (response) -> new GenericTpmResponseParser().parseResponse(response, TpmObjectType.CLOUD_TRADING_PARTNER)
        );
    }

    public TradingPartnerVerboseDto getById(String tradingPartnerId, RequestContext requestContext) {
        log.debug("#getById: requestContext={}, tradingPartnerId={}", requestContext, tradingPartnerId);

        return executeGet(
            requestContext,
            format(TRADING_PARTNER_RESOURCE_BY_ID, tradingPartnerId),
            new TradingPartnerVerboseParser()::parse
        );
    }

    public String getRawById(String tradingPartnerId, RequestContext requestContext) {
        log.debug("#getRawById: tradingPartnerId={}, requestContext={}", tradingPartnerId, requestContext);

        return executeGet(
            requestContext,
            format(TRADING_PARTNER_RESOURCE_BY_ID, tradingPartnerId),
            (response) -> response
        );
    }

    public List<SystemType> getAllSystemTypes(RequestContext requestContext) {
        log.debug("#getAllSystemTypes: requestContext = {}", requestContext);

        return executeGet(
            requestContext,
            SYSTEM_TYPES_RESOURCE,
            response -> {
                SystemType[] systemTypes = jsonMapper.readValue(response, SystemType[].class);
                return Arrays.asList(systemTypes);
            }
        );
    }

    public List<TypeSystem> getAllTypeSystems(RequestContext requestContext) {
        log.debug("#getAllTypeSystems: requestContext = {}", requestContext);

        return executeGet(
            requestContext,
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
            requestContext,
            format(TYPE_SYSTEM_VERSIONS_RESOURCE, typeSystem),
            response -> {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray valueArray = jsonObject.getJSONArray("value");
                List<TypeSystemVersion> typeSystemVersions = new ArrayList<>();
                for  (int i = 0; i < valueArray.length(); i++) {
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
            requestContext,
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
            requestContext,
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
            requestContext,
            RECEIVER_ADAPTER_LIST_RESOURCE,
            response -> {
                Adapter[] adapters = jsonMapper.readValue(response, Adapter[].class);
                return Arrays.asList(adapters);
            }
        );
    }

    public TradingPartnerVerboseDto createTradingPartner(CreateTradingPartnerRequest createTradingPartnerRequest, RequestContext requestContext) {
        log.debug("#createTradingPartner: createTradingPartnerRequest = {}, requestContext = {}", createTradingPartnerRequest, requestContext);

        return executeMethod(
            requestContext,
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

                TradingPartnerVerboseParser tradingPartnerVerboseParser = new TradingPartnerVerboseParser();
                return tradingPartnerVerboseParser.parse(responseEntity.getBody());
            }
        );
    }

    public String createSystemType(CreateSystemTypeRequest createSystemTypeRequest, RequestContext requestContext) {
        log.debug("#createSystem: createSystemTypeRequest = {}, requestContext = {}", createSystemTypeRequest, requestContext);

        return executeMethod(
            requestContext,
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
            requestContext,
            PATH_FOR_TOKEN,
            format(SYSTEMS_RESOURCE, tradingPartnerId),
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
            requestContext,
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
            requestContext,
            PATH_FOR_TOKEN,
            format(IDENTIFIERS_RESOURCE, tradingPartnerId),
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

    public void createSignatureVerificationConfigurationsRequest(String tradingPartnerId, CreateSignatureVerificationConfigurationsRequest createSignatureVerificationConfigurationsRequest, RequestContext requestContext) {
        log.debug("#createSignatureVerificationConfigurationsRequest: tradingPartnerId = {}, createSignatureVerificationConfigurationsRequest = {}, requestContext = {}", tradingPartnerId, createSignatureVerificationConfigurationsRequest, requestContext);

        executeMethod(
            requestContext,
            PATH_FOR_TOKEN,
            format(SIGNATURE_VERIFICATION_CONFIGURATIONS_RESOURCE, tradingPartnerId),
            (url, token, restTemplateWrapper) -> {
                HttpHeaders httpHeaders = createHttpHeadersWithCSRFToken(token);
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<CreateSignatureVerificationConfigurationsRequest> requestEntity = new HttpEntity<>(createSignatureVerificationConfigurationsRequest, httpHeaders);
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
