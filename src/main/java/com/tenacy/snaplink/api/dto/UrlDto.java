package com.tenacy.snaplink.api.dto;

import com.tenacy.snaplink.domain.Url;
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

    public static UrlDto from(Url url, String domain) {
        UrlDto dto = new UrlDto();
        dto.setOriginalUrl(url.getOriginalUrl());
        dto.setShortCode(url.getShortCode());
        dto.setShortUrl(domain + "/" + url.getShortCode());
        dto.setCreatedAt(url.getCreatedAt());
        dto.setExpiresAt(url.getExpiresAt());
        dto.setClickCount(url.getClickCount());
        dto.setCustom(url.isCustom());
        return dto;
    }
}