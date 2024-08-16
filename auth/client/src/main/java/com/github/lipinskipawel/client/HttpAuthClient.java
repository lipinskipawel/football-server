package com.github.lipinskipawel.client;

import com.github.lipinskipawel.client.HttpConfig.HttpRequestConfig;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Optional;

import static java.net.http.HttpClient.newBuilder;
import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.net.http.HttpResponse.BodyHandlers.discarding;

public final class HttpAuthClient implements AuthClient {

    private final HttpClient httpClient;
    private final HttpRequestConfig requestConfig;

    private HttpAuthClient(HttpConfig httpConfig) {
        this.httpClient = newBuilder()
            .connectTimeout(httpConfig.connectTimeout())
            .build();
        this.requestConfig = httpConfig.httpRequestConfig();
    }

    public static HttpAuthClient httpAuthClient(HttpConfig httpConfig) {
        return new HttpAuthClient(httpConfig);
    }

    @Override
    public Optional<String> register(String username) {
        final var request = HttpRequest.newBuilder(requestConfig.baseUri().resolve("/register"))
            .POST(noBody())
            .header("username", username)
            .timeout(requestConfig.timeout())
            .build();
        try {
            return httpClient.send(request, discarding())
                .headers()
                .firstValue("token");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> findUsernameByToken(String token) {
        final var request = HttpRequest.newBuilder(requestConfig.baseUri().resolve("/find-username"))
            .GET()
            .header("token", token)
            .timeout(requestConfig.timeout())
            .build();
        try {
            return httpClient.send(request, discarding())
                .headers()
                .firstValue("username");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
