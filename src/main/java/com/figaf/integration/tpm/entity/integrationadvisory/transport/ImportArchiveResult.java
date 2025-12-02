package com.figaf.integration.tpm.entity.integrationadvisory.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class ImportArchiveResult {

    @JsonProperty("ArtifactMetadata")
    private ArtifactMetadata artifactMetadata;

    @JsonProperty("TypeOfImport")
    private String typeOfImport;

    @JsonProperty("Value")
    private Value value;

    @JsonProperty("CreatedOn")
    private Date createdOn;

    @JsonProperty("CreatedBy")
    private String createdBy;

    @JsonProperty("Tenant")
    private String tenant;

    @JsonProperty("ImportedBy")
    private String importedBy;

    @JsonProperty("ImportedOn")
    private Date importedOn;

    @Getter
    @Setter
    @ToString
    public static class ArtifactMetadata {

        @JsonProperty("SchemaVersion")
        private String schemaVersion;

        @JsonProperty("ArtifactType")
        private String artifactType;

    }

    @Getter
    @Setter
    @ToString
    public static class Value {

        @JsonProperty("Migs")
        private List<MigVersionImportResult> migs;

        @JsonProperty("Mags")
        private List<MagVersionImportResult> mags;

    }
}
