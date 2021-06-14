package com.naqiran.oas.validator;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import com.naqiran.oas.validator.Context.MessageLevel;

import java.util.List;

public class SchemaValidator {

    public static void validateSchema(final Context context, final String attributeName, final Schema<?> schema, final List<String> values) {
        if (schema != null && values != null) {
            if (schema.getType() != null && !values.isEmpty()) {
                if ("array".equals(schema.getType())) {
                    checkMinItems(context, attributeName, schema.getMinLength(), values);
                    checkMaxItems(context, attributeName, schema.getMaxLength(), values);
                    values.forEach(val -> validateSchema(context, attributeName, ((ArraySchema) schema).getItems(), val));
                } else {
                    validateSchema(context, attributeName, schema, values.get(0));
                }
            }
        }
    }

    public static void validateSchema(final Context context, final String attributeName, final Schema<?> schema, final String value) {
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
