package com.github.lipinskipawel.routes;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static io.javalin.apibuilder.ApiBuilder.after;
import static io.javalin.apibuilder.ApiBuilder.before;
import static java.lang.Long.parseLong;
import static java.lang.String.valueOf;
import static java.lang.System.nanoTime;

public final class LoggingResource implements EndpointGroup {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingResource.class);
    private static final String USER_AGENT = "User-Agent";
    private static final String REQUEST_PATH = "Request-Url";
    private static final String START_REQUEST = "start-request";
    private static final Routes routes = new Routes();

    public LoggingResource() {
    }

    @Override
    public void addEndpoints() {
        before(routes.inbound);
        after(routes.outbound);
    }

    private static final class Routes {
        private final Handler inbound = ctx -> {
            MDC.put(USER_AGENT, ctx.header(USER_AGENT));
            final var requestUrl = ctx.method() + " " + ctx.path();
            MDC.put(REQUEST_PATH, requestUrl);
            MDC.put(START_REQUEST, valueOf(nanoTime()));

            LOG.info("Inbound request from [{}] at ip [{}]", ctx.host(), ctx.ip());
        };

        private final Handler outbound = ctx -> {
            final var elapsed = (nanoTime() - parseLong(MDC.get(START_REQUEST))) / 1_000_000;
            LOG.info("Outbound request from [{}] at ip [{}] took [{}] ms", ctx.host(), ctx.ip(), elapsed);
            MDC.remove(USER_AGENT);
            MDC.remove(REQUEST_PATH);
            MDC.remove(START_REQUEST);
        };
    }
}
