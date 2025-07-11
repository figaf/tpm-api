package com.figaf.integration.tpm.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.factory.HttpClientsFactory;

/**
 * @author Kostas Charalambous
 */
public abstract class TpmBaseClient extends BaseClient {

    protected static final String MIG_RESOURCE = "/api/1.0/migs";
    public static final String MIG_RESOURCE_BY_ID = "/api/1.0/migs/%s";
    public static final String MIG_CREATE_DRAFT_RESOURCE = "/api/1.0/migs/%s/migVersions?source=%s&status=draft";
    protected static final String COMPANY_PROFILE_RESOURCE = "/itspaces/tpm/company";
    protected static final String COMPANY_SUBSIDIARIES_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries";
    protected static final String COMPANY_SYSTEMS_RESOURCE = "/itspaces/tpm/company/%s/systems";
    protected static final String SUBSIDIARY_SYSTEMS_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/systems";
    protected static final String COMPANY_IDENTIFIERS_RESOURCE = "/itspaces/tpm/company/%s/identifiers";
    protected static final String SUBSIDIARY_IDENTIFIERS_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/identifiers";
    protected static final String COMPANY_CHANNELS_RESOURCE = "/itspaces/tpm/company/%s/systems/%s/channels";
    protected static final String SUBSIDIARY_CHANNELS_RESOURCE = "/itspaces/tpm/company/%s/subsidiaries/%s/systems/%s/channels";

    protected static final String TRADING_PARTNER_RESOURCE = "/itspaces/tpm/tradingpartners";
    protected static final String TRADING_PARTNER_RESOURCE_BY_ID = "/itspaces/tpm/tradingpartners/%s";
    protected static final String AGREEMENT_TEMPLATE_RESOURCE = "/itspaces/tpm/api/2.0/agreementtemplates";
    protected static final String AGREEMENT_TEMPLATE_B2B_SCENARIOS_RESOURCE = "/itspaces/tpm/api/2.0/agreementtemplates/%s/b2bscenario/%s";
    public static final String MIG_DELETE_DRAFT_RESOURCE = "/api/1.0/migs/%s/migVersions/%s";
    protected static final String AGREEMENTS_RESOURCE = "/itspaces/tpm/api/2.0/tradingpartneragreements";
    protected static final String AGREEMENT_RESOURCE = "/itspaces/tpm/api/2.0/tradingpartneragreements/%s";
    protected static final String B2B_SCENARIOS_RESOURCE = "/itspaces/tpm/api/2.0/tradingpartneragreements/%s/b2bscenario";
    protected static final String B2B_SCENARIO_RESOURCE = "/itspaces/tpm/api/2.0/tradingpartneragreements/%s/b2bscenario/%s";
    protected static final String SYSTEM_TYPES_RESOURCE = "/itspaces/tpm/systemtypes";
    protected static final String TRADING_PARTNER_SYSTEMS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/systems";
    protected static final String TYPE_SYSTEMS_RESOURCE = "/itspaces/tpm/bootstrap/?type=typesystems";
    protected static final String TYPE_SYSTEM_VERSIONS_RESOURCE = "/itspaces/tpm/api/2.0/typesystems/%s?artifacttype=TypeSystemVersion";
    protected static final String SENDER_ADAPTER_LIST_RESOURCE = "/itspaces/tpm/bootstrap?type=adapterlist&direction=Sender";
    protected static final String RECEIVER_ADAPTER_LIST_RESOURCE = "/itspaces/tpm/bootstrap?type=adapterlist&direction=Receiver";
    protected static final String PRODUCTS_RESOURCE = "/itspaces/tpm/bootstrap/?type=products";
    protected static final String TRADING_PARTNER_IDENTIFIERS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/identifiers";
    protected static final String COMMUNICATIONS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/systems/%s/channels";
    protected static final String SIGNATURE_VERIFICATION_CONFIGURATIONS_RESOURCE = "/itspaces/tpm/tradingpartners/%s/config.signval";

    protected final ObjectMapper jsonMapper;

    public TpmBaseClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
        jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
}
