package com.mpanov.diploma.auth.repository;

import com.mpanov.diploma.auth.model.ServiceUser;
import com.mpanov.diploma.auth.model.common.UserSystemRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceUserRepository extends JpaRepository<ServiceUser, Long> {

    Optional<ServiceUser> findByEmail(String email);

    @Modifying
    @Query("update ServiceUsers u set u.systemRole=:newRole where u.id=:userId")
    void changeSystemRoleByUserId(Long userId, UserSystemRole newRole);

    @Modifying
    @Query("update ServiceUsers u set u.lastLoginDate=current_timestamp where u.id=:userId")
    void updateLastLoginDate(Long userId);

}
