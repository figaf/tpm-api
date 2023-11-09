package com.figaf.integration.tpm.entity.trading;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContactChannelDTO {

    private String communicationNumber;
    private String internationalCallingCode;
    private boolean isPreferred;
    private String typeCode;
}
