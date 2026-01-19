package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AgreementTemplateMetadata extends TpmObjectMetadata {

    private String b2bScenarioDetailsId;
    private AdministrativeData b2bScenarioDetailsAdministrativeData;

    public AdministrativeData resolveLatestAdministrativeData() {
        AdministrativeData agreementTemplateAdministrativeData = getAdministrativeData();
        if (b2bScenarioDetailsAdministrativeData == null) {
            return agreementTemplateAdministrativeData;
        }

        return agreementTemplateAdministrativeData.getModifiedAt().after(b2bScenarioDetailsAdministrativeData.getModifiedAt())
            ? agreementTemplateAdministrativeData
            : b2bScenarioDetailsAdministrativeData;
    }

}
