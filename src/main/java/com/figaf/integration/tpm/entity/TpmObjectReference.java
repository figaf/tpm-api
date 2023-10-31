package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TpmObjectReference {

    private String companyProfileId;
    private String tradingPartnerId;
    private String agreementTemplateId;
}
