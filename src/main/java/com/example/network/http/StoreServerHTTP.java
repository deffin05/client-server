package com.example.network.http;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.database.Db;
import com.example.database.Product;
import com.example.database.ProductFilters;
import com.example.database.UserCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class StoreServerHTTP implements Runnable {
    private static ObjectMapper mapper = new ObjectMapper();
    private final Db db;
    private HttpServer server;
    public static int PORT = 13287;
    private static final String KEY = "keykeykey";

    public StoreServerHTTP(Db db) {
        this.db = db;
    }

    @Override
    public void run() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
        } catch (IOException e) {
            System.err.println("Failed to start HTTP server");
        }

        HttpContext loginContext = server.createContext("/login/", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                String errorJson = "{\"error\": \"Method Not Allowed.\"}";
                byte[] responseBytes = errorJson.getBytes();

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(405, responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
                return;
            }
            UserCredentials userCredentials = mapper.readValue(exchange.getRequestBody(), UserCredentials.class);
            Optional<UserCredentials> retrievedCredentials = db.getUser(userCredentials.getLogin());


            if (retrievedCredentials.isPresent()
                    && retrievedCredentials.get().getLogin().equals(userCredentials.getLogin())
                    && retrievedCredentials.get().getPassword().equals(userCredentials.getPassword())) {
                try {
                    Algorithm algorithm = Algorithm.HMAC256(KEY);
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
                           "error": "Invalid login credentials"
                       }
                       """;
                sendResponse(exchange, 401, payload);
            }
        });

        HttpContext productContext = server.createContext("/products/", exchange -> {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");

            try {
                switch (method) {
                    case "GET":
                        if (segments.length < 3) {
                            sendResponse(exchange, 400, "{\"error\": \"Bad request.\"}");
                            return;
                        }

                        int getProductId = Integer.parseInt(segments[2]);
                        Optional<Product> productOpt = db.getById(getProductId);

                        if (productOpt.isPresent()) {
                            String json = mapper.writeValueAsString(productOpt.get());
                            sendResponse(exchange, 200, json);
                        } else {
                            sendResponse(exchange, 404, "{\"error\": \"Product does not exist.\"}");
                        }
                        break;
                    case "PUT":
                        if (segments.length > 2) {
                            sendResponse(exchange, 400, "{\"error\": \"Bad request.\"}");
                            return;
                        }
                        Product newProduct = null;
                        try {
                            newProduct = mapper.readValue(exchange.getRequestBody(), Product.class);
                        } catch (Exception e) {
                            sendResponse(exchange, 400, "{\"error\": \"Bad request.\"}");
                            return;
                        }

                        ProductFilters filters = new ProductFilters();
                        filters.setName(newProduct.getName());
                        List<Product> existingProducts = db.getAll(filters);
                        boolean exists = false;
                        for (Product product : existingProducts) {
                            if (product.getName().equals(newProduct.getName())) {
                                exists = true;
                                break;
                            }
                        }

                        if (exists) {
                            sendResponse(exchange, 400, "{\"error\": \"Product with such name exists.\"}");
                            return;
                        }

                        int id = db.insert(newProduct);
                        if (id > 0) {
                            newProduct.setId(id);
                            sendResponse(exchange, 201, mapper.writeValueAsString(newProduct));
                        } else {
                            sendResponse(exchange, 500, "{\"error\": \"Failed to create product.\"}");
                        }
                        break;

                    case "POST":
                        if (segments.length < 3) {
                            sendResponse(exchange, 400, "{\"error\": \"Bad request.\"}");
                            return;
                        }

                        int postProductId = Integer.parseInt(segments[2]);

                        if (db.getById(postProductId).isEmpty()) {
                            sendResponse(exchange, 404, "{\"error\": \"Product does not exist.\"}");
                            return;
                        }

                        Product updatedProduct = mapper.readValue(exchange.getRequestBody(), Product.class);
                        updatedProduct.setId(postProductId);
                        boolean isUpdated = db.update(updatedProduct);

                        if (isUpdated) {
                            sendResponse(exchange, 200, mapper.writeValueAsString(updatedProduct));
                        } else {
                            sendResponse(exchange, 500, "{\"error\": \"Failed to update product.\"}");
                        }
                        break;

                    case "DELETE":
                        if (segments.length < 3) {
                            sendResponse(exchange, 400, "{\"error\": \"Bad request.\"}");
                            return;
                        }

                        int deleteProductId = Integer.parseInt(segments[2]);
                        boolean isDeleted = db.delete(deleteProductId);

                        if (isDeleted) {
                            sendResponse(exchange, 200, "{\"message\": \"Product deleted successfully\"}");
                        } else {
                            sendResponse(exchange, 404, "{\"error\": \"Product does not exist\"}");
                        }
                        break;
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Bad request.\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Internal server error.\"}");
                e.printStackTrace();
            }
        });
        productContext.setAuthenticator(new JWTAuthenticator());

        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] responseBytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static class JWTAuthenticator extends Authenticator {
        @Override
        public Result authenticate(HttpExchange exch) {
            List<String> values = exch.getRequestHeaders().get("Authorization");
            // header doesn't exists
            if (values == null || values.isEmpty()) {
                return new Failure(401);
            }

            // wrong auth type
            String[] credentialParts = values.getFirst().split(" ");
            if (credentialParts.length != 2 || !credentialParts[0].equals("Bearer")) {
                return new Failure(401);
            }

            String token = credentialParts[1];
            DecodedJWT decodedJWT;
            try {
                Algorithm algorithm = Algorithm.HMAC256(KEY);
                JWTVerifier verifier = JWT.require(algorithm)
                        .build();

                decodedJWT = verifier.verify(token);

                return new Success(new HttpPrincipal(decodedJWT.getSubject(), "ROLE_ADMIN"));
            } catch (JWTVerificationException exception) {
                exception.printStackTrace();
            }

            // correct format, but wrong user for this endpoint
            return new Failure(403);
        }
    }

}
