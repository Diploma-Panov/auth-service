package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.auth.dao.OrganizationMemberDao;
import com.mpanov.diploma.auth.model.OrganizationMember;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class OrganizationMembersService {

    private final OrganizationMemberDao organizationMemberDao;

    public List<OrganizationMember> getOrganizationMembersBySlug(String slug, Pageable pageable) {
        log.info("getOrganizationMembersBySlug: slug={}, pageable={}", slug, pageable);
        Page<OrganizationMember> members = organizationMemberDao.getOrganizationMembersBySlugPageable(slug, pageable);
        return members.getContent();
    }

    public int countOrganizationMembersBySlug(String slug) {
        log.info("countOrganizationMembersBySlug: for slug={}", slug);
        return organizationMemberDao.countOrganizationMembersBySlug(slug);
    }

}
