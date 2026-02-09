package com.figaf.integration.tpm.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.TpmObjectDetails;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.figaf.integration.tpm.utils.TpmUtils.PATH_FOR_TOKEN;
import static java.lang.String.format;

@Slf4j
public class TradingPartnerClient extends PartnerProfileAbstractClient {

    private static final String COMMUNICATION_PARTNER_RESOURCE = "/itspaces/tpm/tradingpartners";
    private static final String COMMUNICATION_PARTNER_RESOURCE_BY_ID = "/itspaces/tpm/tradingpartners/%s";
    private static final String COMMUNICATION_PARTNER_SYSTEMS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/systems";
    private static final String COMMUNICATION_PARTNER_IDENTIFIERS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/identifiers";
    private static final String COMMUNICATION_PARTNER_PARAMETERS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/parameters";
    private static final String COMMUNICATION_PARTNER_COMMUNICATIONS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/systems/%s/channels";

    private static final String COMMUNICATION_PARTNER_CONFIGURATION_RESOURCE = "/itspaces/tpm/tradingpartners/%s::profileConfiguration";
    private static final String COMMUNICATION_PARTNER_CONFIG_SIGNVAL_RESOURCE = "/itspaces/tpm/tradingpartners/%s/config.signval";

    public TradingPartnerClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    @Override
    protected TpmObjectType getTpmObjectType() {
        return TpmObjectType.CLOUD_TRADING_PARTNER;
    }

    @Override
    protected String getPartnerProfileResource() {
        return COMMUNICATION_PARTNER_RESOURCE;
    }

    @Override
    protected String getPartnerProfileResourceById() {
        return COMMUNICATION_PARTNER_RESOURCE_BY_ID;
    }

    @Override
    protected String getPartnerProfileSystemsResource() {
        return COMMUNICATION_PARTNER_SYSTEMS_RESOURCE;
    }

    @Override
    protected String getPartnerProfileCommunicationsResource() {
        return COMMUNICATION_PARTNER_COMMUNICATIONS_RESOURCE;
    }

    @Override
    protected String getPartnerProfileConfigurationResource() {
        return COMMUNICATION_PARTNER_CONFIGURATION_RESOURCE;
    }

    @Override
    protected String getPartnerProfileConfigSignvalResource() {
        return COMMUNICATION_PARTNER_CONFIG_SIGNVAL_RESOURCE;
    }

    public AggregatedTpmObject getAggregatedPartnerProfile(RequestContext requestContext, String partnerProfileId) {
        log.debug("#getAggregatedPartnerProfile: requestContext = {}, partnerProfileId = {}", requestContext, partnerProfileId);
        TpmObjectDetails tpmObjectDetails = getById(requestContext, partnerProfileId);
        if (tpmObjectDetails == null) {
            return null;
        }

        List<System> systems = getPartnerProfileSystems(requestContext, partnerProfileId);
        List<Identifier> identifiers = getPartnerProfileIdentifiers(requestContext, partnerProfileId);
        List<Parameter> parameters = getPartnerProfileParameters(requestContext, partnerProfileId);

        Map<String, List<Channel>> systemIdToChannels = new LinkedHashMap<>();
        for (System system : systems) {
            List<Channel> partnerProfileChannels = getPartnerProfileChannels(requestContext, partnerProfileId, system.getId());
            systemIdToChannels.put(system.getId(), partnerProfileChannels);
        }

        ProfileConfiguration profileConfiguration = resolveProfileConfiguration(requestContext, partnerProfileId);

        return new AggregatedTpmObject(tpmObjectDetails, systems, identifiers, parameters, systemIdToChannels, profileConfiguration);
    }

    public List<Identifier> getPartnerProfileIdentifiers(RequestContext requestContext, String tradingPartnerId) {
        log.debug("#getPartnerProfileIdentifiers: requestContext = {}, tradingPartnerId = {}", requestContext, tradingPartnerId);
        return executeGet(
                requestContext.withPreservingIntegrationSuiteUrl(),
                format(COMMUNICATION_PARTNER_IDENTIFIERS_RESOURCE, tradingPartnerId),
                this::parseIdentifiersList
        );
    }

    public List<Parameter> getPartnerProfileParameters(RequestContext requestContext, String partnerProfileId) {
        log.debug("#getPartnerProfileParameters: requestContext = {}, partnerProfileId = {}", requestContext, partnerProfileId);
        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            format(COMMUNICATION_PARTNER_PARAMETERS_RESOURCE, partnerProfileId),
            this::parseParametersList
        );
    }

    public void createIdentifier(RequestContext requestContext, String tradingPartnerId, CreateIdentifierRequest createIdentifierRequest) {
        log.debug("#createIdentifier: requestContext = {}, tradingPartnerId = {}, createIdentifierRequest = {}", requestContext, tradingPartnerId, createIdentifierRequest);

        executeMethod(
                requestContext.withPreservingIntegrationSuiteUrl(),
                PATH_FOR_TOKEN,
                format(COMMUNICATION_PARTNER_IDENTIFIERS_RESOURCE, tradingPartnerId),
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

}
