package com.surest.service;

import com.surest.dto.MemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MemberService {
    Page<MemberDto> list(String firstName, String lastName, Pageable pageable);
    MemberDto get(UUID id);
    MemberDto create(MemberDto dto);
    MemberDto update(UUID id, MemberDto dto);
    void delete(UUID id);
}
