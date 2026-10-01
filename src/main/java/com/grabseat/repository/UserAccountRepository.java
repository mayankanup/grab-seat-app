package com.grabseat.repository;

import com.grabseat.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByLogin(String login);
    Optional<UserAccount> findByEmail(String email);
}
