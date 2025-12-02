package com.figaf.integration.tpm.entity.integrationadvisory.external_api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONArray;

@Getter
@Setter
@ToString
public class Mig {

    private String migGuid;
    private JSONArray migVersions;

}
