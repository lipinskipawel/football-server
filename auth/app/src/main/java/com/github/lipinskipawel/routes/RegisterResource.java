package com.github.lipinskipawel.routes;

import com.github.lipinskipawel.register.AuthRegister;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;

import static io.javalin.apibuilder.ApiBuilder.before;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;
import static java.util.Objects.requireNonNull;

public final class RegisterResource implements EndpointGroup {
    private final AuthRegister authRegister;
    private final Routes routes = new Routes();

    public RegisterResource(AuthRegister authRegister) {
        this.authRegister = requireNonNull(authRegister);
    }

    @Override
    public void addEndpoints() {
        before("/register", routes.beforeRegister);
        post("/register", routes.register);
        get("/find-username", routes.findUsername);
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

        private final Handler findUsername = ctx -> {
            final var token = ctx.header("token");
            if (token == null) {
                throw new BadRequestResponse("Token not provided");
            }
            final var username = authRegister.findUsernameByToken(token);
            if (username.isEmpty()) {
                throw new NotFoundResponse("Username not found");
            }
            ctx.res().addHeader("username", username.get());
        };
    }
}
