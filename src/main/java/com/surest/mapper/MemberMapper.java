package com.surest.mapper;

import com.surest.dto.MemberDto;
import com.surest.model.Member;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberDto toDto(Member m);
    Member toEntity(MemberDto d);
}
