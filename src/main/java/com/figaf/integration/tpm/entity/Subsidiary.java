package com.figaf.integration.tpm.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Subsidiary {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("ShortName")
    private String shortName;

    @JsonProperty("WebURL")
    private String webUrl;

    @JsonProperty("LogoId")
    private String logoId;

    @JsonProperty("EmailAddress")
    private String emailAddress;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    private String id;

}
