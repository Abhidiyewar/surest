package com.surest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surest.dto.SurestMemberDto;
import com.surest.service.SurestMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SurestMemberController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.surest.security.jwt.JwtAuthFilter.class
        )
)
@AutoConfigureMockMvc
class SurestMemberControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    SurestMemberService memberService;

    private static SurestMemberDto dto(UUID uuid, String frName, String ltName, String email) {
        return new SurestMemberDto(uuid, frName, ltName, LocalDate.of(1997, 10, 13), email);
    }

    private String createJson(String frName, String ltName, String email) throws Exception {
        var n = objectMapper.createObjectNode();
        n.put("firstName", frName);
        n.put("lastName", ltName);
        n.put("email", email);
        n.put("dateOfBirth", "1997-10-13");
        return objectMapper.writeValueAsString(n);
    }

    /**
     * POST /api/surest
     */
    @ParameterizedTest(name = "Create member -> 201 for {0} {1}")
    @CsvSource({
            "Rahul,Bhatt,rahul.bhatt@gmail.com",
            "Ananya,Sharma,ananya.sharma@gmail.com",
            "Vikram,Mehta,vikram.mehta@gmail.com"
    })
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/surest -> 201 Created (ADMIN) [parametrized]")
    void createNewMembers_created201(String first, String last, String email) throws Exception {
        UUID id = UUID.randomUUID();
        var created = dto(id, first, last, email);
        given(memberService.createMembers(any(SurestMemberDto.class))).willReturn(created);

        mockMvc.perform(post("/api/surest")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(first, last, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value(first))
                .andExpect(jsonPath("$.lastName").value(last));

        verify(memberService).createMembers(any(SurestMemberDto.class));
    }

    /**
     * GET /api/surest (by lastName, pageable = page, size=5, sort lastName ASC)
     */
    @ParameterizedTest(name = "List members by lastName={0}, page={1}")
    @MethodSource("lastNameAndPageProvider")
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/surest -> 200 OK, correct pageable used [parametrized]")
    void getMembersByLastName_ok(String lastName, int pageIndex) throws Exception {
        UUID id = UUID.randomUUID();
        var page = new PageImpl<>(
                List.of(dto(id, "Abhi", lastName, "abhi+" + lastName.toLowerCase() + "@gmail.com")),
                PageRequest.of(pageIndex, 5, Sort.by("lastName").ascending()),
                1
        );
        given(memberService.memberList(any(), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/surest")
                        .param("page", String.valueOf(pageIndex))
                        .param("lastName", lastName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uuid").value(id.toString()));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(memberService).memberList(eq(lastName), captor.capture());
        Pageable p = captor.getValue();
        assertThat(p.getPageNumber()).isEqualTo(pageIndex);
        assertThat(p.getPageSize()).isEqualTo(5);
        assertThat(Objects.requireNonNull(p.getSort().getOrderFor("lastName")).getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    static Stream<Arguments> lastNameAndPageProvider() {
        return Stream.of(
                Arguments.of("War", 0),
                Arguments.of("Diy", 1),
                Arguments.of("Khan", 2)
        );
    }

    /**
     * GET /api/surest/{uuid}
     */
    @ParameterizedTest(name = "Get member by id -> {0}")
    @MethodSource("uuidAndNameProvider")
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/surest/{uuid} -> 200 OK [parametrized]")
    void getMembersByUUID_ok(UUID id, String first, String last, String email) throws Exception {
        given(memberService.getMembers(id)).willReturn(dto(id, first, last, email));

        mockMvc.perform(get("/api/surest/{uuid}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(id.toString()));

        verify(memberService).getMembers(id);
    }

    static Stream<Arguments> uuidAndNameProvider() {
        return Stream.of(
                Arguments.of(UUID.randomUUID(), "Abhi", "War", "abhi.war@gmail.com"),
                Arguments.of(UUID.randomUUID(), "Neha", "Patel", "neha.patel@gmail.com"),
                Arguments.of(UUID.randomUUID(), "Rohan", "Verma", "rohan.verma@gmail.com")
        );
    }

    /**
     * GET /api/surest/members (default page size = 3)
     */
    @ParameterizedTest(name = "Get all members page={0} (expect size=3)")
    @ValueSource(ints = {0, 1, 2})
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/surest/members -> 200 OK (page size 3) [parametrized]")
    void getAllMembersTest(int pageIndex) throws Exception {
        UUID id = UUID.randomUUID();
        var page = new PageImpl<>(
                List.of(dto(id, "Ahi", "Diy", "ahi.diy@gmail.com")),
                PageRequest.of(pageIndex, 3),
                1
        );
        given(memberService.getAllMembers(any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/surest/members").param("page", String.valueOf(pageIndex)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].uuid").value(id.toString()));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(memberService).getAllMembers(captor.capture());
        Pageable p = captor.getValue();
        assertThat(p.getPageNumber()).isEqualTo(pageIndex);
        assertThat(p.getPageSize()).isEqualTo(3);
    }

    /**
     * PUT /api/surest/{uuid}
     */
    @ParameterizedTest(name = "Update member {0} -> firstName={1}, lastName={2}")
    @MethodSource("updatePayloadProvider")
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/surest/{uuid} -> 200 OK (ADMIN) [parametrized]")
    void updateExistingMemberTest(UUID id, String reqFirst, String reqLast, String email, String returnedFirst) throws Exception {
        var updated = dto(id, returnedFirst, reqLast, email);
        given(memberService.updateMember(eq(id), any(SurestMemberDto.class))).willReturn(updated);

        mockMvc.perform(put("/api/surest/{uuid}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(reqFirst, reqLast, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value(returnedFirst));

        verify(memberService).updateMember(eq(id), any(SurestMemberDto.class));
    }

    static Stream<Arguments> updatePayloadProvider() {
        return Stream.of(
                Arguments.of(UUID.randomUUID(), "John", "Jacobs", "john.jacobs@gmail.com", "John"),
                Arguments.of(UUID.randomUUID(), "Aarav", "Iyer", "aarav.iyer@gmail.com", "Aarav"),
                Arguments.of(UUID.randomUUID(), "Sara", "Ali", "sara.ali@gmail.com", "Sara")
        );
    }

    /**
     * DELETE /api/surest/{uuid}
     */
    @ParameterizedTest(name = "Delete member id -> {0}")
    @MethodSource("uuidBatch")
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/surest/{uuid} -> 204 No Content (ADMIN) [parametrized]")
    void deleteMemberTest(UUID id) throws Exception {
        mockMvc.perform(delete("/api/surest/{uuid}", id).with(csrf()))
                .andExpect(status().isNoContent());

        verify(memberService).deleteMember(id);
    }

    static Stream<UUID> uuidBatch() {
        return IntStream.range(0, 3).mapToObj(i -> UUID.randomUUID());
    }
}
