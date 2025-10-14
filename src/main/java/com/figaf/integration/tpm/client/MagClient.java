package com.figaf.integration.tpm.client;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MagClient extends TpmBaseClient {

    public MagClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public String getMagVersionInfoById(String magVersionId, RequestContext requestContext) {
        log.debug("#getMagVersionInfoById: magVersionId={}, requestContext={}", magVersionId, requestContext);

        return executeGet(
            requestContext.withPreservingIntegrationSuiteUrl(),
            String.format(MAG_VERSION_INFO_RESOURCE, magVersionId)
        );
    }
}
