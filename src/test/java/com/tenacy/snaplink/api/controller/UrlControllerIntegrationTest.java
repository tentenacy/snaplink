package com.tenacy.snaplink.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenacy.snaplink.api.dto.UrlCreationRequest;
import com.tenacy.snaplink.api.dto.UrlDto;
import com.tenacy.snaplink.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class UrlControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlService urlService;

    @Test
    public void testCreateShortUrl() throws Exception {
        UrlCreationRequest request = new UrlCreationRequest();
        request.setOriginalUrl("https://example.com");

        UrlDto response = new UrlDto();
        response.setOriginalUrl("https://example.com");
        response.setShortCode("abc1234");
        response.setShortUrl("https://snap.link/abc1234");
        response.setCreatedAt(LocalDateTime.now());
        response.setClickCount(0L);

        when(urlService.createShortUrl(any(UrlCreationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.shortCode").value("abc1234"));
    }

    @Test
    public void testInvalidUrl() throws Exception {
        UrlCreationRequest request = new UrlCreationRequest();
        request.setOriginalUrl("invalid-url");

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}