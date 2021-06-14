package com.naqiran.oas.validator;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HttpUtils {

    public static @Nonnull String getUrl(final @Nonnull HttpRequest request) {
        return Optional.ofNullable(request.uri().getQuery())
                .or(() -> Optional.of(StringUtils.EMPTY))
                .map(s -> request.uri().toString().replace("?" + s, StringUtils.EMPTY)).get();
    }

    public static @Nonnull Map<String, List<String>> getQueryParameters(final @Nonnull HttpRequest request) {
        var queries = request.uri().getQuery();
        if (StringUtils.isNotBlank(queries)) {
            final Map<String, List<String>> parameters = new HashMap<>();
            for (final var queryArray : queries.split("&")) {
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
        final var pattern = context.getRegexPath();
        if (pattern != null) {
            final var pathName = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>").matcher(pattern.pattern()).results().map(m -> m.group(1)).collect(Collectors.toList());
            final Map<String, List<String>> pathMap = new HashMap<>();
            int index = 0;
            for (var result: pattern.matcher(context.getPath()).results().collect(Collectors.toList())) {
                pathMap.put(pathName.get(index++), List.of(result.group(1)));
            }
            return pathMap;
        }
        return Map.of();
    }

    public static @Nonnull HttpResponse<String> getResponse(final @Nonnull HttpRequest request) throws IOException, InterruptedException {
        return HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
    }
}
