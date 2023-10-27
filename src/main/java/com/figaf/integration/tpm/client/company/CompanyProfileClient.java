package com.figaf.integration.tpm.client.company;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.company.CompanyProfile;
import lombok.extern.slf4j.Slf4j;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;

import java.util.List;

@Slf4j
public class CompanyProfileClient extends TpmBaseClient {

    public CompanyProfileClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<CompanyProfile> getAll(RequestContext requestContext) {
        log.debug("#getAll: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            COMPANY_PROFILE_RESOURCE,
            new GenericTpmResponseParser<>(CompanyProfile::new)::parseResponse
        );
    }
}
