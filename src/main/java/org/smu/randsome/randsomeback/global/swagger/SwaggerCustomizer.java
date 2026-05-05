package org.smu.randsome.randsomeback.global.swagger;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.smu.randsome.randsomeback.global.support.error.ErrorType;
import org.smu.randsome.randsomeback.global.support.response.ErrorMessage;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class SwaggerCustomizer implements OperationCustomizer {

    private static final String MEDIA_JSON = "application/json";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ApiExceptions apiExceptions = handlerMethod.getMethodAnnotation(ApiExceptions.class);
        if (apiExceptions != null) {
            generateErrorCodeResponseExample(operation, apiExceptions.values());
        }
        return operation;
    }

    private void generateErrorCodeResponseExample(Operation operation, ErrorType[] errorTypes) {
        ApiResponses responses = operation.getResponses();

        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorTypes)
                .map(errorType -> ExampleHolder.builder()
                        .holder(getSwaggerExample(errorType))
                        .name(errorType.name())
                        .code(errorType.getStatus().value())
                        .build()
                )
                .collect(Collectors.groupingBy(ExampleHolder::code));

        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private Example getSwaggerExample(ErrorType errorType) {
        ErrorMessage errorResponse = new ErrorMessage(errorType.getStatus().name(), errorType.getMessage(), null);
        Example example = new Example();
        example.setValue(errorResponse);

        return example;
    }

    private void addExamplesToResponses(
            ApiResponses responses,
            Map<Integer, List<ExampleHolder>> statusWithExampleHolders
    ) {
        statusWithExampleHolders.forEach((status, examples) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse apiResponse = new ApiResponse();

            examples.forEach(exampleHolder -> mediaType.addExamples(
                    exampleHolder.name(),
                    exampleHolder.holder()
            ));
            content.addMediaType(MEDIA_JSON, mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(String.valueOf(status), apiResponse);
        });
    }

}