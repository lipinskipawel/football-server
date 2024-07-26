package com.github.lipinskipawel.client;

import java.net.URI;
import java.time.Duration;

import static java.util.Objects.requireNonNull;

public record HttpConfig(
        Duration connectTimeout,
        HttpRequestConfig httpRequestConfig
) {
    public HttpConfig {
        requireNonNull(connectTimeout);
        requireNonNull(httpRequestConfig);
    }

    public record HttpRequestConfig(
            URI baseUri,
            Duration timeout
    ) {
        public HttpRequestConfig {
            requireNonNull(baseUri);
            requireNonNull(timeout);
        }
    }
}
