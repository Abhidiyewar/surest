package com.surest.mapper;

import com.surest.dto.MemberDto;
import com.surest.model.Member;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy  = ReportingPolicy.IGNORE)
public interface MemberMapper {
    MemberDto toDto(Member m);
    Member toEntity(MemberDto d);
}
