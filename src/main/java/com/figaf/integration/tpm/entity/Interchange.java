package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class Interchange {

    private String id;
    private String overallStatus;
    private Date startedAt;
    private Date endedAt;
    private Date documentCreationTime;

    private List<InterchangePayloadData> interchangePayloadDataList;

    public List<InterchangePayloadData> getInterchangePayloadDataList() {
        if (interchangePayloadDataList == null) {
            interchangePayloadDataList = new ArrayList<>();
        }
        return interchangePayloadDataList;
    }
}
