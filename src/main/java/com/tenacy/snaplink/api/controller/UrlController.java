package com.tenacy.snaplink.api.controller;

import com.tenacy.snaplink.api.dto.UrlCreationRequest;
import com.tenacy.snaplink.api.dto.UrlDto;
import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "1. 단축 URL API", description = "")
public class UrlController {
    private final UrlService urlService;

    @Value("${app.shorturl.domain}")
    private String domain;

    @PostMapping("/shorten")
    @Operation(summary = "단축 URL 생성", description = "")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "성공"))
    public ResponseEntity<UrlDto> createShortUrl(
            @Valid
            @RequestBody
            @Schema(implementation = UrlCreationRequest.class)
            UrlCreationRequest request
    ) {
        UrlDto urlDto = urlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(urlDto);
    }

    @GetMapping("/urls/{shortCode}")
    @Operation(summary = "단축 URL 정보 조회", description = "")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "성공"))
    public ResponseEntity<UrlDto> getUrlInfo(
            @Parameter(description = "단축 URL 맨 끝에 있는 7자리 코드입니다.")
            @PathVariable String shortCode
    ) {
        Url url = urlService.getUrlByShortCode(shortCode);
        return ResponseEntity.ok(UrlDto.from(url, domain));
    }
}