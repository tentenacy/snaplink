package com.tenacy.snaplink.service;

import com.tenacy.snaplink.api.dto.UrlCreationRequest;
import com.tenacy.snaplink.api.dto.UrlResponse;
import com.tenacy.snaplink.domain.Url;

public interface UrlServiceInterface {

    Url getUrlByShortCode(String shortCode);
    UrlResponse createShortUrl(UrlCreationRequest request);
    Url incrementClickCount(String shortCode);
}
