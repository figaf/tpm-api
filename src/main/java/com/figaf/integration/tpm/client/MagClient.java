package com.figaf.integration.tpm.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MagClient extends TpmBaseClient {

    private static final String MAG_VERSION_INFO_RESOURCE = "/externalApi/1.0/mags/%s";

    public MagClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public String getMagVersionInfoById(RequestContext requestContext, String magVersionId) {
        log.debug("#getMagVersionInfoById: requestContext = {}, magVersionId = {}", requestContext, magVersionId);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            String.format(MAG_VERSION_INFO_RESOURCE, magVersionId)
        );
    }
}
