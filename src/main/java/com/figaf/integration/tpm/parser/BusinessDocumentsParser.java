package com.figaf.integration.tpm.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.figaf.integration.tpm.entity.ErrorDetails;
import com.figaf.integration.tpm.entity.Interchange;
import com.figaf.integration.tpm.entity.InterchangePayloadData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.figaf.integration.common.utils.Utils.optString;
import static com.figaf.integration.common.utils.Utils.parseDate;

public class BusinessDocumentsParser {

    public List<Interchange> parseResponse(String response) throws JsonProcessingException {
        List<Interchange> interchanges = new ArrayList<>();
        JSONArray results = new JSONObject(response).getJSONObject("d").getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonObject = results.getJSONObject(i);
            Interchange interchange = new Interchange();
            interchange.setId(optString(jsonObject, "Id"));
            interchange.setOverallStatus(optString(jsonObject, "OverallStatus"));
            interchange.setStartedAt(parseDate(optString(jsonObject, "StartedAt")));
            interchange.setEndedAt(parseDate(optString(jsonObject, "EndedAt")));
            interchange.setDocumentCreationTime(parseDate(optString(jsonObject, "DocumentCreationTime")));

            interchanges.add(interchange);
        }
        return interchanges;
    }

    public List<InterchangePayloadData> parsePayloadsResponse(String response) {
        List<InterchangePayloadData> interchangePayloadDataList = new ArrayList<>();
        JSONArray results = new JSONObject(response).getJSONObject("d").getJSONArray("results");
        for (int i = 0; i < results.length(); i++) {
            JSONObject jsonObject = results.getJSONObject(i);
            InterchangePayloadData interchangePayloadData = new InterchangePayloadData();
            interchangePayloadData.setPayloadId(optString(jsonObject, "Id"));

            JSONObject businessDocumentProcessingEvent = jsonObject.optJSONObject("BusinessDocumentProcessingEvent");
            if (businessDocumentProcessingEvent != null) {
                interchangePayloadData.setBusinessDocumentProcessingEventId(optString(businessDocumentProcessingEvent, "Id"));
                interchangePayloadData.setEventType(optString(businessDocumentProcessingEvent, "EventType"));
                interchangePayloadData.setDate(parseDate(optString(businessDocumentProcessingEvent, "Date")));
                interchangePayloadData.setMonitoringType(optString(businessDocumentProcessingEvent, "MonitoringType"));
                interchangePayloadData.setMonitoringId(optString(businessDocumentProcessingEvent, "MonitoringId"));
            }

            interchangePayloadDataList.add(interchangePayloadData);
        }

        //https://chatgpt.com/c/67f923aa-545c-8001-8fe0-01bc370d71f3
        interchangePayloadDataList.sort(
            Comparator.comparing(
                    // Convert the condition to a boolean: 'true' means "NOT a BUSINESSDOCUMENT_CREATE_EVENT", so those come later.
                    (InterchangePayloadData data) -> !"BUSINESSDOCUMENT_CREATE_EVENT".equals(data.getEventType())
                )
                // Then sort by date ascending within each subgroup
                .thenComparing(InterchangePayloadData::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        return interchangePayloadDataList;
    }

    public ErrorDetails parseErrorDetails(String response) {
        JSONObject errorDetailsJsonObject = new JSONObject(response).getJSONObject("d");
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setId(optString(errorDetailsJsonObject, "Id"));
        errorDetails.setErrorInformation(optString(errorDetailsJsonObject, "ErrorInformation"));
        errorDetails.setErrorCategory(optString(errorDetailsJsonObject, "ErrorCategory"));
        return errorDetails;
    }

}
