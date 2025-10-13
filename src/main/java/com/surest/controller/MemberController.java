package com.surest.controller;

import com.surest.dto.MemberDto;
import com.surest.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/surest")
public class MemberController {

    private final MemberService svc;

    public MemberController(MemberService svc) {
        this.svc = svc;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<MemberDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort,
            @RequestParam(required = false) String lastName
    ) {
        String[] sp = sort.split(",");
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sp[1]), sp[0]));
        return ResponseEntity.ok(svc.list(lastName, pageable));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<MemberDto> get(@PathVariable UUID uuid) {
        return ResponseEntity.ok(svc.get(uuid));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberDto> create( @RequestBody MemberDto dto) {
        var created = svc.create(dto);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberDto> update(@PathVariable UUID uuid, @RequestBody MemberDto dto) {
        return ResponseEntity.ok(svc.update(uuid, dto));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        svc.delete(uuid);
        return ResponseEntity.noContent().build();
    }
}
