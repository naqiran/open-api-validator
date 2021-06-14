package com.naqiran.oas.validator.command;

import com.naqiran.oas.validator.OASValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(OASValidatorCommand.class);

    @Parameters(description = "API Endpoint URL")
    private URI uri;

    @Option(names = {"-X", "--method"}, defaultValue = "GET")
    private String method;

    @Option(names = {"-d", "--data"}, defaultValue = "", description = "HTTP Request Body")
    private String requestBody;

    @Option(names = {"-H", "--headers", "Http Headers"})
    private List<String> headers;

    @Option(names = {"-s", "--schema"}, description = "Open API Schema URL or File", required = true)
    private String schema;

    @Override
    public String call() {
        var builder = HttpRequest.newBuilder()
                .uri(uri)
                .method(method, HttpRequest.BodyPublishers.ofString(requestBody));
        for (var entry : getHeaders().entrySet()) {
            builder.headers(entry.getKey(), entry.getValue());
        }
        var messages = OASValidator.builder().withSchema(schema).build().validate(builder.build());
        messages.getMessages().forEach(message -> LOGGER.info(message.toString()));
        return "success";
    }

    public Map<String, String> getHeaders() {
        final Map<String, String> headers = new HashMap<>();
        return headers;
    }

    public static void main(final String[] args) {
        String[] args1 = {"--schema=https://petstore3.swagger.io/api/v3/openapi.json", "https://petstore3.swagger.io/api/v3/store/order/1234", "--method=GET"};
        System.exit(new CommandLine(OASValidatorCommand.class).execute(args1));

        //System.exit(new CommandLine(OASValidatorCommand.class).execute(args));
    }
}
