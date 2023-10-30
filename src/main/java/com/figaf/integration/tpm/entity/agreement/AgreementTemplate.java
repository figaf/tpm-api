package com.figaf.integration.tpm.entity.agreement;

import com.figaf.integration.tpm.entity.BaseTpmObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgreementTemplate extends BaseTpmObject {

    @Override
    public String getType() {
        return "CLOUD_AGREEMENT_TEMPLATE";
    }
}
