package com.figaf.integration.tpm.entity.agreement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AgreementCreationRequest {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Version")
    private String version;

    @JsonProperty("OwnerId")
    private String ownerId;

    @JsonProperty("Shared")
    private boolean shared;

    @JsonProperty("TransactionOption")
    private TransactionOption transactionOption;

    @JsonProperty("CompanyData")
    private CompanyData companyData;

    @JsonProperty("TradingPartnerData")
    private TradingPartnerData tradingPartnerData;

    @JsonProperty("TradingPartnerDetails")
    private TradingPartnerDetails tradingPartnerDetails;

    @JsonProperty("ParentId")
    private String parentId;

}
