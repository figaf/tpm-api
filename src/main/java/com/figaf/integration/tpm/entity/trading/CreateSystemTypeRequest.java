package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
public class CreateSystemTypeRequest {

    @JsonProperty("DeploymentType")
    private String deploymentType;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("SAPProduct")
    private String sapProduct;

}
