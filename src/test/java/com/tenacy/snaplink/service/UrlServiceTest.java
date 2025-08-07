package com.tenacy.snaplink.service;

import com.tenacy.snaplink.api.dto.UrlCreationRequest;
import com.tenacy.snaplink.api.dto.UrlDto;
import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.domain.UrlRepository;
import com.tenacy.snaplink.exception.UrlNotFoundException;
import com.tenacy.snaplink.util.Base62Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UrlServiceTest {
    @Autowired
    @Qualifier("originalUrlService")
    private UrlService urlService;

    @MockitoBean
    private UrlRepository urlRepository;

    @MockitoBean
    private Base62Encoder encoder;

    @MockitoBean
    private CacheManager cacheManager;

    @BeforeEach
    public void setup() {
        // 빈 캐시 반환하도록 설정
        Cache emptyCache = mock(Cache.class);
        when(cacheManager.getCache("urls")).thenReturn(emptyCache);
        when(emptyCache.get(any())).thenReturn(null);
    }

    @Test
    public void testCreateShortUrl() {
        // given
        UrlCreationRequest request = new UrlCreationRequest();
        request.setOriginalUrl("https://example.com");

        when(encoder.encode(anyLong())).thenReturn("abc1234");
        when(urlRepository.existsByShortCode("abc1234")).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenAnswer(i -> i.getArgument(0));

        // when
        UrlDto result = urlService.createShortUrl(request);

        // then
        assertNotNull(result);
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals("abc1234", result.getShortCode());

        verify(urlRepository).save(any(Url.class));
    }

    @Test
    public void testGetUrlByShortCode() {
        // given
        Url url = new Url();
        url.setId(1L);
        url.setOriginalUrl("https://example.com");
        url.setShortCode("abc1234");
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0L);

        when(urlRepository.findActiveByShortCode("abc1234")).thenReturn(Optional.of(url));

        // when
        Url result = urlService.getUrlByShortCode("abc1234");

        // then
        assertNotNull(result);
        assertEquals("https://example.com", result.getOriginalUrl());

        verify(urlRepository).findActiveByShortCode("abc1234");
    }

    @Test
    public void testGetUrlByShortCode_NotFound() {
        // given & when
        when(urlRepository.findActiveByShortCode("notfound")).thenReturn(Optional.empty());

        // then
        assertThrows(UrlNotFoundException.class, () -> {
            urlService.getUrlByShortCode("notfound");
        });

        verify(urlRepository).findActiveByShortCode("notfound");
    }
}