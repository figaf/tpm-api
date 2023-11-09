package com.figaf.integration.tpm.entity.trading;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProfileDto {

    private Address address;
    private List<ContactPersonDTO> contactPersonList;
    private List<Object> contactChannel;
    private BusinessContext businessContext;
}
