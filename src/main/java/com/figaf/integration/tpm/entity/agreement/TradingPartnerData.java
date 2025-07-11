package com.figaf.integration.tpm.entity.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class TradingPartnerData {

    @JsonProperty("Role")
    private String role;

    @JsonProperty("AliasForSystemInstance")
    private AliasWrapper aliasForSystemInstance;

    @JsonProperty("TypeSystem")
    private IdWrapper typeSystem;

    @JsonProperty("TypeSystemVersion")
    private String typeSystemVersion;

    @JsonProperty("AliasForIdentifierInOwnTS")
    private Map<String, Object> aliasForIdentifierInOwnTs = new HashMap<>();

    @JsonProperty("AliasForIdentifierInCompanyTS")
    private Map<String, Object> aliasForIdentifierInCompanyTs = new HashMap<>();
}
