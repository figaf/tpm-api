package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class System {

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("SystemType")
    private String systemType;

    private String displayName;
    private String id;
}
