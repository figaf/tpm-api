package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class AgreementLifeCycleMetadata implements Serializable {
    private boolean activated;
    private String updatedStatus;
    private Date activatedAt;
}
