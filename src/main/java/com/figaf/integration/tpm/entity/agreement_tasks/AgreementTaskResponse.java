package com.figaf.integration.tpm.entity.agreement_tasks;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AgreementTaskResponse(
    @JsonProperty("TaskInput") AgreementTasksRequest taskInput,
    @JsonProperty("TaskId") String taskId,
    @JsonProperty("TriggeredBy") String triggeredBy,
    @JsonProperty("executionStatus") ExecutionStatus executionStatus,
    @JsonProperty("taskResult") TaskResult taskResult
) {
}
