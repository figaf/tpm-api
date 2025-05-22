package com.figaf.integration.tpm.entity;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class MigMetadata implements Serializable {

    private String migGuid;
    private String migVersion;
    private String migName;
    private String objectGuid;
}
