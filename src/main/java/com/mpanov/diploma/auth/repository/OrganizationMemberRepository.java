package com.mpanov.diploma.auth.repository;

import com.mpanov.diploma.auth.model.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    @Query("SELECT om.organization.id FROM OrganizationMember om WHERE om.memberUser.id = :memberUserId")
    Set<Long> findAllOrganizationIdsByMemberUserId(@Param("memberUserId") Long memberUserId);

    @Query("SELECT COUNT(*) > 0 FROM OrganizationMember om WHERE om.memberUser.id = ?1 AND om.organization.slug = ?2")
    boolean existsByMemberUserIdAndOrganizationSlug(Long memberUserId, String organizationSlug);

    int countAllOrganizationIdsByMemberUserId(Long memberUserId);

}
