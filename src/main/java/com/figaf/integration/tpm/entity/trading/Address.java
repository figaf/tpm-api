package com.figaf.integration.tpm.entity.trading;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {
    private String cityName;
    private String countryCode;
    private String countrySubdivisionCode;
    private String houseNumber;
    private String pOBox;
    private String pOBoxPostalCode;
    private String streetName;
    private String streetPostalCode;
}
