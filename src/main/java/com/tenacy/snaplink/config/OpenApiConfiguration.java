package com.tenacy.snaplink.config;

import com.tenacy.snaplink.doc.ApiErrorCodeExample;
import com.tenacy.snaplink.doc.ExampleHolder;
import com.tenacy.snaplink.doc.NoExample;
import com.tenacy.snaplink.exception.BaseErrorCode;
import com.tenacy.snaplink.exception.ErrorReason;
import com.tenacy.snaplink.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.reflections.Reflections;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.annotation.Annotation;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

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

    @Bean
    public ModelConverter noExampleConverter() {
        return (type, context, chain) -> {
            if (chain.hasNext()) {
                Schema schema = chain.next().resolve(type, context, chain);

                if (schema != null && type.getCtxAnnotations() != null) {
                    for (Annotation annotation : type.getCtxAnnotations()) {
                        if (annotation instanceof NoExample) {
                            schema.setExample(null);
                            break;
                        }
                    }
                }

                return schema;
            }
            return null;
        };
    }

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {

            //ApiErrorCodeExample - 에러코드 기본값을 코드 배열을 기준으로 문서화
            ApiErrorCodeExample apiErrorCodeExample = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);

            if (apiErrorCodeExample != null) {

                Reflections reflections = new Reflections(BaseErrorCode.class.getPackageName());
                Set<Class<? extends BaseErrorCode>> errorCodeClasses = reflections.getSubTypesOf(BaseErrorCode.class);

                BaseErrorCode[] errorCodes = Arrays.stream(apiErrorCodeExample.value()).flatMap(code -> {
                    // 구현체 인스턴스 생성 후 메서드 호출
                    return errorCodeClasses.stream().map(errorCodeClass -> {
                                try {
                                    return Arrays.stream(errorCodeClass.getEnumConstants())
                                            .filter(i -> Objects.equals(i.getErrorReason().getCode(), code))
                                            .findAny().orElse(null);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .filter(Objects::nonNull);
                }).toArray(BaseErrorCode[]::new);

                generateErrorCodeResponseExample(operation.getResponses(), errorCodes);
            }

            return operation;
        };
    }

    private void generateErrorCodeResponseExample(ApiResponses responses, BaseErrorCode[] errorCodes) {

        // 400, 401, 404 등 에러코드의 상태코드들로 리스트로 있다.
        // 400 같은 상태코드에 여러 에러코드들이 있을 수 있다.
        Map<Integer, List<ExampleHolder>> statusWithExampleHolders =
                Arrays.stream(errorCodes)
                        .map(
                                errorCode -> {
                                    ErrorReason errorReason = errorCode.getErrorReason();
                                    try {
                                        return ExampleHolder.builder()
                                                .holder(getSwaggerExample(errorCode.getErrorExplanation(), errorCode))
                                                .code(errorReason.getStatus())
                                                .name(errorReason.getCode())
                                                .build();
                                    } catch (NoSuchFieldException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .collect(groupingBy(ExampleHolder::getCode));
        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private Example getSwaggerExample(String value, BaseErrorCode errorCode) {
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);
        Example example = new Example();
        example.description(value);
        example.setValue(errorResponse);
        return example;
    }

    private void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach(
                (status, v) -> {
                    Content content = new Content();
                    MediaType mediaType = new MediaType();
                    // 상태 코드마다 ApiResponse을 생성합니다.
                    ApiResponse apiResponse = new ApiResponse();
                    //  List<ExampleHolder> 를 순회하며, mediaType 객체에 예시값을 추가합니다.
                    v.forEach(
                            exampleHolder -> mediaType.addExamples(
                                    exampleHolder.getName(), exampleHolder.getHolder()));
                    // ApiResponse 의 content 에 mediaType을 추가합니다.
                    content.addMediaType("application/json", mediaType);
                    apiResponse.setContent(content);
                    // 상태코드를 key 값으로 responses 에 추가합니다.
                    responses.addApiResponse(status.toString(), apiResponse);
                });
    }
}