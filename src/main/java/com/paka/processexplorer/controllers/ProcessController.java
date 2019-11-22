package com.paka.processexplorer.controllers;


import com.paka.processexplorer.model.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ProcessController {

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Message greeting(String author) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Message(author, "Super duper text on date: " + System.currentTimeMillis());
    }
}