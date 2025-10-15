package com.surest.controller;

import com.surest.dto.SurestMemberDto;
import com.surest.service.SurestMemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/surest")
public class SurestMemberController {

    private final SurestMemberService memberService;

    public SurestMemberController(SurestMemberService svc) {
        this.memberService = svc;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurestMemberDto> createNewMembers(@Valid @RequestBody SurestMemberDto dto) {
        var created = memberService.createMembers(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<SurestMemberDto>> getMembersByLastName(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String lastName
    ) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by("lastName").ascending());
        return ResponseEntity.ok(memberService.memberList(lastName, pageable));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<SurestMemberDto> getMembersByUUID(@PathVariable UUID uuid) {
        return ResponseEntity.ok(memberService.getMembers(uuid));
    }

    @GetMapping("/members")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<SurestMemberDto>> getAllMembers(
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 3);
        return ResponseEntity.ok(memberService.getAllMembers(pageable));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SurestMemberDto> updateExistingMember(@PathVariable UUID uuid, @Valid @RequestBody SurestMemberDto dto) {
        return ResponseEntity.ok(memberService.updateMember(uuid, dto));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMember(@PathVariable UUID uuid) {
        memberService.deleteMember(uuid);
        return ResponseEntity.noContent().build();
    }
}
