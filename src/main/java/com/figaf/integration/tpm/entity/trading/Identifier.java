package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.figaf.integration.tpm.entity.AdministrativeData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Identifier {

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("TypeSystemId")
    private String typeSystemId;

    @JsonProperty("IdentifierId")
    private String identifierId;

    @JsonProperty("SchemeName")
    private String schemeName;

    @JsonProperty("SchemeCode")
    private String schemeCode;

    private AdministrativeData administrativeData;

    private String displayName;
    private String id;

    private String rawPayload;
}
