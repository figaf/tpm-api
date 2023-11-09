package com.figaf.integration.tpm.client.trading;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import com.figaf.integration.tpm.entity.trading.TradingPartner;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;
import com.figaf.integration.tpm.parser.TradingPartnerVerboseParser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

    public TradingPartner getById(String tradingPartnerId, RequestContext requestContext) {
        log.debug("#getTradingPartnerVerboseById: requestContext={}, tradingPartnerId={}", requestContext, tradingPartnerId);

        return executeGet(
            requestContext,
            String.format(TRADING_PARTNER_RESOURCE_BY_ID, tradingPartnerId),
            new TradingPartnerVerboseParser()::parse
        );
    }

    public String getRawById(String tradingPartnerId, RequestContext requestContext) {
        log.debug("#getRawById: tradingPartnerId={}, requestContext={}", tradingPartnerId, requestContext);

        return executeGet(
            requestContext,
            String.format(TRADING_PARTNER_RESOURCE_BY_ID, tradingPartnerId),
            (response) -> response
        );
    }
}
