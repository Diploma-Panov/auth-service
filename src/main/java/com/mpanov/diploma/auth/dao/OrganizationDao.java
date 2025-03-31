package com.mpanov.diploma.auth.dao;

import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.repository.OrganizationMemberRepository;
import com.mpanov.diploma.auth.repository.OrganizationRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class OrganizationDao {

    private final OrganizationRepository organizationRepository;

    private final OrganizationMemberRepository organizationMemberRepository;

    public List<Organization> getAllOrganizationsByMemberUserId(Long memberUserId, Pageable pageable) {
        Set<Long> organizationIds = organizationMemberRepository
                .findAllOrganizationIdsByMemberUserId(memberUserId);
        return organizationRepository.findAllByIds(
                organizationIds,
                pageable
        );
    }

    public int countAllOrganizationsByMemberUserId(Long memberUserId) {
        return organizationMemberRepository.countAllOrganizationIdsByMemberUserId(memberUserId);
    }

}
