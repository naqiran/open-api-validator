package com.naqiran.oas.validator;

import io.swagger.v3.oas.models.parameters.Parameter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestValidator {

    public static void validate(final @Nonnull Context context) {
        validateParameter(context, "header");
        validateParameter(context, "query");
        validateParameter(context, "path");
        validateParameter(context, "cookie");
    }

    public static void validateParameter(final @Nonnull Context context, final @Nonnull String type) {
        if (context.getOperation().getParameters() != null) {
            final var parameters = context.getOperation().getParameters().stream().filter(parameter -> type.equals(parameter.getIn()))
                    .collect(Collectors.toList());
            parameters.forEach(parameter -> validateParameterSchema(context, parameter, getValues(context, type).get(parameter.getName())));
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

    public static Map<String, List<String>> getValues(final Context context, @Nonnull String type) {
        var request = context.getRequest();
        switch (type) {
            case "header":
                return request.headers().map();
            case "query":
                return HttpUtils.getQueryParameters(request);
            case "path":
                return HttpUtils.getPathParameters(context);
            default:
                return Map.of();
        }
    }
}
