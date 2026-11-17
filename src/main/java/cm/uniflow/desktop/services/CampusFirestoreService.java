package cm.uniflow.desktop.services;

import cm.uniflow.desktop.config.AppConfig;
import cm.uniflow.desktop.models.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Firestore REST client restricted to the campus contract and signed-in user's ID token. */
public final class CampusFirestoreService {
    private static final String BASE = "https://firestore.googleapis.com/v1/projects/";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS).readTimeout(25, TimeUnit.SECONDS).build();
    private static final Set<String> COLLECTIONS = Set.of(
            "academic_courses", "academic_schedules", "academic_enrollments",
            "academic_assignments", "academic_submissions", "academic_grades",
            "attendance_sessions", "attendance_records", "classrooms", "notifications",
            "academic_library", "tuition_payments", "scholarships", "users");

    private CampusFirestoreService() {}

    /** Every collection query is role-aware and constrained by a field required by firestore.rules. */
    public static List<Map<String, Object>> list(String collection) throws IOException {
        User user = requireUser();
        String role = normalizeRole(user.getRole());
        String uid = user.getId();
        String universityId = user.getUniversity();
        if (!COLLECTIONS.contains(collection)) throw new IOException("Collection hors contrat campus : " + collection);
        if (uid == null || uid.isBlank()) throw new IOException("UID Firebase manquant pour la session.");
        boolean staff = role.equals("admin") || role.equals("teacher") || role.equals("delegate");
        List<Filter> filters = new ArrayList<>();
        switch (collection) {
            case "users" -> {
                if (!role.equals("admin")) throw new IOException("L’annuaire complet est réservé aux administrateurs campus.");
                requireCampusId(universityId, filters);
            }
            case "academic_courses", "academic_schedules", "academic_assignments" -> {
                requireCampusId(universityId, filters);
                if (!staff) filters.add(Filter.eq("isPublished", true));
            }
            case "academic_enrollments", "academic_submissions", "academic_grades", "attendance_records" -> {
                if (staff) requireCampusId(universityId, filters);
                else filters.add(Filter.eq("studentUid", uid));
            }
            case "attendance_sessions", "classrooms" -> requireCampusId(universityId, filters);
            case "notifications" -> filters.add(Filter.eq("recipientUid", uid));
            case "academic_library", "scholarships" -> {
                requireCampusId(universityId, filters);
                filters.add(Filter.eq("isPublished", true));
            }
            case "tuition_payments" -> {
                if (role.equals("admin")) requireCampusId(universityId, filters);
                else if (role.equals("student")) filters.add(Filter.eq("studentUid", uid));
                else throw new IOException("Les dossiers de frais sont réservés aux étudiants concernés et aux administrateurs.");
            }
            default -> throw new IOException("Collection non prise en charge : " + collection);
        }
        String token = requireSession();
        ObjectNode body = structuredQuery(collection, filters, 100);
        String url = BASE + enc(FirebaseAuthService.projectId())
                + "/databases/(default)/documents:runQuery?key=" + enc(FirebaseAuthService.clientApiKey());
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + token)
                .post(RequestBody.create(body.toString(), JSON)).build();
        JsonNode response = execute(request, "Lecture Firestore filtrée (" + collection + ")");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (JsonNode result : response) {
            JsonNode doc = result.path("document");
            if (doc.isMissingNode() || doc.isNull()) continue;
            Map<String, Object> fields = decodeFields(doc.path("fields"));
            String name = doc.path("name").asText("");
            int slash = name.lastIndexOf('/');
            if (slash >= 0) fields.put("_id", name.substring(slash + 1));
            rows.add(fields);
        }
        return rows;
    }

    /** Students may create only their own academic submission; no generic writes to arbitrary collections. */
    public static void createSubmission(Map<String, Object> values) throws IOException {
        User user = requireUser();
        if (!"student".equals(normalizeRole(user.getRole())))
            throw new IOException("Seul un compte étudiant actif peut soumettre un devoir.");
        if (user.getUniversity() == null || user.getUniversity().isBlank())
            throw new IOException("Le profil actif ne contient pas d’université (universityId).");
        String token = requireSession();
        ObjectNode body = MAPPER.createObjectNode();
        ObjectNode fields = body.putObject("fields");
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (Set.of("studentUid", "universityId", "createdAt", "updatedAt", "role", "status", "uid").contains(entry.getKey()))
                throw new IOException("Champ réservé ou géré par le client : " + entry.getKey());
            encode(fields, entry.getKey(), entry.getValue());
        }
        encode(fields, "studentUid", user.getId());
        encode(fields, "universityId", user.getUniversity());
        fields.putObject("createdAt").put("timestampValue", Instant.now().toString());
        String url = BASE + enc(FirebaseAuthService.projectId())
                + "/databases/(default)/documents/academic_submissions?key=" + enc(FirebaseAuthService.clientApiKey());
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + token)
                .post(RequestBody.create(body.toString(), JSON)).build();
        execute(request, "Création du dépôt académique");
    }

    static ObjectNode structuredQuery(String collection, List<Filter> filters, int limit) {
        if (filters == null || filters.isEmpty()) throw new IllegalArgumentException("Une requête Firestore doit avoir un filtre de sécurité.");
        ObjectNode body = MAPPER.createObjectNode();
        ObjectNode query = body.putObject("structuredQuery");
        query.putArray("from").addObject().put("collectionId", collection);
        if (filters.size() == 1) {
            filters.get(0).write(query.putObject("where"));
        } else {
            ObjectNode composite = query.putObject("where").putObject("compositeFilter");
            composite.put("op", "AND");
            ArrayNode clauses = composite.putArray("filters");
            for (Filter filter : filters) filter.write(clauses.addObject());
        }
        query.put("limit", Math.min(Math.max(limit, 1), 100));
        return body;
    }

    record Filter(String field, Object value) {
        static Filter eq(String field, Object value) { return new Filter(field, value); }
        void write(ObjectNode node) {
            ObjectNode filter = node.putObject("fieldFilter");
            filter.putObject("field").put("fieldPath", field);
            filter.put("op", "EQUAL");
            ObjectNode encoded = filter.putObject("value");
            if (value instanceof Boolean b) encoded.put("booleanValue", b);
            else encoded.put("stringValue", String.valueOf(value));
        }
    }

    private static void requireCampusId(String universityId, List<Filter> filters) throws IOException {
        if (universityId == null || universityId.isBlank()) throw new IOException("Le profil actif ne contient pas d’université (universityId).");
        filters.add(Filter.eq("universityId", universityId));
    }

    private static User requireUser() throws IOException {
        User user = AppConfig.getCurrentUser();
        if (user == null) throw new IOException("Session Firebase requise. Connectez-vous avec votre compte universitaire.");
        return user;
    }
    private static String requireSession() throws IOException {
        String message = FirebaseAuthService.missingConfigurationMessage();
        if (message != null) throw new IOException(message);
        String token = FirebaseAuthService.getCurrentIdToken();
        if (token == null || token.isBlank() || AppConfig.getCurrentUser() == null)
            throw new IOException("Session Firebase requise. Connectez-vous avec votre compte universitaire.");
        return token;
    }

    private static JsonNode execute(Request request, String operation) throws IOException {
        try (Response response = HTTP.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                String detail = "HTTP " + response.code();
                try { detail = MAPPER.readTree(body).path("error").path("message").asText(detail); }
                catch (Exception ignored) { }
                throw new IOException(operation + " impossible : " + detail
                        + ". Vérifiez les règles d'accès Firestore et la configuration Firebase.");
            }
            return body.isBlank() ? MAPPER.createArrayNode() : MAPPER.readTree(body);
        }
    }

    private static Map<String, Object> decodeFields(JsonNode fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        fields.fields().forEachRemaining(entry -> result.put(entry.getKey(), decode(entry.getValue())));
        return result;
    }

    private static Object decode(JsonNode value) {
        if (value.has("stringValue")) return value.path("stringValue").asText();
        if (value.has("integerValue")) {
            try { return Long.parseLong(value.path("integerValue").asText()); }
            catch (NumberFormatException ignored) { return value.path("integerValue").asText(); }
        }
        if (value.has("doubleValue")) return value.path("doubleValue").asDouble();
        if (value.has("booleanValue")) return value.path("booleanValue").asBoolean();
        if (value.has("timestampValue")) return value.path("timestampValue").asText();
        if (value.has("referenceValue")) return value.path("referenceValue").asText();
        if (value.has("nullValue")) return "";
        if (value.has("arrayValue")) {
            List<Object> items = new ArrayList<>();
            for (JsonNode item : value.path("arrayValue").path("values")) items.add(decode(item));
            return items;
        }
        if (value.has("mapValue")) return decodeFields(value.path("mapValue").path("fields"));
        return "";
    }

    private static void encode(ObjectNode fields, String key, Object value) {
        ObjectNode field = fields.putObject(key);
        if (value instanceof Boolean b) field.put("booleanValue", b);
        else if (value instanceof Integer || value instanceof Long) field.put("integerValue", String.valueOf(value));
        else if (value instanceof Number n) field.put("doubleValue", n.doubleValue());
        else field.put("stringValue", value == null ? "" : String.valueOf(value));
    }

    private static String normalizeRole(String role) { return role == null ? "" : role.trim().toLowerCase(Locale.ROOT); }
    private static String enc(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
