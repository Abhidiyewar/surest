package com.surest.mapper;

import com.surest.dto.SurestMemberDto;
import com.surest.model.SurestMember;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy  = ReportingPolicy.IGNORE)
public interface SurestMemberMapper {
    SurestMemberDto toDto(SurestMember m);
    SurestMember toEntity(SurestMemberDto d);
}
