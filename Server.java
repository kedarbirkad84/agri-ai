import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Core Java backend: JDK HttpServer + JDBC (MySQL). Run: java Server */
public class Server {
    static final String DB_URL = "jdbc:mysql://localhost:3306/agri_ai?useSSL=false&allowPublicKeyRetrieval=true";
    static final String DB_USER = "root", DB_PASS = "kedar";   // <-- change to yours

    interface Api { String run(Map<String, String> q) throws Exception; }

    public static void main(String[] args) throws Exception {
        HttpServer s = HttpServer.create(new InetSocketAddress(8080), 0);
        api(s, "/api/crop", Server::crop);
        api(s, "/api/soil", Server::soil);
        api(s, "/api/disease", Server::disease);
        s.createContext("/", Server::staticFile);
        s.start();
        System.out.println("Running at http://localhost:8080");
    }

    // ---------- tools ----------
    static String crop(Map<String, String> q) throws Exception {
        String soil = q.get("soil");
        double ph = num(q, "ph"), rain = num(q, "rain"), temp = num(q, "temp");
        String sql = "SELECT name,season,tips FROM crops WHERE soil_type=? AND ? BETWEEN min_ph AND max_ph "
                   + "AND ? BETWEEN min_rain AND max_rain AND ? BETWEEN min_temp AND max_temp";
        StringBuilder sb = new StringBuilder("[");
        try (Connection c = db(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, soil); p.setDouble(2, ph); p.setDouble(3, rain); p.setDouble(4, temp);
            ResultSet r = p.executeQuery();
            while (r.next()) {
                if (sb.length() > 1) sb.append(',');
                sb.append(obj("name", r.getString(1), "season", r.getString(2), "tips", r.getString(3)));
            }
            log(c, "crop", soil + " pH" + ph + " rain" + rain + " temp" + temp);
        }
        return sb.append(']').toString();
    }

    static String soil(Map<String, String> q) throws Exception {
        double ph = num(q, "ph");
        String type = q.get("soil");
        List<String> tips = new ArrayList<>();
        if (ph < 6.0) tips.add("Soil is acidic: apply agricultural lime (2-4 quintals/acre).");
        else if (ph > 7.5) tips.add("Soil is alkaline: apply gypsum or elemental sulphur and organic compost.");
        else tips.add("pH is in the ideal range (6.0-7.5). Maintain with organic matter.");
        nutrient(tips, "Nitrogen", q.get("n"), "urea or compost/FYM");
        nutrient(tips, "Phosphorus", q.get("p"), "single super phosphate (SSP) or DAP");
        nutrient(tips, "Potassium", q.get("k"), "muriate of potash (MOP) or wood ash");
        switch (type) {
            case "Sandy": tips.add("Sandy soil drains fast: add compost and use drip irrigation."); break;
            case "Clay":  tips.add("Clay soil holds water: improve drainage and add sand/organic matter."); break;
            case "Black": tips.add("Black soil retains moisture: avoid working when wet; good for cotton."); break;
            default:      tips.add("Loamy soil is well balanced: ideal for most crops.");
        }
        StringBuilder sb = new StringBuilder("{\"advice\":[");
        for (int i = 0; i < tips.size(); i++) sb.append(i > 0 ? "," : "").append(str(tips.get(i)));
        sb.append("],\"crops\":[");
        try (Connection c = db(); PreparedStatement p = c.prepareStatement("SELECT name FROM crops WHERE soil_type=?")) {
            p.setString(1, type);
            ResultSet r = p.executeQuery();
            boolean first = true;
            while (r.next()) { sb.append(first ? "" : ",").append(str(r.getString(1))); first = false; }
            log(c, "soil", type + " pH" + ph);
        }
        return sb.append("]}").toString();
    }

    static String disease(Map<String, String> q) throws Exception {
        StringBuilder sb = new StringBuilder("[");
        try (Connection c = db(); PreparedStatement p = c.prepareStatement(
                "SELECT disease,cause,treatment FROM diseases WHERE crop=? AND symptom=?")) {
            p.setString(1, q.get("crop")); p.setString(2, q.get("symptom"));
            ResultSet r = p.executeQuery();
            while (r.next()) {
                if (sb.length() > 1) sb.append(',');
                sb.append(obj("disease", r.getString(1), "cause", r.getString(2), "treatment", r.getString(3)));
            }
            log(c, "disease", q.get("crop") + " / " + q.get("symptom"));
        }
        return sb.append(']').toString();
    }

    static void nutrient(List<String> t, String name, String level, String fert) {
        if ("low".equals(level)) t.add(name + " is low: add " + fert + ".");
        else if ("high".equals(level)) t.add(name + " is high: skip " + name.toLowerCase() + " fertilizer this season.");
    }

    // ---------- helpers ----------
    static Connection db() throws SQLException { return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS); }

    static void log(Connection c, String tool, String text) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("INSERT INTO search_log(tool,query_text) VALUES(?,?)")) {
            p.setString(1, tool); p.setString(2, text); p.executeUpdate();
        }
    }

    static double num(Map<String, String> q, String k) { return Double.parseDouble(q.getOrDefault(k, "0")); }

    static String str(String s) { return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }

    static String obj(String... kv) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < kv.length; i += 2) sb.append(i > 0 ? "," : "").append(str(kv[i])).append(':').append(str(kv[i + 1]));
        return sb.append('}').toString();
    }

    static Map<String, String> parse(String qs) throws UnsupportedEncodingException {
        Map<String, String> m = new HashMap<>();
        if (qs == null) return m;
        for (String pair : qs.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) m.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
        }
        return m;
    }

    static void api(HttpServer s, String path, Api a) {
        s.createContext(path, ex -> {
            String out; int code = 200;
            try { out = a.run(parse(ex.getRequestURI().getRawQuery())); }
            catch (Exception e) { e.printStackTrace(); code = 500; out = obj("error", String.valueOf(e.getMessage())); }
            send(ex, code, "application/json", out.getBytes(StandardCharsets.UTF_8));
        });
    }

    static void staticFile(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        Path f = Paths.get("web", path).normalize();
        if (!f.startsWith("web") || !Files.isRegularFile(f)) { send(ex, 404, "text/plain", "Not found".getBytes()); return; }
        String n = f.toString(), type = n.endsWith(".html") ? "text/html" : n.endsWith(".css") ? "text/css"
                 : n.endsWith(".js") ? "application/javascript" : n.endsWith(".svg") ? "image/svg+xml" : "application/octet-stream";
        send(ex, 200, type, Files.readAllBytes(f));
    }

    static void send(HttpExchange ex, int code, String type, byte[] body) throws IOException {
        ex.getResponseHeaders().add("Content-Type", type + "; charset=utf-8");
        ex.sendResponseHeaders(code, body.length);
        try (OutputStream o = ex.getResponseBody()) { o.write(body); }
    }
}
