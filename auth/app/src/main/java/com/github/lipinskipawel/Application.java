package com.github.lipinskipawel;

import static com.github.lipinskipawel.HttpApplicationServer.httpServer;

public final class Application {

    public static void main(String[] args) {
        final var dependencies = new Dependencies();
        final var app = httpServer(dependencies);
        app.start(8090);
    }
}
