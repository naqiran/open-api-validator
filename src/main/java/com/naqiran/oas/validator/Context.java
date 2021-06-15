package com.naqiran.oas.validator;

import com.naqiran.oas.validator.utils.HttpUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;

import javax.annotation.Nonnull;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Context {

    private final Request request;
    private final List<Message> messages;
    private final String path;
    private Pattern regexPath;
    private Operation operation;
    private HttpResponse<String> response;
    private Components components;

    private Context(final @Nonnull Request request) {
        this.request = request;
        this.path = HttpUtils.getUrl(request);
        this.messages = new ArrayList<>();
    }

    public static Context getContext(final @Nonnull Request request) {
        return new Context(request);
    }

    public void setRegexPath(final @Nonnull Pattern regexPath) {
        this.regexPath = regexPath;
    }

    public final Context setOperation(final Operation operation) {
        if (operation == null) {
            addMessage(MessageLevel.ERROR, "Operation not defined for the path: %s", path);
        } else {
            addMessage(MessageLevel.INFO, "API is validated against the Operation: %s", operation.getOperationId());
            this.operation = operation;
            if (Boolean.TRUE.equals(operation.getDeprecated())) {
                addMessage(MessageLevel.WARN, "API is deprecated %s", request.getUri().getPath());
            }

        }
        return this;
    }

    public Context withComponents(final Components components) {
        this.components = components;
        return this;
    }

    public Context withOperation(final Function<Context,Context> operationFunction) {
        return operationFunction.apply(this);
    }

    public final Context addMessage(final @Nonnull MessageLevel level, final @Nonnull String message, final String... args) {
        messages.add(new Message(String.format(message, (Object[]) args), level));
        return this;
    }

    public final Context validate(Consumer<Context> validateMethod) {
        if (operation != null) {
            validateMethod.accept(this);
        }
        return this;
    }

    public Context getResponseForRequest() {
        try {
            response = HttpUtils.getResponse(request);
        } catch (Exception e) {
            addMessage(MessageLevel.ERROR, "Error Occurred requesting API : %s", e.getMessage());
        }
        return this;
    }

    public Schema getSchema(Schema schema) {
        if (schema.getType() != null) {
            return schema;
        } else {
            String reference = schema.get$ref();
            if (reference != null) {
                String schemaName = reference.replace("#/components/schemas/", "");
                Schema referenceSchema = components.getSchemas().get(schemaName);
                if (referenceSchema == null) {
                    throw new ValidationException("Reference schema is missing: " + schemaName);
                }
                return referenceSchema;
            } else {
                throw new ValidationException("Reference or type shoule be present");
            }
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public Operation getOperation() {
        return operation;
    }

    public Request getRequest() {
        return request;
    }

    public HttpResponse<String> getResponse() {
        return response;
    }

    public Pattern getRegexPath() {
        return regexPath;
    }

    public String getPath() {
        return path;
    }

    public Components getComponents() {
        return components;
    }

    public enum MessageLevel {
        WARN, ERROR, IGNORED, INFO
    }

    public static class Message {
        private final String message;
        private final MessageLevel level;

        public Message(final @Nonnull String message, final @Nonnull MessageLevel level) {
            this.message = message;
            this.level = level;
        }

        @Override
        public String toString() {
            return String.format("%s: %s", level, message);
        }
    }


}
