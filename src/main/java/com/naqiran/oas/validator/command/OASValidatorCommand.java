package com.naqiran.oas.validator.command;

import com.naqiran.oas.validator.OASValidator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
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

    @Option(names = {"-X", "--method"}, description = "HTTP Method", defaultValue = "GET")
    private String method;

    @Option(names = {"-d", "--data"}, description = "HTTP Request Body", defaultValue = "")
    private String requestBody;

    @Option(names = {"-H", "--headers", "Pass custom header(s) to server"})
    private List<String> headers;

    @Option(names = {"-b", "--cookie"}, description = "Send cookies from string/file")
    private List<String> cookies;

    @Option(names = {"-s", "--schema"}, description = "Open API Schema URL or File", required = true)
    private String schema;

    @Option(names = {"--connect-timeout"}, description = "Connection timeout in seconds", defaultValue = "60")
    private Integer connectionTimeout;

    @Option(names = {"-h", "--help"}, usageHelp = true)
    private boolean help;

    @Option(names = {"-V", "--version"}, versionHelp = true)
    private boolean versionHelp;

    @Override
    public String call() {
        var builder = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(connectionTimeout))
                .uri(uri)
                .method(method, HttpRequest.BodyPublishers.ofString(requestBody));
        for (var entry : getHeaders().entrySet()) {
            builder.headers(entry.getKey(), entry.getValue().get(0));
        }
        var messages = OASValidator.builder().withSchema(schema).build().validate(builder.build());
        messages.getMessages().forEach(System.out::println);
        return "success";
    }

    @Nonnull
    private Map<String, List<String>> getHeaders() {
        final Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put("Content-Type", List.of("application/json"));
        headerMap.put("Accept", List.of("application/json"));
        if (headers != null) {
            for (final var header: headers) {
                final var headerKv = StringUtils.removeEnd(StringUtils.removeStart(header, "'"), "'").split(":");
                headerMap.put(headerKv[0].trim(), List.of(headerKv[1].trim()));
            }
        }
        return headerMap;
    }

    public static void main(final String[] args) {
        String[] args1 = {"--schema=https://petstore3.swagger.io/api/v3/openapi.json", "https://petstore3.swagger.io/api/v3/store/order/1234", "--method=GET"};
        System.exit(new CommandLine(OASValidatorCommand.class).execute(args1));
    }
}
