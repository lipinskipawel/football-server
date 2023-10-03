package com.github.lipinskipawel.register;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.post;

public final class RegisterResource implements EndpointGroup {
    private final AuthRegister authRegister;
    private final Routes routes = new Routes();

    public RegisterResource(AuthRegister authRegister) {
        this.authRegister = authRegister;
    }

    @Override
    public void addEndpoints() {
        before("/register", routes.beforeRegister);
        post("/register", routes.register);
    }

    private final class Routes {
        private final Handler beforeRegister = ctx -> {
            if (ctx.headerMap().containsKey("username")) {
                return;
            }
            throw new UnauthorizedResponse();
        };

        private final Handler register = ctx -> {
            final var token = authRegister.handle(ctx.headerMap())
                    .orElseThrow(UnauthorizedResponse::new);
            ctx.res().addHeader("token", token);
        };
    }
}
