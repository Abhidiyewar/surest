package com.surest.service;

import com.surest.dto.SurestMemberDto;
import com.surest.exception.ConflictException;
import com.surest.exception.NotFoundException;
import com.surest.mapper.SurestMemberMapper;
import com.surest.model.SurestMember;
import com.surest.repository.SurestMemberRepository;
import com.surest.service.impl.SurestMemberServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurestMemberServiceImplTest {

    @Mock
    private SurestMemberRepository repo;
    @Mock
    private SurestMemberMapper mapper;

    @InjectMocks
    private SurestMemberServiceImpl memberService;


    private SurestMemberDto dto(UUID id) {
        SurestMemberDto memberDto = new SurestMemberDto();
        memberDto.setUuid(id);
        memberDto.setFirstName("Rahul");
        memberDto.setLastName("Bhatt");
        memberDto.setEmail("rahul@gmail.com");
        memberDto.setDateOfBirth(LocalDate.of(1997, 10, 13));
        return memberDto;
    }

    private SurestMember entity(UUID id) {
        SurestMember surestMember = new SurestMember();
        surestMember.setUuid(id);
        surestMember.setFirstName("Rahul");
        surestMember.setLastName("Bhatt");
        surestMember.setEmail("rahul@gmail.com");
        surestMember.setDateOfBirth(LocalDate.of(1997, 10, 13));
        return surestMember;
    }

    @Test
    void memberList_filtersByLastName_andMapsToDto() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("lastName"));
        SurestMember e1 = entity(UUID.randomUUID());
        when(repo.findByLastNameContainsIgnoreCase("Bhatt", pageable)).thenReturn(new PageImpl<>(List.of(e1), pageable, 1));
        when(mapper.toDto(e1)).thenReturn(dto(e1.getUuid()));

        Page<SurestMemberDto> page = memberService.memberList("Bhatt", pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals("Bhatt", page.getContent().get(0).getLastName());
        verify(repo).findByLastNameContainsIgnoreCase("Bhatt", pageable);
        verify(mapper).toDto(e1);
    }

    @Test
    void memberList_blankLastName_searchesWithEmptyString() {
        Pageable pageable = PageRequest.of(0, 5);
        when(repo.findByLastNameContainsIgnoreCase("", pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<SurestMemberDto> page = memberService.memberList("   ", pageable);

        assertTrue(page.isEmpty());
        verify(repo).findByLastNameContainsIgnoreCase("", pageable);
    }

    @Test
    void getAllMembers_mapsAll() {
        Pageable pageable = PageRequest.of(0, 2);
        SurestMember e1 = entity(UUID.randomUUID());
        SurestMember e2 = entity(UUID.randomUUID());
        when(repo.findAll(pageable)).thenReturn(new PageImpl<>(List.of(e1, e2), pageable, 2));
        when(mapper.toDto(e1)).thenReturn(dto(e1.getUuid()));
        when(mapper.toDto(e2)).thenReturn(dto(e2.getUuid()));

        Page<SurestMemberDto> out = memberService.getAllMembers(pageable);

        assertEquals(2, out.getTotalElements());
        verify(repo).findAll(pageable);
        verify(mapper).toDto(e1);
        verify(mapper).toDto(e2);
    }

    @Test
    void getMembers_returnsDto_whenFound() {
        UUID id = UUID.randomUUID();
        SurestMember e = entity(id);
        when(repo.findById(id)).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(dto(id));

        SurestMemberDto out = memberService.getMembers(id);

        assertEquals(id, out.getUuid());
        verify(repo).findById(id);
        verify(mapper).toDto(e);
    }

    @Test
    void getMembers_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> memberService.getMembers(id));
        assertTrue(ex.getMessage().contains(id.toString()));
        verify(repo).findById(id);
        verifyNoInteractions(mapper);
    }

    @Test
    void createMembers_throwsConflict_whenEmailExists() {
        SurestMemberDto memberDto = dto(null);
        when(repo.existsByEmailIgnoreCase(memberDto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> memberService.createMembers(memberDto));
        verify(repo).existsByEmailIgnoreCase(memberDto.getEmail());
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(mapper);
    }

    @Test
    void createMembers_saves_andReturnsDto_withTimestampsSet() {
        SurestMemberDto in = dto(null);
        SurestMember toSave = entity(null);

        when(repo.existsByEmailIgnoreCase(in.getEmail())).thenReturn(false);
        when(mapper.toEntity(in)).thenReturn(toSave);
        when(repo.save(any(SurestMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any(SurestMember.class))).thenAnswer(inv -> {
            SurestMember m = inv.getArgument(0);
            SurestMemberDto d = dto(m.getUuid());
            d.setEmail(m.getEmail());
            return d;
        });

        SurestMemberDto createdMember = memberService.createMembers(in);

        assertNotNull(createdMember);
        ArgumentCaptor<SurestMember> captor = ArgumentCaptor.forClass(SurestMember.class);
        verify(repo).save(captor.capture());
        SurestMember persisted = captor.getValue();
        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());
        assertEquals(in.getEmail(), persisted.getEmail());

        verify(repo).existsByEmailIgnoreCase(in.getEmail());
        verify(mapper).toEntity(in);
        verify(mapper).toDto(persisted);
    }

    @Test
    void updateMember_updatesFields_andEvictsCache() {
        UUID id = UUID.randomUUID();
        SurestMember existing = entity(id);
        SurestMemberDto memberDto = dto(id);
        memberDto.setFirstName("Abhishek");
        memberDto.setLastName("Diyewar");
        memberDto.setEmail("abhi@example.com");

        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.existsByEmailIgnoreCaseAndUuidNot(memberDto.getEmail(), id)).thenReturn(false);
        when(repo.save(existing)).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(existing)).thenReturn(memberDto);

        SurestMemberDto updatedMember = memberService.updateMember(id, memberDto);

        assertEquals("Abhishek", updatedMember.getFirstName());
        assertEquals("Diyewar", updatedMember.getLastName());
        assertEquals("abhi@example.com", updatedMember.getEmail());
        verify(repo).findById(id);
        verify(repo).existsByEmailIgnoreCaseAndUuidNot(memberDto.getEmail(), id);
        verify(repo).save(existing);
        verify(mapper).toDto(existing);
    }

    @Test
    void updateMember_throwsConflict_whenEmailTakenByAnother() {
        UUID id = UUID.randomUUID();
        SurestMember existing = entity(id);
        SurestMemberDto memberDto = dto(id);
        memberDto.setEmail("taken@example.com");

        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.existsByEmailIgnoreCaseAndUuidNot("taken@example.com", id)).thenReturn(true);

        assertThrows(ConflictException.class, () -> memberService.updateMember(id, memberDto));
        verify(repo).findById(id);
        verify(repo).existsByEmailIgnoreCaseAndUuidNot("taken@example.com", id);
        verify(repo, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void updateMember_throwsNotFound_whenIdMissing() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> memberService.updateMember(id, dto(id)));
        verify(repo).findById(id);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(mapper);
    }

    @Test
    void deleteMember_deletes_whenExists() {
        UUID id = UUID.randomUUID();

        assertDoesNotThrow(() -> memberService.deleteMember(id));
        verify(repo).deleteById(id);
    }

    @Test
    void deleteMember_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        doThrow(new EmptyResultDataAccessException(1)).when(repo).deleteById(id);

        assertThrows(NotFoundException.class, () -> memberService.deleteMember(id));
        verify(repo).deleteById(id);
    }
}
