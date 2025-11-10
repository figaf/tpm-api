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
public class MagVersion implements IntegrationAdvisoryObjectVersion {

    @JsonProperty("Name")
    private String name;

    //this not a typo that ObjectGUID reflects version ID, this is how SAP works...
    @JsonProperty("ObjectGUID")
    private String versionId;

    @JsonProperty("MAGGUID")
    private String objectId;

    @JsonProperty("BaseObjectGUID")
    private String baseMagVersionId;

    @JsonProperty("BaseMAGGUID")
    private String baseMagObjectId;

    @JsonProperty("Version")
    private String version;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("CreationDate")
    private Date creationDate;

    @JsonProperty("CreatedBy")
    private String createdBy;

    @JsonProperty("ModifiedDate")
    private Date modifiedDate;

    @JsonProperty("ModifiedBy")
    private String modifiedBy;

    @JsonProperty("SourceMigGUID")
    private String sourceMigVersionId;

    @JsonProperty("TargetMigGUID")
    private String targetMigVersionId;

    @Override
    public TpmObjectType getObjectType() {
        return TpmObjectType.CLOUD_MAG;
    }
}
