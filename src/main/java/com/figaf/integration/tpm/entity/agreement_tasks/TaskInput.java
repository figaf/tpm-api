package com.figaf.integration.tpm.entity.agreement_tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.figaf.integration.tpm.entity.agreement_tasks.enums.AgreementTasksArtifactType;

public record TaskInput(
    @JsonProperty("id") String id,
    @JsonProperty("uniqueId") String uniqueId,
    @JsonProperty("artifactType") AgreementTasksArtifactType artifactType,
    @JsonProperty("displayName") String displayName,
    @JsonProperty("semanticVersion") String semanticVersion
) {
}
