package com.figaf.integration.tpm.entity.trading;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusinessContext {
    private BusinessProcess businessProcess;
    private BusinessProcessRole businessProcessRole;
    private IndustryClassification industryClassification;
    private ProductClassification productClassification;
    private GeoPolitical geoPolitical;
}
