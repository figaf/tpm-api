package com.figaf.integration.tpm.entity;

import com.figaf.integration.tpm.entity.trading.verbose.ProfileDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Company extends TpmObjectMetadata {

    private ProfileDto profile;
}
