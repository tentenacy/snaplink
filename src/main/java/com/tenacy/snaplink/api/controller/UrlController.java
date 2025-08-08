package com.tenacy.snaplink.api.controller;

import com.tenacy.snaplink.api.dto.UrlCreationRequest;
import com.tenacy.snaplink.api.dto.UrlResponse;
import com.tenacy.snaplink.doc.ApiErrorCodeExample;
import com.tenacy.snaplink.domain.Url;
import com.tenacy.snaplink.exception.CommonErrorCode;
import com.tenacy.snaplink.service.UrlService;
import com.tenacy.snaplink.util.DocumentationDescriptions;
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
@Tag(name = "1. 단축 URL API", description = DocumentationDescriptions.TAG_URL_API)
public class UrlController {
    private final UrlService urlService;

    @Value("${app.shorturl.domain}")
    private String domain;

    @PostMapping("/shorten")
    @Operation(summary = "단축 URL 생성", description = DocumentationDescriptions.OPERATION_CREATE_SHORT_URL)
    @ApiResponses(@ApiResponse(responseCode = "201", description = "성공"))
    @ApiErrorCodeExample(CommonErrorCode._SHORT_CODE_DUPLICATED)
    public ResponseEntity<UrlResponse> createShortUrl(
            @Valid
            @RequestBody
            @Schema(implementation = UrlCreationRequest.class)
            UrlCreationRequest request
    ) {
        UrlResponse urlResponse = urlService.createShortUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(urlResponse);
    }

    @GetMapping("/urls/{shortCode}")
    @Operation(summary = "단축 URL 정보 조회", description = DocumentationDescriptions.OPERATION_GET_URL_INFO)
    @ApiResponses(@ApiResponse(responseCode = "200", description = "성공"))
    @ApiErrorCodeExample(CommonErrorCode._URL_NOT_FOUND)
    public ResponseEntity<UrlResponse> getUrlInfo(
            @Parameter(description = DocumentationDescriptions.PARAM_SHORT_CODE)
            @PathVariable String shortCode
    ) {
        Url url = urlService.getUrlByShortCode(shortCode);
        return ResponseEntity.ok(UrlResponse.from(url, domain));
    }
}