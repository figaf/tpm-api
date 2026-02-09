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
public class Parameter {

    private List<Map<String, Object>> parameterList;

    @JsonProperty("DocumentSchemaVersion")
    private String documentSchemaVersion;

    private AdministrativeData administrativeData;

    private String displayName;
    private String id;

    private String rawPayload;

}
