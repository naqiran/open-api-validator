package com.naqiran.oas.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naqiran.oas.validator.utils.HttpUtils;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;

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
        validateRequestBody(context);
    }

    public static void validateRequestBody(final @Nonnull Context context) {
        if (context.getOperation().getRequestBody() != null) {
            final RequestBody body = context.getOperation().getRequestBody();
            if (body.getRequired() && StringUtils.isBlank(context.getRequest().getBody())) {
                context.addMessage(Context.MessageLevel.ERROR,"Request Body is required for %s : %s", context.getRequest().getMethod(), context.getPath());
            }
            try {
                final var type = body.getContent().get(ContentType.APPLICATION_JSON.toString());
                if (type != null) {
                    SchemaValidator.validateJsonSchema(context, "root", context.getSchema(type.getSchema()), new ObjectMapper().readTree(context.getRequest().getBody()));
                }
            } catch (final JsonProcessingException ex) {
                context.addMessage(Context.MessageLevel.ERROR,"Not a valid JSON Object: %s", ex.getMessage());
            }
        }
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
        SchemaValidator.validateParameterSchema(context, parameter.getName(), parameter.getSchema(), values);
    }

    public static Map<String, List<String>> getValues(final Context context, @Nonnull String type) {
        var request = context.getRequest();
        switch (type) {
            case "header":
                return request.getHeaders();
            case "query":
                return HttpUtils.getQueryParameters(request);
            case "path":
                return HttpUtils.getPathParameters(context);
            default:
                return Map.of();
        }
    }
}
