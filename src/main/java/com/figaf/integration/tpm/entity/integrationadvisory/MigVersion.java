package com.figaf.integration.tpm.entity.integrationadvisory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.Optional;

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

    @JsonProperty("Documentation")
    private Documentation documentation;

    @JsonProperty("MessageTemplate")
    private MessageTemplate messageTemplate;

    @Override
    public String getName() {
        return Optional.ofNullable(documentation)
            .map(Documentation::getName)
            .map(Name::getArtifactValue)
            .map(ArtifactValue::getId)
            .orElse(null);
    }

    @Override
    public TpmObjectType getObjectType() {
        return TpmObjectType.CLOUD_MIG;
    }

    @Getter
    @Setter
    @ToString
    public static class Documentation {

        @JsonProperty("Name")
        private Name name;
    }

    @Getter
    @Setter
    @ToString
    public static class Name {

        @JsonProperty("ArtifactValue")
        private ArtifactValue artifactValue;
    }

    @Getter
    @Setter
    @ToString
    public static class ArtifactValue {

        @JsonProperty("Id")
        private String id;

        @JsonProperty("LanguageCode")
        private String languageCode;

        @JsonProperty("action")
        private String action;
    }

    @Getter
    @Setter
    @ToString
    public static class MessageTemplate {

        @JsonProperty("Id")
        private String id;

        @JsonProperty("TypeSystemId")
        private String typeSystemName;

        @JsonProperty("VersionId")
        private String typeSystemVersion;
    }

}
