package com.figaf.integration.tpm.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class CreateBusinessEntityRequest {

    @JsonProperty("artifactType")
    private final String artifactType;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("ShortName")
    private String shortName;

    @JsonProperty("WebURL")
    private String webURL;

    @JsonProperty("LogoId")
    private String logoId;

    @JsonProperty("EmailAddress")
    private String emailAddress;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("Profile")
    private Profile profile = new Profile();

    @Getter
    @Setter
    @ToString
    public static class Profile {

        @JsonProperty("Address")
        private Address address = new Address();

        @JsonProperty("ContactPersonList")
        private List<ContactPerson> contactPersonList = new ArrayList<>();

        @JsonProperty("IdentifierList")
        private List<Identifier> identifierList = new ArrayList<>();

        @JsonProperty("ContactChannel")
        private List<ContactChannel> contactChannel = new ArrayList<>();

        @JsonProperty("BusinessContext")
        private BusinessContext businessContext = new BusinessContext();

        @JsonProperty("PartnerType")
        private String partnerType;

    }

    @Getter
    @Setter
    @ToString
    public static class Address {

        @JsonProperty("CityName")
        private String cityName;

        @JsonProperty("CountryCode")
        private String countryCode;

        @JsonProperty("CountrySubdivisionCode")
        private String countrySubdivisionCode;

        @JsonProperty("HouseNumber")
        private String houseNumber;

        @JsonProperty("POBox")
        private String poBox;

        @JsonProperty("POBoxPostalCode")
        private String poBoxPostalCode;

        @JsonProperty("StreetName")
        private String streetName;

        @JsonProperty("StreetPostalCode")
        private String streetPostalCode;

    }

    @Getter
    @Setter
    public static class ContactPerson {

    }

    @Getter
    @Setter
    public static class Identifier {

    }

    @Getter
    @Setter
    public static class ContactChannel {

    }

    @Getter
    @Setter
    @ToString
    public static class BusinessContext {

        @JsonProperty("BusinessProcessRole")
        private BusinessProcessRole businessProcessRole = new BusinessProcessRole();

        @JsonProperty("IndustryClassification")
        private IndustryClassification industryClassification = new  IndustryClassification();

        @JsonProperty("ProductClassification")
        private ProductClassification productClassification =  new  ProductClassification();

        @JsonProperty("GeoPolitical")
        private GeoPolitical geoPolitical = new GeoPolitical();
    }

    @Getter
    @Setter
    @ToString
    public static class BusinessProcessRole {

        @JsonProperty("BusinessProcessRoleCodeList")
        private List<String> businessProcessRoleCodeList = new ArrayList<>();

        @JsonProperty("IndustryClassificationCodeList")
        private List<String> industryClassificationCodeList = new ArrayList<>();

        @JsonProperty("ProductClassificationCodeList")
        private List<String> productClassificationCodeList = new ArrayList<>();

    }

    @Getter
    @Setter
    @ToString
    public static class IndustryClassification {

        @JsonProperty("IndustryClassificationCodeList")
        private List<String> industryClassificationCodeList = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class ProductClassification {

        @JsonProperty("ProductClassificationCodeList")
        private List<String> productClassificationCodeList = new ArrayList<>();
    }

    @Getter
    @Setter
    @ToString
    public static class GeoPolitical {

        @JsonProperty("CountryInfoList")
        private List<String> countryInfoList = new ArrayList<>();
    }

}
