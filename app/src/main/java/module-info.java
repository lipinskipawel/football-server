module football.server {
    requires spring.boot.starter;
    requires spring.boot;
    requires spring.websocket;
    requires spring.messaging;
    requires spring.context;
    requires spring.boot.autoconfigure;

    opens com.github.lipinskipawel to spring.core, spring.beans, spring.context;
}
