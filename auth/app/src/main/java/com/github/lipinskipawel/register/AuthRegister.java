package com.github.lipinskipawel.register;

import com.github.lipinskipawel.db.User;
import com.github.lipinskipawel.db.UserRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static com.github.lipinskipawel.db.User.Builder.userBuilder;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;

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
            .map(User::token);
    }

    private Optional<User> register(String username) {
        final var registered = register.register(username);
        if (registered) {
            final var user = userBuilder()
                .id(randomUUID())
                .username(username)
                .token(register.getTokenForUsername(username))
                .created(Instant.now())
                .build();
            final var saved = userRepository.save(user);
            return saved > 0 ? Optional.of(user) : empty();
        } else {
            return empty();
        }
    }

    public Optional<String> findUsernameByToken(final String token) {
        return userRepository.findByToken(token)
            .map(User::username);
    }

    public void clearAll() {
        register.store().clear();
    }
}
