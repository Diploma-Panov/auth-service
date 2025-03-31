package com.mpanov.diploma.auth.dao;

import com.mpanov.diploma.auth.exception.common.NotFoundException;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.model.common.UserSystemRole;
import com.mpanov.diploma.auth.repository.ServiceUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ServiceUserDao {

    private ServiceUserRepository serviceUserRepository;

    public ServiceUser createServiceUser(
            ServiceUser newUser,
            Organization permanentOrganization,
            OrganizationMember member
    ) {
        newUser.addOrganizationMember(member);
        newUser.addOrganization(permanentOrganization);
        permanentOrganization.addMember(member);
        return serviceUserRepository.save(newUser);
    }

    public ServiceUser findServiceUserByEmailThrowable(String email) {
        return serviceUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ServiceUser.class, "email", email));
    }

    public ServiceUser findServiceUserByIdThrowable(Long id) {
        return serviceUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ServiceUser.class, "id", String.valueOf(id)));
    }

    public void updateUserSystemRole(Long id, UserSystemRole type) {
        serviceUserRepository.changeSystemRoleByUserId(id, type);
    }

    public void updateLoginDate(Long userId) {
        serviceUserRepository.updateLastLoginDate(userId);
    }

}
