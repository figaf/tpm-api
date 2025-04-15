package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString(callSuper = true)
public class OrphanedInterchange extends AbstractInterchange {

    private String adapterType;
    private String monitoringType;
    private String monitoringId;
    private Date date;
    private String payloadId;

}
