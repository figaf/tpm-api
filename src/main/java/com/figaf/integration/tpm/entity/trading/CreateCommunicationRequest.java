package com.figaf.integration.tpm.entity.trading;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class CreateCommunicationRequest {

    @JsonProperty("Direction")
    private String direction;

    @JsonProperty("AdapterType")
    private String adapterType;

    @JsonProperty("as2PartnerId")
    private String as2PartnerId;

    @JsonProperty("SecurityConfigurationMode")
    private String securityConfigurationMode;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("ConfigurationProperties")
    private ConfigurationProperties configurationProperties = new ConfigurationProperties();

    @Getter
    @Setter
    @ToString
    public static class ConfigurationProperties {

        private Map<String, Attribute> allAttributes = new HashMap<>();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class Attribute {

        private String key;
        private String value;
        @JsonProperty("isPersisted")
        private Boolean isPersisted;

    }
}
