package com.surest.service;

import com.surest.dto.SurestMemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SurestMemberService {
    Page<SurestMemberDto> memberList(String lastName, Pageable pageable);

    Page<SurestMemberDto> getAllMembers(Pageable pageable);

    SurestMemberDto getMembers(UUID uuid);

    SurestMemberDto createMembers(SurestMemberDto dto);

    SurestMemberDto updateMember(UUID uuid, SurestMemberDto dto);

    void deleteMember(UUID uuid);
}
