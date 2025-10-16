package com.figaf.integration.tpm.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateAs2InboundDecryptionConfigurationRequest {

    @JsonProperty("artifactType")
    private String artifactType = "SUBSIDIARY";

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("UserAccount")
    private String userAccount;

}
