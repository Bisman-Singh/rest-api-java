import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/tasks", new TaskHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("REST API server started on http://localhost:" + port);
        System.out.println("Endpoints:");
        System.out.println("  GET    /tasks       - List all tasks");
        System.out.println("  GET    /tasks/{id}  - Get task by ID");
        System.out.println("  POST   /tasks       - Create a task");
        System.out.println("  PUT    /tasks/{id}  - Update a task");
        System.out.println("  DELETE /tasks/{id}  - Delete a task");
        System.out.println();
        System.out.println("Press Ctrl+C to stop.");
    }
}
