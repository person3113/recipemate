package com.recipemate.domain.user.repository;

import com.recipemate.domain.user.entity.PersistentToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * PersistentToken JPA Repository
 * Remember-Me 토큰 DB 관리
 */
public interface PersistentTokenJpaRepository extends JpaRepository<PersistentToken, String> {

    Optional<PersistentToken> findBySeries(String series);

    @Modifying
    @Query("DELETE FROM PersistentToken p WHERE p.username = :username")
    void deleteByUsername(@Param("username") String username);

    @Modifying
    @Query("DELETE FROM PersistentToken p WHERE p.lastUsed < :expiryDate")
    void deleteByLastUsedBefore(@Param("expiryDate") LocalDateTime expiryDate);
}
