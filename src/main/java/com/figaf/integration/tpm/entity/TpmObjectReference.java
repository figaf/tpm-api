package com.figaf.integration.tpm.entity;

import com.figaf.integration.tpm.enumtypes.TpmObjectType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TpmObjectReference {

    private String objectId;
    private TpmObjectType tpmObjectType;
}
