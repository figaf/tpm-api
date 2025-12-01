package com.figaf.integration.tpm.entity.agreement_tasks;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TaskParameters(
    @JsonProperty("BT_LIST") List<String> btList
) {
}
