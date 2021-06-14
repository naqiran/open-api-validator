package com.naqiran.oas.validator;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;

public class SchemaValidator {

    public static void validateParameterSchema(final Context context, final Parameter parameter, final List<String> values) {
        final var schema = parameter.getSchema();
        if (Boolean.TRUE.equals(parameter.getRequired()) && values == null) {
            context.addMessage(Context.MessageLevel.ERROR, "%s: %s is required", parameter.getName(), parameter.getIn());
        }
        if (Boolean.TRUE.equals(parameter.getDeprecated())) {
            context.addMessage(Context.MessageLevel.WARN, "%s: %s is deprecated", parameter.getName(), parameter.getIn());
        }
        if (schema != null && values != null) {
            if (schema.getType() != null && !values.isEmpty()) {
                if ("array".equals(schema.getType())) {
                    ArraySchema aSchema = (ArraySchema) schema;
                    values.forEach(val -> validateParameterSchema(context, parameter.getName(), aSchema.getItems(), val));
                } else {
                    validateParameterSchema(context, parameter.getName(), schema, values.get(0));
                }
            }
        }
    }

    public static void validateParameterSchema(final Context context, final String parameterName, final Schema<?> schema, final String value) {
        if (schema != null) {
            var format = schema.getFormat();
            if (schema.getType() != null) {
                Object obj = null;
                try {
                    switch (schema.getType()) {
                        case "integer":
                            if ("int64".equals(format)) {
                                obj = Long.parseLong(value);
                            } else {
                                obj = Integer.parseInt(value);
                            }
                            break;
                        case "number":
                            if ("double".equals(format)) {
                                obj = Float.parseFloat(value);
                            } else {
                                obj = Double.parseDouble(value);
                            }
                            break;
                        case "string":
                            obj = value;
                            break;
                        case "boolean":
                            obj = Boolean.valueOf(value);
                            break;
                        default:
                            System.out.println("Default");
                    }
                } catch (Exception e) {
                    context.addMessage(Context.MessageLevel.ERROR,"Parameter Name: %s | Expected Type: %s", parameterName, schema.getType());
                }
                checkEnumValues(context, parameterName, schema, obj);
            }
        }
    }

    public static void checkEnumValues(Context context, String parameterName, Schema<?> schema, Object obj) {
        if (schema.getEnum() != null && obj != null && !schema.getEnum().contains(obj)) {
            context.addMessage(Context.MessageLevel.ERROR, "Parameter Name: %s - Allowed Value : %s", parameterName, schema.getEnum().toString());
        }
    }
}
