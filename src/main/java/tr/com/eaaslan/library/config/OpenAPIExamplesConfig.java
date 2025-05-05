package tr.com.eaaslan.library.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import tr.com.eaaslan.library.util.ExampleProviders;

import java.util.Map;

@Configuration
public class OpenAPIExamplesConfig {

    @Bean
    public OperationCustomizer customizeOpenAPIDocumentation() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            String methodName = handlerMethod.getMethod().getName();

            // Handle request body examples
            if (methodName.equals("login")) {
                addLoginRequestExamples(operation);
            } else if (methodName.equals("createBook")) {
                // We can add other request examples here
            }

            // Handle response examples
            if (methodName.equals("login")) {
                addLoginResponses(operation);
            }
//            } else if (methodName.equals("register")) {
//                addRegistrationResponses(operation);
//            } else if (methodName.contains("create") || methodName.contains("add")) {
//                // Add standard creation responses
//                addCreationResponses(operation);
//            } else if (methodName.contains("getById") || methodName.contains("findById")) {
//                // Add standard get-by-id responses
//                addGetByIdResponses(operation);
//            }

            return operation;
        };
    }

    private void addLoginRequestExamples(Operation operation) {
        // Get or create request body and content
        RequestBody requestBody = operation.getRequestBody();
        if (requestBody == null) {
            requestBody = new RequestBody();
            operation.setRequestBody(requestBody);
        }

        Content content = requestBody.getContent();
        if (content == null) {
            content = new Content();
            requestBody.setContent(content);
        }

        // Add examples to application/json media type
        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = new MediaType();
            content.addMediaType("application/json", mediaType);
        }

        // Add the examples
        Map<String, Example> examples = Map.of(
                "Admin Login", createExample("Login with admin credentials", ExampleProviders.ADMIN_LOGIN_REQUEST_EXAMPLE),
                "Librarian Login", createExample("Login with librarian credentials", ExampleProviders.LIBRARIAN_LOGIN_REQUEST_EXAMPLE),
                "Patron Login", createExample("Login with patron credentials", ExampleProviders.PATRON_LOGIN_REQUEST_EXAMPLE)
        );

        mediaType.setExamples(examples);
    }

    private void addLoginResponses(Operation operation) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        // Success response (200)
        ApiResponse successResponse = new ApiResponse()
                .description("Authentication successful")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/JwtAuthResponse"))
                                .examples(Map.of("Default", createExample(null, ExampleProviders.LOGIN_SUCCESS_EXAMPLE)))
                        )
                );
        responses.addApiResponse("200", successResponse);

        // Unauthorized response (401)
        ApiResponse unauthorizedResponse = new ApiResponse()
                .description("Authentication failed")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(Map.of("Default", createExample(null, ExampleProviders.AUTH_FAILURE_EXAMPLE)))
                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                        )
                );
        responses.addApiResponse("401", unauthorizedResponse);
    }

//    private void addRegistrationResponses(Operation operation) {
//        ApiResponses responses = operation.getResponses();
//        if (responses == null) {
//            responses = new ApiResponses();
//            operation.setResponses(responses);
//        }
//
//        // Success response (201)
//        ApiResponse successResponse = new ApiResponse()
//                .description("User successfully registered")
//                .content(new Content()
//                        .addMediaType("application/json", new MediaType()
//                                .schema(new Schema<>().$ref("#/components/schemas/UserResponse"))
//                        )
//                );
//        responses.addApiResponse("201", successResponse);
//
//        // Bad request response (400)
//        ApiResponse badRequestResponse = new ApiResponse()
//                .description("Validation error")
//                .content(new Content()
//                        .addMediaType("application/json", new MediaType()
//                                .addExample("Default", createExample(null, ExampleProviders.VALIDATION_ERROR_EXAMPLE))
//                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
//                        )
//                );
//        responses.addApiResponse("400", badRequestResponse);
//
//        // Conflict response (409)
//        ApiResponse conflictResponse = new ApiResponse()
//                .description("Email already exists")
//                .content(new Content()
//                        .addMediaType("application/json", new MediaType()
//                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
//                        )
//                );
//        responses.addApiResponse("409", conflictResponse);
//    }

//    private void addCreationResponses(Operation operation) {
//        ApiResponses responses = operation.getResponses();
//        if (responses == null) {
//            responses = new ApiResponses();
//            operation.setResponses(responses);
//        }
//
//        // Success response (201)
//        ApiResponse successResponse = new ApiResponse()
//                .description("Resource successfully created");
//        responses.addApiResponse("201", successResponse);
//
//        // Bad request response (400)
//        ApiResponse badRequestResponse = new ApiResponse()
//                .description("Validation error")
//                .content(new Content()
//                        .addMediaType("application/json", new MediaType()
//                                .addExample("Default", createExample(null, ExampleProviders.VALIDATION_ERROR_EXAMPLE))
//                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
//                        )
//                );
//        responses.addApiResponse("400", badRequestResponse);
//    }

//    private void addGetByIdResponses(Operation operation) {
//        ApiResponses responses = operation.getResponses();
//        if (responses == null) {
//            responses = new ApiResponses();
//            operation.setResponses(responses);
//        }
//
//        // Success response (200)
//        ApiResponse successResponse = new ApiResponse()
//                .description("Resource found");
//        responses.addApiResponse("200", successResponse);
//
//        // Not found response (404)
//        ApiResponse notFoundResponse = new ApiResponse()
//                .description("Resource not found")
//                .content(new Content()
//                        .addMediaType("application/json", new MediaType()
//                                .addExample("Default", createExample(null, ExampleProviders.NOT_FOUND_EXAMPLE))
//                                .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
//                        )
//                );
//        responses.addApiResponse("404", notFoundResponse);
//    }

    private Example createExample(String summary, String value) {
        Example example = new Example();
        if (summary != null) {
            example.setSummary(summary);
        }
        example.setValue(value);
        return example;
    }
}