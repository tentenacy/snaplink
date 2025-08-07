package com.tenacy.snaplink.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UrlDto {
    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long clickCount;
    private boolean custom;
}