package com.naqiran.open.api.validator;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.net.http.HttpRequest;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValidatorUtils {

    public static @Nonnull String getUrl(final @Nonnull HttpRequest request) {
        return Optional.ofNullable(request.uri().getQuery())
                .or(() -> Optional.of(StringUtils.EMPTY))
                .map(s -> request.uri().toString().replace("?" + s, StringUtils.EMPTY)).get();
    }

    public static @Nonnull Map<String, List<String>> getQueryParameters(final @Nonnull HttpRequest request) {
        var queries = request.uri().getQuery();
        if (StringUtils.isNotBlank(queries)) {
            Map<String, List<String>> parameters = new HashMap<>();
            for (final var queryArray :queries.split("&")) {
                var kv = queryArray.split("=");
                final List<String> values = parameters.getOrDefault(kv[0], new ArrayList<>());
                if (kv.length < 2) {
                    values.add(StringUtils.EMPTY);
                } else {
                    values.add(kv[1]);
                }
                parameters.put(kv[0], values);
            }
            return parameters;
        } else {
            return Map.of();
        }
    }

    public static @Nonnull Map<String, List<String>> getPathParameters(final @Nonnull Context context) {
        final String path = context.getPath();
        final Pattern pattern = context.getRegexPath();
        if (pattern != null) {
            List<String> names = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(pattern.pattern()).results().map(m -> m.group(1)).collect(Collectors.toList());
            System.out.println("*****" + names);
            Map<String, List<String>> pathMap = new HashMap<>();
            int index = 0;
            List<MatchResult> results = pattern.matcher(path).results().collect(Collectors.toList());
            for (var result: results) {
                pathMap.put(names.get(index++), List.of(result.group(1)));
            }
            return pathMap;
        }
        return Map.of();
    }
}
