package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class B2BScenarioMetadata extends TpmObjectMetadata {

    private String agreementId;
    private String customMappingIFlowUrl;
    private String preIFlowUrl;
    private String postIFlowUrl;
    private MigMetadata senderMigMetadata;
    private MigMetadata receiverMigMetadata;
    private MagMetadata magMetadata;
    private CommunicationChannelTemplateMetadata senderCommunicationChannelMetadata;
    private CommunicationChannelTemplateMetadata receiverCommunicationChannelMetadata;
    private Direction direction;
    private String initiator;
    private String reactor;
    private String senderSystemPurpose;
    private String receiverSystemPurpose;
    private boolean activated;
    private String updateStatus;

    public enum Direction {

        INBOUND,
        OUTBOUND
    }
}
