package com.figaf.integration.tpm.entity.agreement;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AgreementRequestMapper {

    AgreementRequestMapper INSTANCE = Mappers.getMapper(AgreementRequestMapper.class);

    AgreementUpdateRequest toUpdateRequest(AgreementCreationRequest request);
}
