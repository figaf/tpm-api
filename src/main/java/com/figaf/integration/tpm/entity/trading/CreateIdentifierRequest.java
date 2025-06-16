package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateIdentifierRequest {

    @JsonProperty("IsGroupIdentifier")
    private boolean isGroupIdentifier;

    @JsonProperty("TypeSystemId")
    private String typeSystemId;

    @JsonProperty("SchemeCode")
    private String schemeCode;

    @JsonProperty("SchemeName")
    private String schemeName;

    @JsonProperty("IdentifierId")
    private String identifierId;

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("Agency")
    private String agency;

}
