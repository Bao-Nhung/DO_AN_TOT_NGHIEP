package com.zestia.datn.zestia.repository;

import com.zestia.datn.zestia.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByTokenAndUsedAtIsNull(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT token FROM PasswordResetToken token WHERE token.token = :token AND token.usedAt IS NULL")
    Optional<PasswordResetToken> findByTokenAndUsedAtIsNullForUpdate(@Param("token") String token);

    List<PasswordResetToken> findByAccountTypeAndAccountIdAndUsedAtIsNull(String accountType, Integer accountId);
}
