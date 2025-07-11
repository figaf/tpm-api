package com.figaf.integration.tpm.entity.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TransactionOption {

    @JsonProperty("Option")
    private String option;

    @JsonProperty("TransactionIds")
    private List<String> transactionIds;
}
