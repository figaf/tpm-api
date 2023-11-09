package com.figaf.integration.tpm.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.factory.HttpClientsFactory;

/**
 * @author Kostas Charalambous
 */
public abstract class TpmBaseClient extends BaseClient {

    protected static final String COMPANY_PROFILE_RESOURCE = "/itspaces/tpm/company";

    protected static final String TRADING_PARTNER_RESOURCE = "/itspaces/tpm/tradingpartners";
    protected static final String TRADING_PARTNER_RESOURCE_BY_ID = "/itspaces/tpm/tradingpartners/%s";
    protected static final String AGREEMENT_TEMPLATE_RESOURCE = "/itspaces/tpm/agreementtemplates";

    protected static final String AGREEMENT_RESOURCE = "/itspaces/tpm/tradingpartneragreements";

    public TpmBaseClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }
}
