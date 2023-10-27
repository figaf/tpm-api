package com.figaf.integration.tpm.client;

import com.figaf.integration.common.client.BaseClient;
import com.figaf.integration.common.entity.ConnectionProperties;
import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.exception.ClientIntegrationException;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.common.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @author Kostas Charalambous
 */
public abstract class TpmBaseClient extends BaseClient {

    protected static final String COMPANY_PROFILE_RESOURCE = "/itspaces/tpm/company";

    protected static final String TRADING_PARTNER_RESOURCE = "/itspaces/tpm/tradingpartners";

    protected static final String AGREEMENT_TEMPLATE_RESOURCE = "/itspaces/tpm/agreementtemplates";

    protected static final String AGREEMENT_RESOURCE = "/itspaces/tpm/tradingpartneragreements";

    public TpmBaseClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }
}
