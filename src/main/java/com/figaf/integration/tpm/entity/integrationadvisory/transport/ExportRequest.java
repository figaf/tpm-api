package com.figaf.integration.tpm.entity.integrationadvisory.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ToString
public class ExportRequest {

    @JsonProperty("Dependencies")
    private List<Dependency> dependencies = new ArrayList<>();

    @JsonProperty("Mags")
    private List<Map<String, Object>> mags = new ArrayList<>();

    @JsonProperty("Migs")
    private List<Map<String, Object>> migs = new ArrayList<>();

    @JsonProperty("Msgs")
    private List<Map<String, Object>> msgs = new ArrayList<>();

    @JsonProperty("PTSs")
    private List<Map<String, Object>> ptss = new ArrayList<>();
}
