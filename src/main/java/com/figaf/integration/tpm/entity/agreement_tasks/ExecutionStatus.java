package com.figaf.integration.tpm.entity.agreement_tasks;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExecutionStatus(
    @JsonProperty("Status") String status,
    @JsonProperty("PercentComplete") Integer percentComplete,
    @JsonProperty("ErrorMessage") String errorMessage,
    @JsonProperty("completedAt") Long completedAt
) {
}
