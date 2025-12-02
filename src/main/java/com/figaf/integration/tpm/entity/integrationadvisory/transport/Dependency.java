package com.figaf.integration.tpm.entity.integrationadvisory.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Dependency {

    @JsonProperty("Dep")
    private String dep;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Type")
    private String type;

}
