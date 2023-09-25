/**
 * User model for REST API.
 * Author: Bisman Singh <bismanmadaan1@gmail.com>
 */
public class User {
    public int id;
    public String name;
    public String email;

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String toJson() {
        return "{\"id\":" + id + ",\"name\":\"" + escape(name) + "\",\"email\":\"" + escape(email) + "\"}";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
