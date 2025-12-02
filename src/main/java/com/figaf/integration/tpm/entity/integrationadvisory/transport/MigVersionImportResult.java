package com.figaf.integration.tpm.entity.integrationadvisory.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MigVersionImportResult extends IntegrationAdvisoryObjectImportResult {

    @JsonProperty("MIGGUID")
    private String migGuid;

}
