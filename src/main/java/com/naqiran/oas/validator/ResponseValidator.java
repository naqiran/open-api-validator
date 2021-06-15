package com.naqiran.oas.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

public class ResponseValidator {

    public static void validate(final @Nonnull Context context) {
        if (context.getResponse() != null) {
            ResponseValidator.validateHeader(context);
            ResponseValidator.validateResponseBody(context);
        }
    }

    public static void validateHeader(final @Nonnull Context context) {
        if (context.getOperation().getResponses() != null) {
            var apiResponse = context.getOperation().getResponses().get(String.valueOf(context.getResponse().statusCode()));
            if (apiResponse != null && apiResponse.getHeaders() != null) {
                var responseHeaders = context.getResponse().headers().map();
                apiResponse.getHeaders().forEach((name, header) -> SchemaValidator.validateParameterSchema(context, name, header.getSchema(), responseHeaders.get(name)));
            }
        }
    }

    public static void validateResponseBody(final @Nonnull Context context) {
        if (StringUtils.isNotBlank(context.getResponse().body())) {
            try {
                final var apiResponse = context.getOperation().getResponses().get(String.valueOf(context.getResponse().statusCode()));
                if (apiResponse != null && apiResponse.getContent() != null) {
                    var mediaType = apiResponse.getContent().get("application/json");
                    if (mediaType != null) {
                        SchemaValidator.validateJsonSchema(context, "root", context.getSchema(mediaType.getSchema()), new ObjectMapper().readTree(context.getResponse().body()));
                    }
                } else {
                    context.addMessage(Context.MessageLevel.WARN, "No Response schema defined for status code : %s", String.valueOf(context.getResponse().statusCode()));
                }
            } catch (final JsonProcessingException ex) {
                context.addMessage(Context.MessageLevel.ERROR,"Not a valid JSON Object: %s", ex.getMessage());
            }
        }
    }
}
