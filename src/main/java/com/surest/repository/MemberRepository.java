package com.surest.repository;

import com.surest.model.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
    Page<Member> findByLastNameContainsIgnoreCase( String lastName,Pageable pageable);
}
