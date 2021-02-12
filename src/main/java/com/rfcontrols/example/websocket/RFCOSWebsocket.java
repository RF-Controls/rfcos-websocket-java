package com.rfcontrols.example.websocket;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.Scanner;

public class RFCOSWebsocket {

    private static final String url = "ws://127.0.0.1:8888/websockets/messaging/websocket";
    private static final String username = "admin";
    private static final String password = "admin";

    public static void main(String[] args) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new StringMessageConverter());
        stompClient.setTaskScheduler(taskScheduler);

        String authString = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("Authorization", authString);

        StompSessionHandler sessionHandler = new StompSessionHandler();
        stompClient.connect(url, headers, sessionHandler);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Press q to quit");
        String input = scanner.nextLine();
        stompClient.stop();
        taskScheduler.destroy();
    }

    public static class StompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            super.handleException(session, command, headers, payload, exception);
            System.err.println("Error connecting to  websocket server");
            exception.printStackTrace();
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to the websocket server!");
            session.subscribe("/topic/tagBlinkLite.*", new StompFrameHandler() {

                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    System.out.println("Got a message: " + payload);
                }
            });
        }
    }
}