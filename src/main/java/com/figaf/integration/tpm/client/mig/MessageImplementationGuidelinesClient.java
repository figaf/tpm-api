package com.figaf.integration.tpm.client.mig;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.parser.MigResponseParser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class MessageImplementationGuidelinesClient extends TpmBaseClient {

    public MessageImplementationGuidelinesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TpmObjectMetadata> getAllLatestMetadata(RequestContext requestContext) {
        log.debug("#getAllLatestMetadata: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            MIG_RESOURCE,
            (response) -> new MigResponseParser().parseJsonToTpmObjectMetadata(response)
        );
    }

    public String getRawById(String migVersionId, RequestContext requestContext) {
        log.debug("#getRawById: migVersionId={}, requestContext={}", migVersionId, requestContext);

        return executeGet(
            requestContext,
            String.format(MIG_RESOURCE_BY_ID, migVersionId),
            (response) -> response
        );
    }
}

