package com.tenacy.snaplink.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OpenApiConfiguration implements WebMvcConfigurer {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("스냅링크 API")
                .version("v0.0.1")
                .description("");
        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}