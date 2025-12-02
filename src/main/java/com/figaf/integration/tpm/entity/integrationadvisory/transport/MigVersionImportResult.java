package com.figaf.integration.tpm.entity.integrationadvisory.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MigVersionImportResult {

    @JsonProperty("VersionId")
    private String versionId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("CheckResult")
    private CheckResult checkResult;

    @JsonProperty("ImportResult")
    private ImportResult importResult;

}
