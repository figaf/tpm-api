package com.figaf.integration.tpm.entity.integrationadvisory.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ImportResult {

    @JsonProperty("OverallResult")
    private String overallResult;

    @JsonProperty("Details")
    private List<ImportResultDetails> details;

    @Getter
    @Setter
    @ToString
    public static class ImportResultDetails {

        @JsonProperty("objectGUID")
        private String objectGuid;

        @JsonProperty("Artifact")
        private String artifact;

        @JsonProperty("Result")
        private String result;

        @JsonProperty("MessageKey")
        private String messageKey;

        @JsonProperty("ArtifactName")
        private String artifactName;
    }

}
