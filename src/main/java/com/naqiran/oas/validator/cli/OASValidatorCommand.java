package com.naqiran.oas.validator.cli;

import com.naqiran.oas.validator.OASValidator;
import com.naqiran.oas.validator.Request;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.Parameters;

@Command(name = "oas-validator")
public class OASValidatorCommand implements Callable<String> {

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
        var request = Request.builder(uri, method).withHeader(getHeaders()).withBody(requestBody);
        var messages = OASValidator.builder().withSchema(schema).build().validate(request);
        messages.getMessages().forEach(System.out::println);
        return "success";
    }

    @Nonnull
    private Map<String, List<String>> getHeaders() {
        final Map<String, List<String>> headerMap = new HashMap<>();
        headerMap.put(HttpHeaders.CONTENT_TYPE, List.of(ContentType.APPLICATION_JSON.toString()));
        headerMap.put(HttpHeaders.ACCEPT, List.of(ContentType.APPLICATION_JSON.toString()));
        if (headers != null) {
            for (final var header: headers) {
                final var headerKv = StringUtils.removeEnd(StringUtils.removeStart(header, "'"), "'").split(":");
                headerMap.put(headerKv[0].trim(), List.of(headerKv[1].trim()));
            }
        }
        return headerMap;
    }

    public static void main(final String[] args) {
        System.exit(new CommandLine(OASValidatorCommand.class).execute(args));
    }
}
