package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Subsidiary extends TpmObjectMetadata {

    private String shortName;
    private String webUrl;
    private String logoId;
    private String emailAddress;
    private String phoneNumber;

    private String parentId;

}
