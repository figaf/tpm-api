package com.figaf.integration.tpm.entity;

import lombok.*;

@Getter
@Setter
@ToString
public class TpmBusinessEntity extends TpmObjectMetadata {

    private ArtifactProperties artifactProperties;

    //only for Subsidiary
    private String parentId;
}
