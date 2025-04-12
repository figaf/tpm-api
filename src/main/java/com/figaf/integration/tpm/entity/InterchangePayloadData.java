package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class InterchangePayloadData {

    private String payloadId;
    private String businessDocumentProcessingEventId;
    private String eventType;
    private Date date;
    private String monitoringType;
    private String monitoringId;
}
