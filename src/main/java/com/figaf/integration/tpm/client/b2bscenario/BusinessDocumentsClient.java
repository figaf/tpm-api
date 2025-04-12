package com.figaf.integration.tpm.client.b2bscenario;

import com.figaf.integration.common.entity.RequestContext;
import com.figaf.integration.common.factory.HttpClientsFactory;
import com.figaf.integration.tpm.client.TpmBaseClient;
import com.figaf.integration.tpm.entity.Interchange;
import com.figaf.integration.tpm.entity.InterchangePayloadData;
import com.figaf.integration.tpm.entity.InterchangeRequest;
import com.figaf.integration.tpm.parser.BusinessDocumentsParser;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
public class BusinessDocumentsClient extends TpmBaseClient {

    public BusinessDocumentsClient(HttpClientsFactory httpClientsFactory) {
        super(httpClientsFactory);
    }

    public List<Interchange> searchInterchanges(RequestContext requestContext, Date leftBoundDate, InterchangeRequest interchangeRequest) {
        log.debug("#searchInterchanges: requestContext = {}, leftBoundDate = {}, interchangeRequest = {}", requestContext, leftBoundDate, interchangeRequest);
        String filter = interchangeRequest.buildFilter(leftBoundDate);
        String path = String.format("/odata/api/v1/BusinessDocuments?$orderby=DocumentCreationTime&20desc&$filter=%s&$format=json", URLEncoder.encode(filter, StandardCharsets.UTF_8));
        List<Interchange> interchanges = executeGet(
            requestContext,
            path,
            (response) -> new BusinessDocumentsParser().parseResponse(response)
        );
        for (Interchange interchange : interchanges) {
            try {
                List<InterchangePayloadData> interchangePayloadDataList = getBusinessDocumentPayloadDataListByInterchangeId(requestContext, interchange.getId());
                interchange.setInterchangePayloadDataList(interchangePayloadDataList);
            }
            catch (Exception e) {
                log.warn("Can't get BusinessDocumentPayloads data for interchange {}", interchange, e);
            }
        }
        return interchanges;
    }

    public List<InterchangePayloadData> getBusinessDocumentPayloadDataListByInterchangeId(RequestContext requestContext, String interchangeId) {
        log.debug("#getBusinessDocumentPayloadDataListByInterchangeId: requestContext = {}, interchangeId = {}", requestContext, interchangeId);
        String path = String.format("/odata/api/v1/BusinessDocuments('%s')/BusinessDocumentPayloads?$expand=BusinessDocumentProcessingEvent&$format=json", interchangeId);
        return executeGet(
            requestContext,
            path,
            (response) -> new BusinessDocumentsParser().parsePayloadsResponse(response)
        );
    }

    public byte[] downloadPayload(RequestContext requestContext, String payloadId) {
        log.debug("#downloadPayload: requestContext = {}, payloadId = {}", requestContext, payloadId);
        String path = String.format("/odata/api/v1/BusinessDocumentPayloads('%s')/$value", payloadId);
        return executeGet(
            requestContext,
            path,
            response -> response.getBytes(StandardCharsets.UTF_8)
        );
    }

}
