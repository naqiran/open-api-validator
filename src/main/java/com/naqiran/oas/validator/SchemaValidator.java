package com.naqiran.oas.validator;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import com.naqiran.oas.validator.Context.MessageLevel;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class SchemaValidator {

    public static void validateJsonSchema(final @Nonnull Context context, final @Nonnull String path, final Schema<?> schema, final JsonNode node) {
        if (schema != null && node != null) {
            switch (schema.getType()) {
                case "array":
                    validateJsonArray(context, path, (ArraySchema) schema, node);
                    break;
                case "object":
                    validateJsonObject(context, path, (ObjectSchema) schema, node);
                    break;
                case "integer":
                    if ("int64".equals(schema.getFormat()) && !node.canConvertToLong()) {
                        context.addMessage(MessageLevel.ERROR, "Parameter Name: %s expected [long] but found : %s", path, node.getNodeType().toString());
                    } else if ((schema.getFormat() == null || "int32".equals(schema.getFormat())) && !node.canConvertToInt()) {
                        context.addMessage(MessageLevel.ERROR, "Parameter Name: %s expected [integer] but found : %s", path, node.getNodeType().toString());
                    }
                    break;
                case "number":
                    if (!node.isNumber()) {
                        context.addMessage(MessageLevel.ERROR, "Parameter Name: %s expected [number] but found : %s", path, node.getNodeType().toString());
                    }
                    break;
                case "string":
                    if (!node.isTextual()) {
                        context.addMessage(MessageLevel.ERROR, "Parameter Name: %s expected [string] but found : %s", path, node.getNodeType().toString());
                    } else {
                        checkMinLength(context, path, schema.getMinLength(), node.asText());
                        checkMaxLength(context, path, schema.getMaxLength(), node.asText());
                    }
                    break;
                default:
                    System.out.println("Its default" + schema.getType());
            }
        }
    }

    public static void validateJsonArray(final @Nonnull Context context, final @Nonnull String path, final ArraySchema schema, final JsonNode node) {
        if (node.isArray()) {
            if (schema.getMinLength() != null && !node.has(schema.getMinLength())) {
                context.addMessage(MessageLevel.ERROR,"Parameter Name: %s less than min-items: %s", path, schema.getMinLength().toString());
            }
            if (schema.getMaxLength() != null && node.has(schema.getMaxLength() + 1)) {
                context.addMessage(MessageLevel.ERROR,"Parameter Name: %s less than max-items: %s", path, schema.getMaxLength().toString());
            }
            node.forEach(val -> validateJsonSchema(context, path + "[x]", context.getSchema(((ArraySchema) schema).getItems()), val));
        } else {
            context.addMessage(MessageLevel.ERROR, "Parameter Name: %s expected [array] but found : %s", path, node.getNodeType().toString());
        }
    }

    public static void validateJsonObject(final @Nonnull Context context, final @Nonnull String path, final ObjectSchema schema, final JsonNode node) {
        if (node.isObject()) {
            Map<String, Schema> map = schema.getProperties();
            for (Map.Entry<String, Schema> entry : map.entrySet()) {
                validateJsonSchema(context, entry.getKey(), context.getSchema(entry.getValue()), node.get(entry.getKey()));
            }
        } else {
            context.addMessage(MessageLevel.ERROR, "Parameter Name: %s expected [object] but found : %s", path, node.getNodeType().toString());
        }
    }

    public static void validateParameterSchema(final Context context, final String attributeName, final Schema<?> schema, final List<String> values) {
        if (schema != null && values != null) {
            if (schema.getType() != null && !values.isEmpty()) {
                if ("array".equals(schema.getType())) {
                    checkMinItems(context, attributeName, schema.getMinLength(), values);
                    checkMaxItems(context, attributeName, schema.getMaxLength(), values);
                    values.forEach(val -> validateParameterSchema(context, attributeName, ((ArraySchema) schema).getItems(), val));
                } else {
                    validateParameterSchema(context, attributeName, schema, values.get(0));
                }
            }
        }
    }

    public static void validateParameterSchema(final Context context, final String attributeName, final Schema<?> schema, final String value) {
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
                            checkMinLength(context, attributeName, schema.getMinLength(), value);
                            checkMaxLength(context, attributeName, schema.getMaxLength(), value);
                            break;
                        case "boolean":
                            obj = Boolean.valueOf(value);
                            break;
                        default:
                            System.out.println("Default");
                    }
                } catch (Exception e) {
                    context.addMessage(MessageLevel.ERROR,"Parameter Name: %s | Expected Type: %s", attributeName, schema.getType());
                }
                checkEnumValues(context, attributeName, schema, obj);
            }
        }
    }

    public static void checkEnumValues(final Context context, final String attributeName, final Schema<?> schema, final Object obj) {
        if (schema.getEnum() != null && obj != null && !schema.getEnum().contains(obj)) {
            context.addMessage(MessageLevel.ERROR, "Parameter Name: %s - Allowed Value : %s", attributeName, schema.getEnum().toString());
        }
    }

    public static void checkMinLength(final Context context, final String attributeName, final Integer length, final String value) {
        if (length != null && value != null && value.length() < length) {
            context.addMessage(MessageLevel.ERROR,"Parameter Name: %s less than min-length: %s", attributeName, length.toString());
        }
    }

    public static void checkMaxLength(final Context context, final String attributeName, final Integer length, final String value) {
        if (length != null && value != null && value.length() > length) {
            context.addMessage(MessageLevel.ERROR,"Parameter Name: %s less than max-length: %s", attributeName, length.toString());
        }
    }

    public static void checkMinItems(final Context context, final String attributeName, final Integer length, final List<String> value) {
        if (length != null && value != null && value.size() < length) {
            context.addMessage(MessageLevel.ERROR,"Parameter Name: %s less than min-items: %s", attributeName, length.toString());
        }
    }

    public static void checkMaxItems(final Context context, final String parameterName, final Integer length, final List<String> value) {
        if (length != null && value != null && value.size() > length) {
            context.addMessage(MessageLevel.ERROR,"Parameter Name: %s less than max-items: %s", parameterName, length.toString());
        }
    }
}
