import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class Task {
    private static final AtomicLong ID_GEN = new AtomicLong(0);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private long id;
    private String title;
    private String description;
    private String status;
    private String createdAt;

    public Task() {
        this.id = ID_GEN.incrementAndGet();
        this.status = "pending";
        this.createdAt = LocalDateTime.now().format(FMT);
    }

    public Task(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String toJson() {
        return """
            {"id":%d,"title":"%s","description":"%s","status":"%s","createdAt":"%s"}""".formatted(
            id,
            escapeJson(title),
            escapeJson(description),
            escapeJson(status),
            escapeJson(createdAt)
        ).strip();
    }

    public static Task fromJson(String json) {
        Task task = new Task();
        String title = extractJsonString(json, "title");
        String description = extractJsonString(json, "description");
        String status = extractJsonString(json, "status");

        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (status != null) task.setStatus(status);
        return task;
    }

    public void updateFrom(String json) {
        String t = extractJsonString(json, "title");
        String d = extractJsonString(json, "description");
        String s = extractJsonString(json, "status");
        if (t != null) this.title = t;
        if (d != null) this.description = d;
        if (s != null) this.status = s;
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return null;

        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx == -1) return null;

        String rest = json.substring(colonIdx + 1).stripLeading();
        if (rest.startsWith("\"")) {
            int end = rest.indexOf('"', 1);
            if (end == -1) return null;
            return unescapeJson(rest.substring(1, end));
        }
        return null;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }

    public static String toJsonArray(Iterable<Task> tasks) {
        var sb = new StringBuilder("[");
        boolean first = true;
        for (Task t : tasks) {
            if (!first) sb.append(",");
            sb.append(t.toJson());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
