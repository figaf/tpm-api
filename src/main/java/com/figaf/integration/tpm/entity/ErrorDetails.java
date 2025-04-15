package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ErrorDetails {

    private String id;
    private String errorInformation;
    private String errorCategory;
}
