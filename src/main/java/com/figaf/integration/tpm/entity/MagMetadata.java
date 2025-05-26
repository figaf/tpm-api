package com.figaf.integration.tpm.entity;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MagMetadata implements Serializable {

    private String magGuid;
    private String magVersion;
    private String magName;
    private String objectGuid;
}
