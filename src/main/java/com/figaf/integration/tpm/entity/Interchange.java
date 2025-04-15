package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString(callSuper=true)
public class Interchange extends AbstractInterchange {

    private String overallStatus;
    private Date startedAt;
    private Date endedAt;
    private Date documentCreationTime;

    private List<InterchangePayloadData> interchangePayloadDataList;

    @Override
    public String getMonitoringId() {
        if (CollectionUtils.isEmpty(interchangePayloadDataList)) {
            return null;
        }
        return interchangePayloadDataList.get(0).getMonitoringId();
    }

    public List<InterchangePayloadData> getInterchangePayloadDataList() {
        if (interchangePayloadDataList == null) {
            interchangePayloadDataList = new ArrayList<>();
        }
        return interchangePayloadDataList;
    }
}
