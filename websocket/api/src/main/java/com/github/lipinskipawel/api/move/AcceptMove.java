package com.github.lipinskipawel.api.move;

import java.util.Objects;

/**
 * This class is an API.
 * Object of this class is sent by the server on every received {@link GameMove}.
 */
public final class AcceptMove {
    private final String response;

    public AcceptMove() {
        this.response = "ack";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcceptMove that = (AcceptMove) o;
        return Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response);
    }

    @Override
    public String toString() {
        return "AcceptMove{" +
                "response='" + response + '\'' +
                '}';
    }
}
