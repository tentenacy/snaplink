package com.tenacy.snaplink.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Schema(description = "단축 URL 생성 요청 DTO")
public class UrlCreationRequest {
    @NotBlank(message = "URL is required")
    @URL(message = "Invalid URL format")
    @Schema(description = "단축하길 원하는 URL입니다.")
    private String originalUrl;

    @Schema(description = "사용자 정의 코드입니다. 자동으로 생성된 코드가 아닌 이 코드를 이용하여 URL이 생성됩니다.")
    private String customCode;

    @Schema(description = "만료 기간입니다. 단위는 일입니다.")
    private Integer validityInDays;
}