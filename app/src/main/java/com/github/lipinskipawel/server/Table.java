package com.github.lipinskipawel.server;

import com.github.lipinskipawel.util.ThreadSafe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represent a table where two players are connected to each other. Therefore, it manages number of clients
 * connected to the same endpoint.
 * This class is a helper class and used as a dependency for the {@link FootballServer} class.
 */
@ThreadSafe
public final class Table {
    private final Map<String, Integer> urlsToNumber;

    public Table() {
        this.urlsToNumber = new ConcurrentHashMap<>(64);
    }

    boolean playIfThereIsAPlace(final String url) {
        if (this.canConnect(url)) {
            this.urlsToNumber.merge(url, 1, Integer::sum);
            return true;
        } else {
            return false;
        }
    }

    private boolean canConnect(final String url) {
        if (this.urlsToNumber.containsKey(url)) {
            return this.urlsToNumber.get(url) < 2;
        } else {
            return true;
        }
    }

    void leaveTheTable(final String url) {
        this.urlsToNumber.remove(url);
    }
}
