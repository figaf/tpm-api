package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class B2BScenarioInAgreementTemplate implements Serializable {

    private String agreementTemplateId;
    private String objectId;
    private String name;
    private Direction direction;

    public enum Direction {

        INBOUND,
        OUTBOUND
    }
}
