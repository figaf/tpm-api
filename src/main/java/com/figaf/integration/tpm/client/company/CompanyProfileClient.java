package com.figaf.integration.tpm.client.company;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.Subsidiary;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.trading.Channel;
import com.figaf.integration.tpm.entity.trading.Identifier;
import com.figaf.integration.tpm.entity.trading.System;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.extern.slf4j.Slf4j;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

@Slf4j
public class CompanyProfileClient extends TpmBaseClient {

    public CompanyProfileClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TpmObjectMetadata> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            COMPANY_PROFILE_RESOURCE,
            (response) -> new GenericTpmResponseParser().parseResponse(response, TpmObjectType.CLOUD_COMPANY_PROFILE)
        );
    }

    public List<Subsidiary> getSubsidiaries(RequestContext requestContext, String companyId) {
        log.debug("#getSubsidiaries: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext,
            format(COMPANY_SUBSIDIARIES_RESOURCE, companyId),
            response -> {
                Subsidiary[] subsidiaries = jsonMapper.readValue(response, Subsidiary[].class);
                return Arrays.asList(subsidiaries);
            }
        );
    }

    public List<System> getCompanySystems(RequestContext requestContext, String companyId) {
        log.debug("#getCompanySystems: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext,
            format(COMPANY_SYSTEMS_RESOURCE, companyId),
            response -> {
                System[] systems = jsonMapper.readValue(response, System[].class);
                return Arrays.asList(systems);
            }
        );
    }

    public List<System> getSubsidiarySystems(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getSubsidiarySystems: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}", requestContext, parentCompanyId, subsidiaryId);
        return executeGet(
            requestContext,
            format(SUBSIDIARY_SYSTEMS_RESOURCE, parentCompanyId, subsidiaryId),
            response -> {
                System[] systems = jsonMapper.readValue(response, System[].class);
                return Arrays.asList(systems);
            }
        );
    }

    public List<Identifier> getCompanyIdentifiers(RequestContext requestContext, String companyId) {
        log.debug("#getCompanyIdentifiers: requestContext = {}, companyId = {}", requestContext, companyId);
        return executeGet(
            requestContext,
            format(COMPANY_IDENTIFIERS_RESOURCE, companyId),
            response -> {
                Identifier[] identifiers = jsonMapper.readValue(response, Identifier[].class);
                return Arrays.asList(identifiers);
            }
        );
    }

    public List<Identifier> getSubsidiaryIdentifiers(RequestContext requestContext, String parentCompanyId, String subsidiaryId) {
        log.debug("#getSubsidiaryIdentifiers: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}", requestContext, parentCompanyId, subsidiaryId);
        return executeGet(
            requestContext,
            format(SUBSIDIARY_IDENTIFIERS_RESOURCE, parentCompanyId, subsidiaryId),
            response -> {
                Identifier[] identifiers = jsonMapper.readValue(response, Identifier[].class);
                return Arrays.asList(identifiers);
            }
        );
    }

    public List<Channel> getCompanyChannels(RequestContext requestContext, String companyId, String systemId) {
        log.debug("#getCompanyChannels: requestContext = {}, companyId = {}, systemId = {}", requestContext, companyId, systemId);
        return executeGet(
            requestContext,
            format(COMPANY_CHANNELS_RESOURCE, companyId, systemId),
            response -> {
                Channel[] channels = jsonMapper.readValue(response, Channel[].class);
                return Arrays.asList(channels);
            }
        );
    }

    public List<Channel> getSubsidiaryChannels(RequestContext requestContext, String parentCompanyId, String subsidiaryId, String systemId) {
        log.debug("#getSubsidiaryChannels: requestContext = {}, parentCompanyId = {}, subsidiaryId = {}, systemId = {}", requestContext, parentCompanyId, subsidiaryId, systemId);
        return executeGet(
            requestContext,
            format(SUBSIDIARY_CHANNELS_RESOURCE, parentCompanyId, subsidiaryId, systemId),
            response -> {
                Channel[] channels = jsonMapper.readValue(response, Channel[].class);
                return Arrays.asList(channels);
            }
        );
    }

}
