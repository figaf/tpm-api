package com.figaf.integration.tpm.client.agreement;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AgreementTemplateClient extends TpmBaseClient {

    public AgreementTemplateClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TpmObjectMetadata> getAllMetadata(RequestContext requestContext) {
        log.debug("#getAllMetadata: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            AGREEMENT_TEMPLATE_RESOURCE,
            (response) -> new GenericTpmResponseParser().parseResponse(response, TpmObjectType.CLOUD_AGREEMENT_TEMPLATE)
        );
    }
}
