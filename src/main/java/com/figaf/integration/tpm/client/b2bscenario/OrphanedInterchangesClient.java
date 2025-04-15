package com.figaf.integration.tpm.client.b2bscenario;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.figaf.integration.common.utils.Utils.optString;
import static com.figaf.integration.common.utils.Utils.parseDate;

@Slf4j
public class OrphanedInterchangesClient extends TpmBaseClient {

    public OrphanedInterchangesClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<OrphanedInterchange> searchOrphanedInterchanges(RequestContext requestContext, OrphanedInterchangeRequest orphanedInterchangeRequest) {
        log.debug("#searchOrphanedInterchanges: requestContext = {}, orphanedInterchangeRequest = {}", requestContext, orphanedInterchangeRequest);
        String filter = orphanedInterchangeRequest.buildFilter();
        String path = String.format("/odata/api/v1/OrphanedInterchanges?$orderby=Date+desc&$filter=%s&$format=json", URLEncoder.encode(filter, StandardCharsets.UTF_8));
        return executeGet(
            requestContext,
            path,
            response -> {
                List<OrphanedInterchange> orphanedInterchanges = new ArrayList<>();
                JSONArray results = new JSONObject(response).getJSONObject("d").getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject jsonObject = results.getJSONObject(i);
                    OrphanedInterchange orphanedInterchange = new OrphanedInterchange();
                    orphanedInterchange.setId(optString(jsonObject, "Id"));
                    orphanedInterchange.setAdapterType(optString(jsonObject, "AdapterType"));
                    orphanedInterchange.setMonitoringType(optString(jsonObject, "MonitoringType"));
                    orphanedInterchange.setMonitoringId(optString(jsonObject, "MonitoringId"));
                    orphanedInterchange.setDate(parseDate(optString(jsonObject, "Date")));
                    orphanedInterchange.setPayloadId(optString(jsonObject, "PayloadId"));

                    orphanedInterchanges.add(orphanedInterchange);
                }
                return orphanedInterchanges;
            }
        );
    }

}
