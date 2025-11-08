package com.figaf.integration.tpm.entity.integrationadvisory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class MigVersion implements IntegrationAdvisoryObjectVersion {

    //this not a typo that ObjectGUID reflects version ID, this is how SAP works...
    @JsonProperty("ObjectGUID")
    private String versionId;

    @JsonProperty("MIGGUID")
    private String objectId;

    @JsonProperty("MIGVersionId")
    private String version;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("ModifiedDate")
    private Date modifiedDate;

    @JsonProperty("ModifiedBy")
    private String modifiedBy;

    @JsonProperty("ImportCorrelationGroupId")
    private String importCorrelationGroupId;

    @JsonProperty("ImportCorrelationObjectId")
    private String importCorrelationObjectId;

    @Override
    public TpmObjectType getObjectType() {
        return TpmObjectType.CLOUD_MIG;
    }
}
