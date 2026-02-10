package com.figaf.integration.tpm.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.entity.trading.*;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.entity.trading.verbose.TpmObjectDetails;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CommunicationPartnerPartnerClient extends PartnerProfileAbstractClient {

    private static final String COMMUNICATION_PARTNER_RESOURCE = "/itspaces/tpm/communicationpartners";
    private static final String COMMUNICATION_PARTNER_RESOURCE_BY_ID = "/itspaces/tpm/communicationpartners/%s";
    private static final String COMMUNICATION_PARTNER_SYSTEMS_RESOURCE = "/itspaces/tpm/communicationpartners/%s/systems";
    private static final String COMMUNICATION_PARTNER_COMMUNICATIONS_RESOURCE = "/itspaces/tpm/communicationpartners/%s/systems/%s/channels";

    private static final String COMMUNICATION_PARTNER_CONFIGURATION_RESOURCE = "/itspaces/tpm/communicationpartners/%s::profileConfiguration";
    private static final String COMMUNICATION_PARTNER_CONFIG_SIGNVAL_RESOURCE = "/itspaces/tpm/communicationpartners/%s/config.signval";

    public CommunicationPartnerPartnerClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    @Override
    protected TpmObjectType getTpmObjectType() {
        return TpmObjectType.CLOUD_COMMUNICATION_PARTNER;
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

    @Override
    public AggregatedTpmObject getAggregatedPartnerProfile(RequestContext requestContext, String partnerProfileId) {
        log.debug("#getAggregatedPartnerProfile: requestContext = {}, partnerProfileId = {}", requestContext, partnerProfileId);
        TpmObjectDetails tpmObjectDetails = getById(requestContext, partnerProfileId);
        if (tpmObjectDetails == null) {
            return null;
        }

        List<System> systems = getPartnerProfileSystems(requestContext, partnerProfileId);
        Map<String, List<Channel>> systemIdToChannels = new LinkedHashMap<>();
        for (System system : systems) {
            List<Channel> partnerProfileChannels = getPartnerProfileChannels(requestContext, partnerProfileId, system.getId());
            systemIdToChannels.put(system.getId(), partnerProfileChannels);
        }

        ProfileConfiguration profileConfiguration = resolveProfileConfiguration(requestContext, partnerProfileId);

        return new AggregatedTpmObject(tpmObjectDetails, systems, Collections.emptyList(), Collections.emptyList(), systemIdToChannels, profileConfiguration);
    }

}
