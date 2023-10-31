package com.figaf.integration.tpm.entity;

import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TpmObjectMetadata implements Serializable {

    private String id;
    private String uniqueId;
    private String displayName;
    private String semanticVersion;
    private String artifactStatus;
    private TpmObjectReference tpmObjectReference;
    private AdministrativeData administrativeData;
    private String jsonPayload;
    private TpmObjectType tpmObjectType;
}
