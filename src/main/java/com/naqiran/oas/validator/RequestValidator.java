package com.naqiran.oas.validator;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestValidator {

    public static void validateRequest(final @Nonnull Context context, final @Nonnull String type) {
        if (context.getOperation().getParameters() != null) {
            final var parameters = context.getOperation().getParameters().stream().filter(parameter -> type.equals(parameter.getIn()))
                    .collect(Collectors.toList());
            parameters.forEach(parameter -> SchemaValidator.validateParameterSchema(context, parameter, getValues(context, type).get(parameter.getName())));
        }
    }

    public static Map<String, List<String>> getValues(final Context context, @Nonnull String type) {
        var request = context.getRequest();
        switch (type) {
            case "header":
                return request.headers().map();
            case "query":
                return ValidatorUtils.getQueryParameters(request);
            case "path":
                return ValidatorUtils.getPathParameters(context);
            default:
                return Map.of();
        }
    }
}
