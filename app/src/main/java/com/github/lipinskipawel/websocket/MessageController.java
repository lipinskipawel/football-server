package com.github.lipinskipawel.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Controller
public class MessageController {

    @MessageMapping("/hello")
    @SendTo("/topic/messages")
    public String some(final Message message) {
        return "Hello " + message.getName() + " " + LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
