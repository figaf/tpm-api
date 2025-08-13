package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Channel implements Serializable {

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Direction")
    private String direction;

    @JsonProperty("AdapterType")
    private String adapterType;

    @JsonProperty("SecurityConfigurationMode")
    private String securityConfigurationMode;

    private String displayName;
    private String id;

}
