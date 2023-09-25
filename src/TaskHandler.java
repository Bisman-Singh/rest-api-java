import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class TaskHandler implements HttpHandler {
    private final ConcurrentHashMap<Long, Task> store = new ConcurrentHashMap<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        logRequest(method, path);

        try {
            if (path.equals("/tasks") || path.equals("/tasks/")) {
                switch (method) {
                    case "GET" -> handleGetAll(exchange);
                    case "POST" -> handleCreate(exchange);
                    default -> sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } else if (path.matches("/tasks/\\d+")) {
                long id = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
                switch (method) {
                    case "GET" -> handleGetById(exchange, id);
                    case "PUT" -> handleUpdate(exchange, id);
                    case "DELETE" -> handleDelete(exchange, id);
                    default -> sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Not found\"}");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid ID format\"}");
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        String json = Task.toJsonArray(store.values());
        sendResponse(exchange, 200, json);
    }

    private void handleGetById(HttpExchange exchange, long id) throws IOException {
        Task task = store.get(id);
        if (task == null) {
            sendResponse(exchange, 404, "{\"error\":\"Task not found\"}");
        } else {
            sendResponse(exchange, 200, task.toJson());
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        if (body.isBlank()) {
            sendResponse(exchange, 400, "{\"error\":\"Request body is empty\"}");
            return;
        }
        Task task = Task.fromJson(body);
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            sendResponse(exchange, 400, "{\"error\":\"Title is required\"}");
            return;
        }
        store.put(task.getId(), task);
        sendResponse(exchange, 201, task.toJson());
    }

    private void handleUpdate(HttpExchange exchange, long id) throws IOException {
        Task existing = store.get(id);
        if (existing == null) {
            sendResponse(exchange, 404, "{\"error\":\"Task not found\"}");
            return;
        }
        String body = readBody(exchange);
        if (body.isBlank()) {
            sendResponse(exchange, 400, "{\"error\":\"Request body is empty\"}");
            return;
        }
        existing.updateFrom(body);
        sendResponse(exchange, 200, existing.toJson());
    }

    private void handleDelete(HttpExchange exchange, long id) throws IOException {
        Task removed = store.remove(id);
        if (removed == null) {
            sendResponse(exchange, 404, "{\"error\":\"Task not found\"}");
        } else {
            sendResponse(exchange, 204, "");
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, statusCode == 204 ? -1 : bytes.length);
        if (statusCode != 204) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }

    private void logRequest(String method, String path) {
        System.out.printf("[%s] %s %s%n",
            java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
            method, path);
    }
}
