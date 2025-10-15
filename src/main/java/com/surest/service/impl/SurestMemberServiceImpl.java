package com.surest.service.impl;

import com.surest.dto.SurestMemberDto;
import com.surest.exception.ConflictException;
import com.surest.exception.NotFoundException;
import com.surest.mapper.SurestMemberMapper;
import com.surest.model.SurestMember;
import com.surest.repository.SurestMemberRepository;
import com.surest.service.SurestMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class SurestMemberServiceImpl implements SurestMemberService {

    private final SurestMemberRepository repo;
    private final SurestMemberMapper mapper;

    public SurestMemberServiceImpl(SurestMemberRepository repo, SurestMemberMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SurestMemberDto> memberList(String lastName, Pageable pageable) {
        var key = (lastName == null || lastName.isBlank()) ? "" : lastName;

        Page<SurestMemberDto> page = repo.findByLastNameContainsIgnoreCase(key, pageable).map(mapper::toDto);

        log.info("Fetched {} members (totalElements={}, totalPages={}) for filter='{}'", page.getNumberOfElements(), page.getTotalElements(), page.getTotalPages(), key);
        return page;
    }

    @Override
    public Page<SurestMemberDto> getAllMembers(Pageable pageable) {
        Page<SurestMemberDto> page = repo.findAll(pageable).map(mapper::toDto);
        log.info("Fetched {} members (totalElements={}, totalPages={})", page.getNumberOfElements(), page.getTotalElements(), page.getTotalPages());
        return page;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "members", key = "#uuid")
    public SurestMemberDto getMembers(UUID uuid) {
        return repo.findById(uuid).map(m -> {
            log.info("Member found id={}", uuid);
            return mapper.toDto(m);
        }).orElseThrow(() -> {
            log.warn("Member not found id={}", uuid);
            return new NotFoundException("SurestMember not found with ID: " + uuid);
        });
    }

    @Override
    public SurestMemberDto createMembers(SurestMemberDto dto) {

        if (repo.existsByEmailIgnoreCase(dto.getEmail())) {
            log.warn("Create blocked: email already exists '{}'", dto.getEmail());
            throw new ConflictException("Email already exists: " + dto.getEmail());
        }

        SurestMember m = mapper.toEntity(dto);
        m.setCreatedAt(OffsetDateTime.now());
        m.setUpdatedAt(OffsetDateTime.now());
        SurestMember saved = repo.save(m);

        log.info("Member created id={}, email='{}'", saved.getUuid(), saved.getEmail());
        return mapper.toDto(saved);
    }

    @Override
    @CacheEvict(value = "members", key = "#uuid")
    public SurestMemberDto updateMember(UUID uuid, SurestMemberDto dto) {

        var existing = repo.findById(uuid).orElseThrow(() -> {
            log.warn("Update failed: member not found id={}", uuid);
            return new NotFoundException("SurestMember not found with ID: " + uuid);
        });

        if (repo.existsByEmailIgnoreCaseAndUuidNot(dto.getEmail(), uuid)) {
            log.warn("Update blocked: email already in use '{}' for different id", dto.getEmail());
            throw new ConflictException("Email already exists: " + dto.getEmail());
        }

        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setEmail(dto.getEmail());
        existing.setUpdatedAt(OffsetDateTime.now());

        SurestMember saved = repo.save(existing);
        log.info("Member updated id={}, email='{}' (cache evicted for this id)", uuid, saved.getEmail());
        return mapper.toDto(saved);
    }

    @Override
    @CacheEvict(value = "members", key = "#uuid")
    public void deleteMember(UUID uuid) {

        try {
            repo.deleteById(uuid);
            log.info("Member deleted id={} (cache evicted for this id)", uuid);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Delete failed: member not found id={}", uuid);
            throw new NotFoundException("SurestMember not found with ID: " + uuid);
        }
    }
}
