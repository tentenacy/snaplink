package com.tenacy.snaplink.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UrlCreationRequest {
    @NotBlank(message = "URL is required")
    @URL(message = "Invalid URL format")
    private String originalUrl;

    private String customCode;

    private Integer validityInDays;
}