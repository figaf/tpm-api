package com.figaf.integration.tpm.entity.integrationadvisory;

import com.figaf.integration.tpm.enumtypes.TpmObjectType;

import java.util.Date;

public interface IntegrationAdvisoryObjectVersion {

    String getName();
    String getObjectId();
    String getVersionId();
    String getVersion();
    String getStatus();
    Date getModifiedDate();
    String getModifiedBy();
    TpmObjectType getObjectType();
    String getImportCorrelationGroupId();
    String getImportCorrelationObjectId();

}
