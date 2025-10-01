package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateSignatureVerificationConfigurationRequest {

    @JsonProperty("artifactType")
    private String artifactType = "TRADING_PARTNER";

    @JsonProperty("VerificationOption")
    private String verificationOption = "NotRequired";

    @JsonProperty("AS2PartnerId")
    private String as2PartnerId;

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("verifyMIC")
    private boolean verifyMIC;

    @JsonProperty("PublicKeyAliasForVerification")
    private String publicKeyAliasForVerification;

}
