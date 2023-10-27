package com.figaf.integration.tpm.client.trading;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.trading.TradingPartner;
import com.figaf.integration.tpm.parser.GenericTpmResponseParser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TradingPartnerClient extends TpmBaseClient {

    public TradingPartnerClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<TradingPartner> getAll(RequestContext requestContext) {
        log.debug("#getAll: requestContext={}", requestContext);

        return executeGet(
            requestContext,
            TRADING_PARTNER_RESOURCE,
            new GenericTpmResponseParser<>(TradingPartner::new)::parseResponse
        );
    }
}
