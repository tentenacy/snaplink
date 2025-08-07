package com.tenacy.snaplink.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    List<Url> findByExpiresAtBefore(LocalDateTime now);
    boolean existsByShortCode(String shortCode);

    @Query("SELECT u FROM Url u WHERE u.shortCode = :shortCode AND (u.expiresAt IS NULL OR u.expiresAt > CURRENT_TIMESTAMP)")
    Optional<Url> findActiveByShortCode(@Param("shortCode") String shortCode);

    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);

    @Query(value = "SELECT * FROM urls u WHERE u.click_count > :minClicks ORDER BY u.click_count DESC LIMIT 100", nativeQuery = true)
    List<Url> findTopUrlsByClickCount(@Param("minClicks") long minClicks);
}