package com.figaf.integration.tpm.entity.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AliasWrapper {

    @JsonProperty("Properties")
    private AliasProperties properties;

    @Getter
    @Setter
    @ToString
    public static class AliasProperties {

        @JsonProperty("Alias")
        private String alias;
    }

}
