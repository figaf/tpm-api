package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class CreateSystemRequest {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("SystemType")
    private String systemType;

    @JsonProperty("Purpose")
    private String purpose;

    @JsonProperty("Link")
    private String link;

    @JsonProperty("TypeSystems")
    private List<TypeSystemWithVersions> typeSystems = new ArrayList<>();

    @JsonProperty("CommunicationChannelTemplates")
    private List<String> communicationChannelTemplates = new ArrayList<>();

    @JsonProperty("Id")
    private String id;

}
