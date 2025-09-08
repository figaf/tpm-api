package com.figaf.integration.tpm.entity.trading;

import com.figaf.integration.tpm.entity.AdministrativeData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProfileConfiguration {

    private String parentId;
    private String parentArtifactType;
    private String displayName;
    private String id;
    private String artifactType;
    private AdministrativeData administrativeData;

    private String rawPayload;
}
