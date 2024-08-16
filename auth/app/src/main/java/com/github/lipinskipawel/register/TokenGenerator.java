package com.github.lipinskipawel.register;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * This class is capable of generating random tokens. There is no guarantee that the generated token will be unique so
 * the client must check for that. Tokens must be obtained by calling the {@link #get()} method.
 * Tokens will be created out of fixed characters.
 * The default length of the token is 16, but it can be customizable.
 */
public final class TokenGenerator implements Supplier<String> {
    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXZY";
    private final Random random;
    private final int tokenLength;

    public TokenGenerator() {
        this.random = ThreadLocalRandom.current();
        this.tokenLength = 16;
    }

    TokenGenerator(Random random, int tokenLength) {
        this.random = random;
        this.tokenLength = tokenLength;
    }

    @Override
    public String get() {
        final var numberOfAllowedCharacters = ALLOWED_CHARACTERS.length();
        return IntStream
            .range(0, tokenLength)
            .mapToObj(number -> {
                final var randNumber = random.nextInt(numberOfAllowedCharacters);
                return ALLOWED_CHARACTERS.charAt(randNumber);
            })
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
    }
}
