package com.figaf.integration.tpm.entity.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CompanyData {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Role")
    private String role;

    @JsonProperty("SystemInstance")
    private IdWrapper systemInstance;

    @JsonProperty("TypeSystem")
    private IdWrapper typeSystem;

    @JsonProperty("TypeSystemVersion")
    private String typeSystemVersion;

    @JsonProperty("IdAsSender")
    private IdWrapper idAsSender;

    @JsonProperty("IdAsReceiver")
    private IdWrapper idAsReceiver;

    @JsonProperty("ContactPerson")
    private IdWrapper contactPerson;

    @JsonProperty("SelectedProfileType")
    private String selectedProfileType;

    @JsonProperty("ParentId")
    private String parentId;
}
