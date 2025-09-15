package com.figaf.integration.tpm.entity.trading.verbose;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {
    private String cityName;
    private String countryCode;
    private String countrySubdivisionCode;
    private String houseNumber;
    private String poBox;
    private String poBoxPostalCode;
    private String streetName;
    private String streetPostalCode;
}
