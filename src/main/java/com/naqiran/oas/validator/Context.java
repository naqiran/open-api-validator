package com.naqiran.oas.validator;

import io.swagger.v3.oas.models.Operation;

import javax.annotation.Nonnull;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Context {

    private final HttpRequest request;
    private final List<Message> messages;
    private final String path;
    private Pattern regexPath;
    private Operation operation;
    private HttpResponse<String> response;

    private Context(final @Nonnull HttpRequest request) {
        this.request = request;
        this.path = HttpUtils.getUrl(request);
        this.messages = new ArrayList<>();
    }

    public static Context getContext(final @Nonnull HttpRequest request) {
        return new Context(request);
    }

    public void setRegexPath(final @Nonnull Pattern regexPath) {
        this.regexPath = regexPath;
    }

    public final Context withOperation(final Operation operation) {
        if (operation == null) {
            addMessage(MessageLevel.ERROR, "Operation not defined for the path: %s", path);
        } else {
            addMessage(MessageLevel.INFO, "API is validated against the Operation: %s", operation.getOperationId());
            this.operation = operation;
            if (Boolean.TRUE.equals(operation.getDeprecated())) {
                addMessage(MessageLevel.WARN, "API is deprecated %s", request.uri().getPath());
            }

        }
        return this;
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

    public List<Message> getMessages() {
        return messages;
    }

    public Operation getOperation() {
        return operation;
    }

    public HttpRequest getRequest() {
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



    public enum MessageLevel {
        WARN, ERROR, IGNORED, INFO
    }

    public class Message {

        private String message;
        private MessageLevel level;

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
