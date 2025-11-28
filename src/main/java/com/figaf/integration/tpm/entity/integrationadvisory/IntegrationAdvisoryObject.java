package com.figaf.integration.tpm.entity.integrationadvisory;

import com.figaf.integration.tpm.entity.TpmObjectMetadata;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class IntegrationAdvisoryObject extends TpmObjectMetadata {

    private String versionId;
    private String importCorrelationGroupId;
    private String importCorrelationObjectId;
}
