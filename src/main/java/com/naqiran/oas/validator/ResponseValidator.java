package com.naqiran.oas.validator;

import io.swagger.v3.oas.models.parameters.Parameter;

import javax.annotation.Nonnull;
import java.util.List;

public class ResponseValidator {

    public static void validate(final @Nonnull Context context) {
        if (context.getResponse() != null) {
            ResponseValidator.validateHeader(context);
        }
    }

    public static void validateHeader(final @Nonnull Context context) {
        if (context.getOperation().getResponses() != null) {
            var apiResponse = context.getOperation().getResponses().get(String.valueOf(context.getResponse().statusCode()));
            if (apiResponse != null && apiResponse.getHeaders() != null) {
                var responseHeaders = context.getResponse().headers().map();
                apiResponse.getHeaders().forEach((name, header) -> SchemaValidator.validateSchema(context, name, header.getSchema(), responseHeaders.get(name)));
            }
        }
    }

    public static void validateParameterSchema(final Context context, final Parameter parameter, final List<String> values) {
        if (Boolean.TRUE.equals(parameter.getRequired()) && values == null) {
            context.addMessage(Context.MessageLevel.ERROR, "%s: %s is required", parameter.getName(), parameter.getIn());
        }
        if (Boolean.TRUE.equals(parameter.getDeprecated())) {
            context.addMessage(Context.MessageLevel.WARN, "%s: %s is deprecated", parameter.getName(), parameter.getIn());
        }
        SchemaValidator.validateSchema(context, parameter.getName(), parameter.getSchema(), values);
    }
}
