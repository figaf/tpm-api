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

    private String senderInterchangeTypeSystem;
    private String senderInterchangeTypeSystemVersion;
    private String senderInterchangeMigId;
    private String senderInterchangeMigVersionId;

    private String receiverInterchangeTypeSystem;
    private String receiverInterchangeTypeSystemVersion;
    private String receiverInterchangeMigId;
    private String receiverInterchangeMigVersionId;

    public enum Direction {

        INBOUND,
        OUTBOUND
    }
}
