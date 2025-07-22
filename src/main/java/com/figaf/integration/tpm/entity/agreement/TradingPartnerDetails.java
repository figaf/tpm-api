package com.figaf.integration.tpm.entity.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TradingPartnerDetails {

    @JsonProperty("IdForTradingPartner")
    private IdWrapper idForTradingPartner;

    @JsonProperty("IdForSystemInstance")
    private IdWrapper idForSystemInstance;

    @JsonProperty("IdForSenderIdentifier")
    private IdWrapper idForSenderIdentifier;

    @JsonProperty("IdForReceiverIdentifier")
    private IdWrapper idForReceiverIdentifier;

    @JsonProperty("IdForContactPerson")
    private IdWrapper idForContactPerson;
}
