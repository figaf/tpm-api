package com.figaf.integration.tpm.entity.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.figaf.integration.tpm.entity.AdministrativeData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AgreementUpdateRequest extends AgreementCreationRequest {

    @JsonProperty("B2BScenarioDetailsId")
    private String b2bScenarioDetailsId;

    private AdministrativeData administrativeData;
    private String uniqueId;
    private String id;
    private String displayName;

}
