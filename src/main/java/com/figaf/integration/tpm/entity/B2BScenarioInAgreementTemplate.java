package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class B2BScenarioInAgreementTemplate {

    private String agreementTemplateId;
    private String objectId;
    private String name;
    private Direction direction;

    public enum Direction {

        INBOUND,
        OUTBOUND
    }
}
