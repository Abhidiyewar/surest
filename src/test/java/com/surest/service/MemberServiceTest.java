package com.surest.service;

import com.surest.dto.MemberDto;
import com.surest.mapper.MemberMapper;
import com.surest.model.Member;
import com.surest.repository.MemberRepository;
import com.surest.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

 class MemberServiceTest {

    @Test
    void testGetMember() {
        MemberRepository repo = Mockito.mock(MemberRepository.class);
        MemberMapper mapper = Mockito.mock(MemberMapper.class);
        MemberServiceImpl svc = new MemberServiceImpl(repo, mapper);

        UUID id = UUID.randomUUID();
        Member m = Member.builder()
                .uuid(id)
                .firstName("A")
                .lastName("B")

                .dateOfBirth(LocalDate.of(1990,1,1))
                .email("a@b.com")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(repo.findById(id)).thenReturn(Optional.of(m));
        when(mapper.toDto(m)).thenReturn(MemberDto.builder().id(id).firstName("A").lastName("B").dateOfBirth(LocalDate.of(1990,1,1)).email("a@b.com").build());

        var dto = svc.get(id);
        assertEquals(id, dto.getId());
    }
}
