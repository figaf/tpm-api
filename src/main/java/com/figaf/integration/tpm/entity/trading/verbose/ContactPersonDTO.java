package com.figaf.integration.tpm.entity.trading.verbose;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ContactPersonDTO {

    private String id;
    private String familyName;
    private String givenName;
    private boolean isPrimaryContact;
    private ContactPersonAddressDTO address;
    private boolean isNewVersion;
    private List<ContactChannelDTO> contactChannels;
    private String documentSchemaVersion;
    private Map<String, Object> artifactProperties;
    private List<Object> relations;
    private Map<String, Object> artifactRelations;
    private String artifactStatus;
}
