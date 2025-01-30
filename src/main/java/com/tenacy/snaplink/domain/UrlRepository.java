package com.tenacy.snaplink.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    List<Url> findTop100ByOrderByClickCountDesc();
    List<Url> findByExpiresAtBefore(LocalDateTime now);
    boolean existsByShortCode(String shortCode);
}