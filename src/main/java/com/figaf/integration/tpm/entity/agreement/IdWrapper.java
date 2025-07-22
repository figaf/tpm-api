package com.figaf.integration.tpm.entity.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
public class IdWrapper {

    @JsonProperty("Properties")
    private PropertiesId properties;

    public IdWrapper(String id) {
        this.properties = new PropertiesId(id);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class PropertiesId {

        @JsonProperty("Id")
        private String id;
    }
}
