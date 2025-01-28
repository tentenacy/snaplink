package com.tenacy.snaplink.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
@Data
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1024)
    private String originalUrl;

    @Column(nullable = false, unique = true, length = 7)
    private String shortCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Long clickCount;

    @Column(nullable = false)
    private boolean custom;
}