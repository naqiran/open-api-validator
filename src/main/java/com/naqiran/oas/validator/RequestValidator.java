package com.naqiran.oas.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naqiran.oas.validator.utils.HttpUtils;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.StringUtils;

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
        if (StringUtils.isNotBlank(context.getRequest().getBody())) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                RequestBody body = context.getOperation().getRequestBody();
                MediaType type = body.getContent().get("application/json");
                var reference = type.getSchema().get$ref();
                if (reference != null) {
                    String schemaName = reference.replace("#/components/schemas/", "");
                    Schema<?> schema = context.getComponents().getSchemas().get(schemaName);
                    System.out.println(schema);
                    JsonNode node = mapper.readTree(context.getRequest().getBody());
                    SchemaValidator.validateSchema(context, schema, node);
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();
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
        SchemaValidator.validateSchema(context, parameter.getName(), parameter.getSchema(), values);
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
