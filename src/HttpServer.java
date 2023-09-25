import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Simple HTTP REST API server on port 8080.
 * Endpoints: GET/POST /api/users, GET /api/users/:id
 * Author: Bisman Singh <bismanmadaan1@gmail.com>
 */
public class HttpServer {
    private static final int PORT = 8080;
    private static final Map<Integer, User> users = new ConcurrentHashMap<>();
    private static int nextId = 1;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("REST API on http://localhost:" + PORT);
            ExecutorService pool = Executors.newCachedThreadPool();
            while (true) {
                Socket client = server.accept();
                pool.submit(() -> handle(client));
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handle(Socket client) {
        try (InputStream is = client.getInputStream();
             OutputStream out = client.getOutputStream()) {

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String line = in.readLine();
            if (line == null) return;

            String[] parts = line.split(" ");
            String method = parts[0];
            String path = parts.length > 1 ? parts[1] : "/";

            int contentLength = 0;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(line.substring(15).trim());
                }
            }

            StringBuilder body = new StringBuilder();
            for (int i = 0; i < contentLength; i++) {
                int b = is.read();
                if (b < 0) break;
                body.append((char) b);
            }
            String bodyStr = body.toString();

            String response = process(method, path, bodyStr);
            String http = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: " + response.length() + "\r\n\r\n" + response;
            out.write(http.getBytes());
            out.flush();
        } catch (IOException e) {
            // ignore
        } finally {
            try { client.close(); } catch (IOException ignored) {}
        }
    }

    private static String process(String method, String path, String body) {
        if (path.equals("/api/users")) {
            if ("GET".equals(method)) {
                StringBuilder sb = new StringBuilder("[");
                for (User u : users.values()) {
                    if (sb.length() > 1) sb.append(",");
                    sb.append(u.toJson());
                }
                sb.append("]");
                return sb.toString();
            }
            if ("POST".equals(method)) {
                String name = extract(body, "name");
                String email = extract(body, "email");
                User u = new User(nextId++, name, email);
                users.put(u.id, u);
                return u.toJson();
            }
        }
        if (path.startsWith("/api/users/")) {
            try {
                int id = Integer.parseInt(path.substring("/api/users/".length()).split("\\?")[0]);
                User u = users.get(id);
                if (u != null) return u.toJson();
            } catch (NumberFormatException ignored) {}
        }
        return "{\"error\":\"Not found\"}";
    }

    private static String extract(String json, String key) {
        String search = "\"" + key + "\":\"";
        int i = json.indexOf(search);
        if (i < 0) return "";
        i += search.length();
        int j = i;
        while (j < json.length() && json.charAt(j) != '"') {
            if (json.charAt(j) == '\\') j++;
            j++;
        }
        return json.substring(i, j).replace("\\\"", "\"");
    }
}
