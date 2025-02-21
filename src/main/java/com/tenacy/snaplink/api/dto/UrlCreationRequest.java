package com.tenacy.snaplink.api.dto;

import com.tenacy.snaplink.doc.NoExample;
import com.tenacy.snaplink.util.DocumentationDescriptions;
import com.tenacy.snaplink.util.ValidationMessages;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Schema(description = "단축 URL 생성 요청 DTO")
public class UrlCreationRequest {
    @NotBlank
    @URL(message = ValidationMessages.INVALID_URL_FORMAT)
    @Schema(description = DocumentationDescriptions.REQUEST_ORIGINAL_URL, example = DocumentationDescriptions.EXAMPLE_IN_ORIGINAL_URL)
    private String originalUrl;

    @NoExample
    @Schema(description = DocumentationDescriptions.REQUEST_CUSTOM_CODE)
    private String customCode;

    @Schema(description = DocumentationDescriptions.REQUEST_VALIDITY_IN_DAYS, example = DocumentationDescriptions.EXAMPLE_IN_VALIDITY_IN_DAYS)
    private Integer validityInDays;
}