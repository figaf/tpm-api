package com.figaf.integration.tpm.entity.trading;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class Product {

    private String name;
    private String title;
    private boolean thirdParty;
    private String parent;
    private Date createdAt;
    private String createdBy;
    private Date modifiedAt;
    private String modifiedBy;

    private String metadataId;
}
