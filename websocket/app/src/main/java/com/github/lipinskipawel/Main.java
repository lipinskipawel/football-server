package com.github.lipinskipawel;

import com.github.lipinskipawel.client.HttpConfig;
import com.github.lipinskipawel.client.HttpConfig.HttpRequestConfig;
import com.github.lipinskipawel.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.lipinskipawel.client.HttpAuthClient.httpAuthClient;
import static java.time.Duration.ofSeconds;

final class Main {
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();
    private static final WebSocketServer wsServer = new WebSocketServer();

    public static void main(String[] args) throws InterruptedException {
        try {
            wsServer.start(
                    new InetSocketAddress("localhost", 8080),
                    httpAuthClient(httpConfig())
            );

            wsServer.waitForClosureOfServerSocket();
        } finally {
            wsServer.stop();
            pool.shutdown();
        }
    }

    private static HttpConfig httpConfig() {
        return new HttpConfig(ofSeconds(5), new HttpRequestConfig(URI.create("http://localhost:8090"), ofSeconds(1)));
    }
}
