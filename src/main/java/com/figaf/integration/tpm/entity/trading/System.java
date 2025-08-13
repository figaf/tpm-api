package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class System implements Serializable {

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("SystemType")
    private String systemType;

    @JsonProperty("Purpose")
    private String purpose;

    @JsonProperty("TypeSystems")
    private List<TypeSystemWithVersions> typeSystems;

    private String displayName;
    private String id;

}
