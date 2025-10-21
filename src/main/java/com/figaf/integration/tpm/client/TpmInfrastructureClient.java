package com.figaf.integration.tpm.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.trading.*;
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
public class TpmInfrastructureClient extends TpmBaseClient {

    private static final String SYSTEM_TYPES_RESOURCE = "/itspaces/tpm/systemtypes";
    private static final String TYPE_SYSTEMS_RESOURCE = "/itspaces/tpm/bootstrap/?type=typesystems";
    private static final String TYPE_SYSTEM_VERSIONS_RESOURCE = "/itspaces/tpm/api/2.0/typesystems/%s?artifacttype=TypeSystemVersion";
    private static final String SENDER_ADAPTER_LIST_RESOURCE = "/itspaces/tpm/bootstrap?type=adapterlist&direction=Sender";
    private static final String RECEIVER_ADAPTER_LIST_RESOURCE = "/itspaces/tpm/bootstrap?type=adapterlist&direction=Receiver";
    private static final String PRODUCTS_RESOURCE = "/itspaces/tpm/bootstrap/?type=products";

    public TpmInfrastructureClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
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
            requestContext,
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

    public List<TypeSystemVersion> getTypeSystemVersions(RequestContext requestContext, String typeSystem) {
        log.debug("#getTypeSystemVersions: requestContext = {}, typeSystem = {}", requestContext, typeSystem);

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


    public String createSystemType(RequestContext requestContext, CreateSystemTypeRequest createSystemTypeRequest) {
        log.debug("#createSystem: requestContext = {}, createSystemTypeRequest = {}", requestContext, createSystemTypeRequest);

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


}
