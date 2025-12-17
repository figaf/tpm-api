package com.figaf.integration.tpm.entity.agreement_tasks;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TaskResult(@JsonProperty("result") Boolean result) {
}
