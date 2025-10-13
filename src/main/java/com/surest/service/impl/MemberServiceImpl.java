package com.surest.service.impl;

import com.surest.dto.MemberDto;
import com.surest.mapper.MemberMapper;
import com.surest.model.Member;
import com.surest.repository.MemberRepository;
import com.surest.service.MemberService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository repo;
    private final MemberMapper mapper;

    public MemberServiceImpl(MemberRepository repo, MemberMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Page<MemberDto> list(String firstName, String lastName, Pageable pageable) {
        if (firstName == null) firstName = "";
        if (lastName == null) lastName = "";
        return repo.findByFirstNameContainsIgnoreCaseAndLastNameContainsIgnoreCase(firstName, lastName, pageable)
                .map(mapper::toDto);
    }

    @Override
    @Cacheable(value = "members", key = "#id")
    public MemberDto get(UUID id) {
        return repo.findById(id).map(mapper::toDto).orElseThrow(() -> new RuntimeException("Member not found"));
    }

    @Override
    public MemberDto create(MemberDto dto) {
        Member m = mapper.toEntity(dto);

        m.setCreatedAt(OffsetDateTime.now());
        m.setUpdatedAt(OffsetDateTime.now());
        Member saved = repo.save(m);
        return mapper.toDto(saved);
    }

    @Override
    @CacheEvict(value = "members", key = "#id")
    public MemberDto update(UUID id, MemberDto dto) {
        Member existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Member not found"));
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setEmail(dto.getEmail());
        existing.setUpdatedAt(OffsetDateTime.now());
        Member saved = repo.save(existing);
        return mapper.toDto(saved);
    }

    @Override
    @CacheEvict(value = "members", key = "#id")
    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
