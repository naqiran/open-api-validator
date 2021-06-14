package com.naqiran.open.api.validator;

import picocli.CommandLine;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

@Command(name = "oas-validator")
public class OASValidatorCommand implements Callable<String> {

    @Option(names = {"-X", "--method"}, defaultValue = "GET")
    private String method;

    @Option(names = {"-d", "--data"}, defaultValue = "")
    private String requestBody;

    @Option(names = {"-H", "--headers"})
    private List<String> headers;

    @Option(names = {"-s", "--schema"}, required = true)
    private String schema;

    @Parameters
    private URI uri;

    @Override
    public String call() {
        var builder = HttpRequest.newBuilder()
                .uri(uri)
                .method(method, HttpRequest.BodyPublishers.ofString(requestBody));
        for (var entry : getHeaders().entrySet()) {
            builder.headers(entry.getKey(), entry.getValue());
        }
        var messages = OASValidator.builder().withSchema(schema).build().validate(builder.build());
        messages.getMessages().forEach(System.out::println);
        return "success";
    }

    public Map<String, String> getHeaders() {
        final Map<String, String> headers = new HashMap<>();
        return headers;
    }

    public static void main(final String[] args) {
        System.exit(new CommandLine(OASValidatorCommand.class).execute(args));
    }
}
