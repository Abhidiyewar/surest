package com.surest.repository;

import com.surest.model.SurestMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SurestMemberRepository extends JpaRepository<SurestMember, UUID> {
    Page<SurestMember> findByLastNameContainsIgnoreCase(String lastName, Pageable pageable);
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndUuidNot(String email, UUID uuid);
}
