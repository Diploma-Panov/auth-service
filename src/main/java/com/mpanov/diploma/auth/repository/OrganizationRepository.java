package com.mpanov.diploma.auth.repository;

import com.mpanov.diploma.auth.model.Organization;
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

    @Query("SELECT o FROM Organization o WHERE o.id IN :ids")
    Page<Organization> findAllByIds(@Param("ids") Set<Long> ids, Pageable pageable);

    Optional<Organization> findOrganizationBySlug(String slug);

    boolean existsOrganizationBySlug(String slug);

}
