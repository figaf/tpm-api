package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.figaf.integration.tpm.entity.AdministrativeData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class SystemType {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("DeploymentType")
    private String deploymentType;

    @JsonProperty("SAPProduct")
    private String sapProduct;

    @JsonProperty("DocumentSchemaVersion")
    private String documentSchemaVersion;

    private AdministrativeData administrativeData;
    private Map<String, List<String>> searchableAttributes;

    private String artifactStatus;
    private String id;
    private String uniqueId;
    private String artifactType;
    private String displayName;

}
