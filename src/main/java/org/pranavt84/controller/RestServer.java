package org.pranavt84.controller;

import com.sun.net.httpserver.*;
import org.pranavt84.model.DataInput;
import org.pranavt84.service.DataInputService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class RestServer {
    
    private final int port;
    private final DataInputService service;
    private HttpServer server;
    
    public RestServer(int port, DataInputService service) {
        this.port = port;
        this.service = service;
    }
    
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/encode", this::handleEncode);
        server.createContext("/decode", this::handleDecode);
        server.createContext("/health", this::handleHealth);
        
        server.setExecutor(null); // Default executor
        server.start();
        
        System.out.println("Server started on port " + port);
        System.out.println("Available endpoints:");
        System.out.println("  POST http://localhost:" + port + "/encode");
        System.out.println("  POST http://localhost:" + port + "/decode");
        System.out.println("  GET  http://localhost:" + port + "/health");
    }

    private void handleEncode(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            return;
        }
        
        try {
            String requestBody = readRequestBody(exchange);
            System.out.println("Encode request: " + requestBody);

            DataInput input = parseJsonInput(requestBody);

            long startTime = System.nanoTime();
            String encoded = service.encode(input);
            long encodeTime = (System.nanoTime() - startTime) / 1000;


            String response = String.format(
                    "{\"encoded\": \"%s\", \"size\": %d, \"sizeUnit\": \"bytes\", \"encodeTime\": %d, \"encodeTimeUnit\": \"microseconds\"}",
                    encoded,
                    encoded.length() / 2,
                    encodeTime
            );
            
            sendResponse(exchange, 200, response);
            System.out.println("Encoded successfully: " + (encoded.length() / 2) + " bytes");
        } catch (Exception e) {
            String error = String.format("{\"error\": \"%s\"}", escapeJson(e.getMessage()));
            sendResponse(exchange, 400, error);
            System.err.println("Encode error: " + e.getMessage());
        }
    }

    private void handleDecode(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            return;
        }
        
        try {
            String requestBody = readRequestBody(exchange);
            System.out.println("Decode request");

            String hexEncoded = extractHexFromJson(requestBody);

            long startTime = System.nanoTime();
            DataInput decoded = service.decode(hexEncoded);
            long decodeTime = (System.nanoTime() - startTime) / 1000; // μs

            String decodedJson = dataInputToJson(decoded);
            String response = String.format(
                    "{\"decoded\": %s, \"decodeTime\": %d, \"decodeTimeUnit\": \"microseconds\"}",
                    decodedJson,
                    decodeTime
            );
            
            sendResponse(exchange, 200, response);
            System.out.println("Decoded successfully");
            
        } catch (Exception e) {
            String error = String.format("{\"error\": \"%s\"}", escapeJson(e.getMessage()));
            sendResponse(exchange, 400, error);
            System.err.println("Decode error: " + e.getMessage());
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, "{\"status\": \"ok\"}");
    }
    

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) 
            throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // CORS
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private DataInput parseJsonInput(String json) {
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new IllegalArgumentException("Input must be a JSON array starting with [ and ending with ]");
        }
        return parseArray(json.substring(1, json.length() - 1));
    }

    private DataInput parseArray(String content) {
        DataInput result = new DataInput();
        
        if (content.trim().isEmpty()) {
            return result; // Empty array
        }
        
        List<String> tokens = tokenize(content);
        
        for (String token : tokens) {
            token = token.trim();
            
            if (token.isEmpty()) {
                continue;
            }
            
            if (token.startsWith("\"") && token.endsWith("\"")) {
                String str = token.substring(1, token.length() - 1);
                str = unescapeJson(str);
                result.add(str);
            } else if (token.startsWith("[") && token.endsWith("]")) {
                result.add(parseArray(token.substring(1, token.length() - 1)));
            } else {
                try {
                    int value = Integer.parseInt(token);
                    result.add(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + token);
                }
            }
        }
        
        return result;
    }

    private List<String> tokenize(String content) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int bracketDepth = 0;
        boolean inString = false;
        boolean escaping = false;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (escaping) {
                current.append(c);
                escaping = false;
                continue;
            }
            
            if (c == '\\' && inString) {
                current.append(c);
                escaping = true;
                continue;
            }
            
            if (c == '"') {
                inString = !inString;
                current.append(c);
            } else if (c == '[' && !inString) {
                bracketDepth++;
                current.append(c);
            } else if (c == ']' && !inString) {
                bracketDepth--;
                current.append(c);
            } else if (c == ',' && bracketDepth == 0 && !inString) {
                // Top-level comma - split here
                if (current.length() > 0) {
                    tokens.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            tokens.add(current.toString().trim());
        }
        
        return tokens;
    }

    private String dataInputToJson(DataInput input) {
        StringBuilder sb = new StringBuilder("[");
        List<Object> elements = input.getElements();
        
        for (int i = 0; i < elements.size(); i++) {
            Object elem = elements.get(i);
            
            if (elem instanceof String) {
                sb.append("\"").append(escapeJson((String) elem)).append("\"");
            } else if (elem instanceof Integer) {
                sb.append(elem);
            } else if (elem instanceof DataInput) {
                sb.append(dataInputToJson((DataInput) elem));
            }
            
            if (i < elements.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
    

    private String unescapeJson(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\b", "\b")
                .replace("\\f", "\f");
    }

    private String extractHexFromJson(String json) {
        json = json.trim();

        if (!json.startsWith("{")) {
            return json;
        }

        int start = json.indexOf("\"encoded\"");
        if (start == -1) {
            throw new IllegalArgumentException("Missing 'encoded' field in JSON");
        }
        
        start = json.indexOf(":", start) + 1;
        start = json.indexOf("\"", start) + 1;
        int end = json.indexOf("\"", start);
        
        if (end == -1) {
            throw new IllegalArgumentException("Invalid JSON format for 'encoded' field");
        }
        
        return json.substring(start, end);
    }

    private boolean equals(DataInput a, DataInput b) {
        if (a.getElements().size() != b.getElements().size()) {
            return false;
        }
        
        for (int i = 0; i < a.getElements().size(); i++) {
            Object aElem = a.getElements().get(i);
            Object bElem = b.getElements().get(i);
            
            if (aElem instanceof DataInput && bElem instanceof DataInput) {
                if (!equals((DataInput) aElem, (DataInput) bElem)) {
                    return false;
                }
            } else if (!aElem.equals(bElem)) {
                return false;
            }
        }
        
        return true;
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

}