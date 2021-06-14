package com.naqiran.open.api.validator;

import io.swagger.v3.oas.models.Operation;

import javax.annotation.Nonnull;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class Context {

    private HttpRequest request;
    private String path;
    private Pattern regexPath;
    private List<Message> messages;
    private Operation operation;


    private Context(final @Nonnull HttpRequest request) {
        this.request = request;
        this.messages = new ArrayList<>();
    }

    public static Context getContext(final HttpRequest request) {
        return new Context(request).withPath(ValidatorUtils.getUrl(request));
    }

    private Context withPath(final String path) {
        this.path = path;
        return this;
    }

    public void setRegexPath(final Pattern regexPath) {
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

    private void addMessages(final @Nonnull List<Message> messages) {
        this.messages.addAll(messages);
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

    public Pattern getRegexPath() {
        return regexPath;
    }

    public String getPath() {
        return path;
    }

    public enum MessageLevel {
        WARN, ERROR, IGNORED, INFO
    }

    class Message {

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
