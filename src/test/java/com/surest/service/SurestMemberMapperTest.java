package com.surest.service;

import com.surest.dto.SurestMemberDto;
import com.surest.mapper.SurestMemberMapper;
import com.surest.model.SurestMember;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SurestMemberMapperTest {

    private final SurestMemberMapper mapper = Mappers.getMapper(SurestMemberMapper.class);

    @Test
    void toDto_mapsSimpleFields() {
        UUID id = UUID.randomUUID();
        SurestMember surestMember = new SurestMember();
        surestMember.setUuid(id);
        surestMember.setFirstName("Rahul");
        surestMember.setLastName("Bhatt");
        surestMember.setEmail("rahul@gmail.com");
        surestMember.setDateOfBirth(LocalDate.of(1997, 10, 13));

        SurestMemberDto memberDto = mapper.toDto(surestMember);

        assertNotNull(memberDto);
        assertEquals(id, memberDto.getUuid());
        assertEquals("Rahul", memberDto.getFirstName());
        assertEquals("Bhatt", memberDto.getLastName());
        assertEquals("rahul@gmail.com", memberDto.getEmail());
        assertEquals(LocalDate.of(1997, 10, 13), memberDto.getDateOfBirth());
    }

    @Test
    void toEntity_mapsSimpleFields() {
        UUID id = UUID.randomUUID();
        SurestMemberDto memberDto = new SurestMemberDto(id, "Rahul", "Bhatt", LocalDate.of(1997, 10, 13), "rahul@gmail.com");

        SurestMember surestMember = mapper.toEntity(memberDto);

        assertNotNull(surestMember);
        assertEquals(id, surestMember.getUuid());
        assertEquals("Rahul", surestMember.getFirstName());
        assertEquals("Bhatt", surestMember.getLastName());
        assertEquals("rahul@gmail.com", surestMember.getEmail());
        assertEquals(LocalDate.of(1997, 10, 13), surestMember.getDateOfBirth());
    }
}
