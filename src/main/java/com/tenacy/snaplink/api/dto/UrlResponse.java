package com.tenacy.snaplink.api.dto;

import com.tenacy.snaplink.domain.Url;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import static com.tenacy.snaplink.util.DocumentationDescriptions.*;

@Data
@Schema(description = "단축 URL 응답 DTO")
public class UrlResponse {
    @Schema(description = RESPONSE_ORIGINAL_URL, example = EXAMPLE_ORIGINAL_URL)
    private String originalUrl;
    @Schema(description = RESPONSE_SHORT_CODE, example = EXAMPLE_SHORT_CODE)
    private String shortCode;
    @Schema(description = RESPONSE_SHORT_URL, example = EXAMPLE_SHORT_URL)
    private String shortUrl;
    @Schema(description = RESPONSE_CREATED_AT, example = EXAMPLE_CREATED_AT)
    private LocalDateTime createdAt;
    @Schema(description = RESPONSE_EXPIRES_AT, example = EXAMPLE_EXPIRES_AT)
    private LocalDateTime expiresAt;
    @Schema(description = RESPONSE_CLICK_COUNT, example = EXAMPLE_CLICK_COUNT)
    private Long clickCount;
    @Schema(description = RESPONSE_CUSTOM, example = EXAMPLE_CUSTOM)
    private boolean custom;

    public static UrlResponse from(Url url, String domain) {
        UrlResponse dto = new UrlResponse();
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