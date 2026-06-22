package com.example.network.http;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.database.Db;
import com.example.database.UserCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Optional;

public class StoreServerHTTP implements Runnable {
    private static ObjectMapper mapper = new ObjectMapper();
    private final Db db;
    private HttpServer server;
    public static int PORT = 13287;

    public StoreServerHTTP(Db db) {
        this.db = db;
    }

    public static void main(String[] args) {
        StoreServerHTTP server = new StoreServerHTTP(new Db("warehouse.db"));
        server.run();
    }

    @Override
    public void run() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException e) {
            System.err.println("Failed to start HTTP server");
        }

        HttpContext loginContext = server.createContext("/login/", exchange -> {
            UserCredentials userCredentials = mapper.readValue(exchange.getRequestBody(), UserCredentials.class);
            Optional<UserCredentials> retrievedCredentials = db.getUser(userCredentials.getLogin());


            if (retrievedCredentials.isPresent()
                    && retrievedCredentials.get().getLogin().equals(userCredentials.getLogin())
                    && retrievedCredentials.get().getPassword().equals(userCredentials.getPassword())) {
                try {
                    Algorithm algorithm = Algorithm.HMAC256("keykeykey");
                    String token = JWT.create()
                            .withSubject(userCredentials.getLogin())
                            .withExpiresAt(Instant.now().plusSeconds(1800))
                            .sign(algorithm);

                    AuthenticationResponse response = new AuthenticationResponse(token);
                    byte[] bytes = mapper.writeValueAsBytes(response);

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                } catch (JWTVerificationException e) {
                    e.printStackTrace();
                }
            } else {
                String payload = """
                       {
                           "message": "Invalid login credentials"
                       }
                       """;
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(401, payload.getBytes().length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(payload.getBytes());
                }
            }
        });


        server.start();
    }
}
