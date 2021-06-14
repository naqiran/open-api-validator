package com.naqiran.oas.validator;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.ParseOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class OASValidator {

    private final OpenAPI schema;
    private final Map<Pattern, PathItem> pathRegexMap;
    private final Map<String, PathItem> pathMap;

    private final Pattern pathPattern = Pattern.compile("\\{(.*)}");

    private OASValidator(final @Nonnull OpenAPI schema) {
        this.schema = schema;
        this.pathRegexMap = new HashMap<>();
        this.pathMap = new HashMap<>();
        init();
    }

    public void init() {
        if (schema.getPaths() != null) {
            for (final var pathItemEntry : schema.getPaths().entrySet()) {
                var path = pathItemEntry.getKey();
                if (path.contains("{")) {
                    path = pathPattern.matcher(path).replaceAll(result -> "(?<" + result.group(1) + ">.*)");
                    for (var server : schema.getServers()) {
                        pathRegexMap.put(Pattern.compile(server.getUrl() + path), pathItemEntry.getValue());
                    }
                } else {
                    for (var server : schema.getServers()) {
                        pathMap.put(server.getUrl() + path, pathItemEntry.getValue());
                    }
                }
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public @Nonnull Context validate(final @Nonnull HttpRequest request) {
        requireNonNull(request, "Http Request should not be null");
        var context = Context.getContext(request);
        var operation = getOperation(context);
        return context.withOperation(operation)
                .validate(RequestValidator::validate)
                .getResponseForRequest()
                .validate(ResponseValidator::validate);
    }

    public @Nullable Operation getOperation(final @Nonnull Context context) {
        final var pathItem = Optional.ofNullable(pathMap.get(context.getPath()))
                .or(() -> {
                    var entry = pathRegexMap.entrySet().stream()
                            .filter(e -> e.getKey().matcher(context.getPath()).matches())
                            .findFirst()
                            .orElse(null);
                    if (entry != null) {
                        context.setRegexPath(entry.getKey());
                        return Optional.ofNullable(entry.getValue());
                    } else {
                        return Optional.empty();
                    }
                })
                .orElse(null);
        return pathItem != null ? pathItem.readOperationsMap().get(PathItem.HttpMethod.valueOf(context.getRequest().method())) : null;
    }

    public static class Builder {

        private String resource;

        @Nonnull
        public Builder withSchema(final String resource) {
            this.resource = resource;
            return this;
        }

        public OASValidator build() {
            return new OASValidator(new OpenAPIParser().readLocation(resource, List.of(), new ParseOptions()).getOpenAPI());
        }
    }
}
