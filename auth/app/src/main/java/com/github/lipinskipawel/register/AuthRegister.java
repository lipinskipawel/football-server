package com.github.lipinskipawel.register;

import com.github.lipinskipawel.model.Token;
import com.github.lipinskipawel.model.User;
import com.github.lipinskipawel.model.UserRepository;
import com.github.lipinskipawel.model.Username;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static com.github.lipinskipawel.model.Token.token;
import static com.github.lipinskipawel.model.User.Builder.createdUser;
import static com.github.lipinskipawel.model.Username.username;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public final class AuthRegister {
    private final Register register;
    private final UserRepository userRepository;

    public AuthRegister(Register register, UserRepository userRepository) {
        this.register = register;
        this.userRepository = userRepository;
    }

    public Optional<String> handle(Map<String, String> headers) {
        return ofNullable(headers.get("username"))
            .flatMap(this::register)
            .map(User::token)
            .map(Token::token);
    }

    private Optional<User> register(String username) {
        final var registered = register.register(username);
        if (registered) {
            final var now = Instant.now();
            final var user = createdUser()
                .username(username(username))
                .token(token(register.getTokenForUsername(username)))
                .createdDate(now)
                .updatedDate(now)
                .build();
            final var saved = userRepository.save(user);
            return saved > 0 ? Optional.of(user) : empty();
        } else {
            return empty();
        }
    }

    public Optional<String> findUsernameByToken(final String token) {
        return userRepository.findByToken(token)
            .map(User::username)
            .map(Username::username);
    }

    public void clearAll() {
        register.store().clear();
    }
}
