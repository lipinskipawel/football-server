package com.github.lipinskipawel.server;

interface Parser<T> {

    String toJson(T data);
}
