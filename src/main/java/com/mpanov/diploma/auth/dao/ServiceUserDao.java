package com.mpanov.diploma.auth.dao;

import com.mpanov.diploma.auth.exception.common.NotFoundException;
import com.mpanov.diploma.auth.model.Organization;
import com.mpanov.diploma.auth.model.OrganizationMember;
import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.model.common.UserSystemRole;
import com.mpanov.diploma.auth.repository.ServiceUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public ServiceUser getServiceUserByEmailThrowable(String email) {
        return serviceUserRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ServiceUser.class, "email", email));
    }

    public ServiceUser getServiceUserByIdThrowable(Long id) {
        return serviceUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ServiceUser.class, "id", String.valueOf(id)));
    }

    public Optional<ServiceUser> getServiceUserByEmailOptional(String email) {
        return serviceUserRepository.findByEmail(email);
    }

    public void updateUserSystemRole(Long id, UserSystemRole type) {
        serviceUserRepository.changeSystemRoleByUserId(id, type);
    }

    public void updateLoginDate(Long userId) {
        serviceUserRepository.updateLastLoginDate(userId);
    }

    public ServiceUser updateWithProfilePictureUrl(ServiceUser user, String url) {
        user.setProfilePictureUrl(url);
        return serviceUserRepository.save(user);
    }

    public ServiceUser syncInfo(ServiceUser user) {
        return serviceUserRepository.save(user);
    }

}
