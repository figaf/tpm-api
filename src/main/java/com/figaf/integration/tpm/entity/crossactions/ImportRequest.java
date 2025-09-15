package com.figaf.integration.tpm.entity.crossactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class ImportRequest {

    @JsonProperty("Action")
    private String action;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("TaskInput")
    private List<TaskInputItem> taskInput;

    @JsonProperty("TaskParameters")
    private Map<String, String> taskParameters;

    @Getter
    @Setter
    @ToString
    public static class TaskInputItem {

        @JsonProperty("id")
        private String id;

        @JsonProperty("uniqueId")
        private String uniqueId;

        @JsonProperty("artifactType")
        private String artifactType;

        @JsonProperty("displayName")
        private String displayName;

        @JsonProperty("semanticVersion")
        private String semanticVersion;

    }
}
