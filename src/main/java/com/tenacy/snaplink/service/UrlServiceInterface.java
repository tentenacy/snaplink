package com.tenacy.snaplink.service;

import com.tenacy.snaplink.api.dto.UrlCreationRequest;
import com.tenacy.snaplink.api.dto.UrlDto;
import com.tenacy.snaplink.domain.Url;

public interface UrlServiceInterface {

    Url getUrlByShortCode(String shortCode);
    UrlDto createShortUrl(UrlCreationRequest request);
    Url incrementClickCount(String shortCode);
}
