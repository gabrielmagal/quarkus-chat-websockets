package com.github.gabrielmagal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.Session;
import org.apache.commons.codec.binary.StringUtils;

@ServerEndpoint("/chat/{username}")
@ApplicationScoped
public class ChatSocketResource {
    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        broadcast("O usuário " + username + " se conectou no canal.");
        sessions.put(username, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(username);
        broadcast("O usuário " + username + " saiu do canal.");
    }

    @OnError
    public void onError(Session session, @PathParam("username") String username, Throwable throwable) {
        sessions.remove(username);
        broadcast("O usuário " + username + " foi desconectado pelo erro : " + throwable);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("username") String username) {
        broadcast(username + " : " + message);
    }

    private void broadcast(String message) {
        String msgFormated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " : " + StringUtils.newStringUtf8(StringUtils.getBytesUtf8(message));
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(msgFormated, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }
}