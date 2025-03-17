package com.mpanov.diploma.auth.repository;

import com.mpanov.diploma.auth.model.ServiceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceUserRepository extends JpaRepository<ServiceUser, Long> {

    Optional<ServiceUser> findByEmail(String email);

}
