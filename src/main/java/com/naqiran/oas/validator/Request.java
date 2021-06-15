package com.naqiran.oas.validator;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class Request {
    private final URI uri;
    private final String method;
    private String body;
    private Map<String, List<String>> headers;
    private Map<String, List<String>> cookies;

    private Request(final URI uri, final String method) {
        this.uri = uri;
        this.method = method;
    }

    public Request withCookie(final Map<String, List<String>> cookies) {
        this.cookies = cookies;
        return this;
    }

    public Request withBody(final String body) {
        this.body = body;
        return this;
    }

    public Request withHeader(final Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    public static Request builder(final URI uri, final String method) {
        return new Request(uri, method);
    }

    public URI getUri() {
        return uri;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getCookies() {
        return cookies;
    }
}
