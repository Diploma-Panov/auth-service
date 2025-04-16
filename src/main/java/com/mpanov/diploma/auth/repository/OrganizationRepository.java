package com.mpanov.diploma.auth.repository;

import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.data.OrganizationScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @Query("SELECT o FROM Organization o WHERE o.id IN :ids AND o.organizationScope=:scope")
    Page<Organization> findAllByIdsAndOrganizationScope(@Param("ids") Set<Long> ids, OrganizationScope scope, Pageable pageable);

    Optional<Organization> findOrganizationBySlug(String slug);

    boolean existsOrganizationBySlug(String slug);

}
