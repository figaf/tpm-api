package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public abstract class BaseTpmObject implements Serializable {

    private String id;
    private String uniqueId;
    private String displayName;
    private String semanticVersion;
    private String artifactStatus;
    private AdministrativeData administrativeData;
    private String jsonPayload;

    public abstract String getType();
}
