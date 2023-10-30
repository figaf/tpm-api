package com.figaf.integration.tpm.entity.company;

import com.figaf.integration.tpm.entity.BaseTpmObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompanyProfile extends BaseTpmObject {

    @Override
    public String getType() {
        return "CLOUD_COMPANY_PROFILE";
    }
}
