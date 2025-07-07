package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class TypeSystemWithVersions {

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Versions")
    private List<TypeSystemVersion> versions = new ArrayList<>();

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class TypeSystemVersion {

        @JsonProperty("Id")
        private String id;

        @JsonProperty("Name")
        private String name;
    }

}
