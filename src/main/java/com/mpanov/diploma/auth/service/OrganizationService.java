package com.mpanov.diploma.auth.service;

import com.mpanov.diploma.auth.dao.OrganizationDao;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.ServiceUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class OrganizationService {

    private final OrganizationDao organizationDao;

    public List<Organization> getUserOrganizations(ServiceUser user, Pageable pageable) {
        Long userId = user.getId();
        log.info("getUserOrganizations: for userId={}", userId);
        return organizationDao.getAllOrganizationsByMemberUserId(
                user.getId(), pageable
        );
    }

    public int countUserOrganizations(ServiceUser user) {
        Long userId = user.getId();
        log.info("countUserOrganizations: for userId={}", userId);
        return organizationDao.countAllOrganizationsByMemberUserId(userId);
    }

}
