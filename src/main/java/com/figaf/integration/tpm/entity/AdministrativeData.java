package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AdministrativeData implements Serializable {

    private Date createdAt;
    private Date modifiedAt;
    private String modifiedBy;
    private String createdBy;
}
