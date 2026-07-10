package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByTokenAndUsedAtIsNull(String token);

    List<PasswordResetToken> findByAccountTypeAndAccountIdAndUsedAtIsNull(String accountType, Integer accountId);
}
