package com.figaf.integration.tpm.entity.integrationadvisory.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CheckResult {

    @JsonProperty("OverallResult")
    private String overallResult;

    @JsonProperty("OverallStatus")
    private String overallStatus;

    @JsonProperty("Details")
    private List<CheckResultDetails> details;

    @Getter
    @Setter
    @ToString
    public static class CheckResultDetails {

        @JsonProperty("objectGUID")
        private String objectGuid;

        @JsonProperty("Artifact")
        private String artifact;

        @JsonProperty("Status")
        private String status;

        @JsonProperty("Result")
        private String result;

        @JsonProperty("MessageKey")
        private String messageKey;

        @JsonProperty("ArtifactName")
        private String artifactName;
    }


}
