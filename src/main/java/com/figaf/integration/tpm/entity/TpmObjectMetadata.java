package com.figaf.integration.tpm.entity;

import com.figaf.integration.tpm.entity.trading.verbose.ArtifactProperties;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TpmObjectMetadata implements Serializable {

    private String objectId;
    private TpmObjectType tpmObjectType;
    private String versionId;
    private String displayedName;
    private String version;
    private String status;
    private List<TpmObjectReference> tpmObjectReferences;
    private AdministrativeData administrativeData;
    private String payload;

    //only for trading partner
    private ArtifactProperties artifactProperties;

    //only for agreement template
    private String b2bScenarioDetailsId;
}
