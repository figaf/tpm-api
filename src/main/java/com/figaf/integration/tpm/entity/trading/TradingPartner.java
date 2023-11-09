package com.figaf.integration.tpm.entity.trading;

import com.figaf.integration.tpm.entity.AdministrativeData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class TradingPartner {

    private String name;
    private String shortName;
    private String webURL;
    private String logoId;
    private String emailAddress;
    private String phoneNumber;
    private ProfileDto profile;
    private String documentSchemaVersion;
    private Map<String, List<String>> searchableAttributes;
    private ArtifactProperties artifactProperties;
    private List<Object> relations;
    private AdministrativeData administrativeData;
    private Map<String, Object> artifactRelations;
    private String artifactType;
    private String artifactStatus;
    private String semanticVersion;
    private String uniqueId;
    private String id;
    private String displayedName;
}
