package cm.uniflow.desktop.services;

import cm.uniflow.desktop.config.AppConfig;
import cm.uniflow.desktop.models.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Firebase Authentication/Firestore REST client using an end-user ID token only. */
public final class FirebaseAuthService {
    private static final String AUTH_BASE = "https://identitytoolkit.googleapis.com/v1";
    private static final String FIRESTORE_BASE = "https://firestore.googleapis.com/v1";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static volatile String idToken;

    private FirebaseAuthService() {}

    public static String projectId() {
        String configured = firstNonBlank(System.getProperty("uniflow.firebase.projectId"),
                System.getenv("UNIFLOW_FIREBASE_PROJECT_ID"));
        return configured == null ? AppConfig.FIREBASE_PROJECT_ID : configured;
    }

    /** Public Firebase client API key from runtime configuration; never a private credential. */
    public static String clientApiKey() {
        return firstNonBlank(System.getProperty(AppConfig.FIREBASE_API_KEY_PROPERTY),
                System.getenv(AppConfig.FIREBASE_API_KEY_ENV), System.getenv("VITE_FIREBASE_API_KEY"));
    }

    /** No Firebase secret is bundled; the API key is a public client config supplied by the operator. */
    public static String missingConfigurationMessage() {
        if (apiKey() == null) {
            return "Configuration Firebase manquante : définissez UNIFLOW_FIREBASE_API_KEY ou "
                    + "-Duniflow.firebase.apiKey=<clé client>. Aucune clé Admin SDK n'est acceptée.";
        }
        return null;
    }

    public static User login(String email, String password) throws IOException {
        requireConfiguration();
        ObjectNode credentials = JSON_MAPPER.createObjectNode();
        credentials.put("email", email);
        credentials.put("password", password);
        credentials.put("returnSecureToken", true);
        JsonNode auth = post(AUTH_BASE + "/accounts:signInWithPassword?key=" + encoded(apiKey()), credentials, null);
        String token = requiredText(auth, "idToken");
        String uid = requiredText(auth, "localId");
        JsonNode profile = getUserProfile(uid, token);
        String status = field(profile, "status").toLowerCase(Locale.ROOT);
        if (!status.equals("active")) {
            throw new IOException("Ce compte universitaire est en attente de validation ou suspendu. Contactez l'administration de votre université.");
        }
        String role = field(profile, "role").toLowerCase(Locale.ROOT);
        if (!java.util.Set.of("student", "delegate", "teacher", "admin").contains(role)) {
            throw new IOException("Le profil ne contient pas un rôle campus valide.");
        }
        String displayName = firstNonBlank(field(profile, "displayName"), auth.path("displayName").asText(""), email);
        User user = new User(uid, displayName,
                firstNonBlank(field(profile, "email"), email),
                roleForUi(role), field(profile, "studentNumber"), field(profile, "programId"), field(profile, "level"));
        user.setFirstName(field(profile, "firstName"));
        user.setLastName(field(profile, "lastName"));
        user.setPhotoUrl(field(profile, "photoUrl"));
        user.setStatus(status);
        user.setUniversity(field(profile, "universityId"));
        user.setFaculty(field(profile, "facultyId"));
        idToken = token;
        AppConfig.setCurrentUser(user);
        return user;
    }

    /** Creates only a student profile in pending state; privileged roles are never accepted as input. */
    public static User registerStudent(String fullName, String email, String password,
                                       String university, String faculty, String program,
                                       String level, String studentNumber) throws IOException {
        requireConfiguration();
        ObjectNode credentials = JSON_MAPPER.createObjectNode();
        credentials.put("email", email);
        credentials.put("password", password);
        credentials.put("returnSecureToken", true);
        credentials.put("displayName", fullName);
        JsonNode auth = post(AUTH_BASE + "/accounts:signUp?key=" + encoded(apiKey()), credentials, null);
        String uid = requiredText(auth, "localId");
        String token = requiredText(auth, "idToken");

        ObjectNode document = JSON_MAPPER.createObjectNode();
        ObjectNode fields = document.putObject("fields");
        String[] nameParts = splitName(fullName);
        putString(fields, "uid", uid);
        putString(fields, "email", email);
        putString(fields, "displayName", fullName);
        putString(fields, "firstName", nameParts[0]);
        putString(fields, "lastName", nameParts[1]);
        putString(fields, "phone", "");
        putString(fields, "universityId", university);
        putString(fields, "facultyId", faculty);
        putString(fields, "programId", program);
        putString(fields, "level", level);
        putString(fields, "studentNumber", studentNumber);
        putString(fields, "role", "student");
        putString(fields, "status", "pending");
        putString(fields, "photoUrl", "");
        String timestamp = Instant.now().toString();
        fields.putObject("createdAt").put("timestampValue", timestamp);
        fields.putObject("updatedAt").put("timestampValue", timestamp);

        String documentUrl = FIRESTORE_BASE + "/projects/" + encoded(projectId())
                + "/databases/(default)/documents/users/" + encoded(uid);
        patch(documentUrl, document, token);
        idToken = null; // a pending profile is not an authenticated application session
        User pending = new User(uid, fullName, email, "student", studentNumber, program, level);
        pending.setFirstName(nameParts[0]);
        pending.setLastName(nameParts[1]);
        pending.setStatus("pending");
        pending.setUniversity(university);
        pending.setFaculty(faculty);
        return pending;
    }

    public static void logout() {
        idToken = null;
        AppConfig.setCurrentUser(null);
    }

    /** Available for future Firestore calls; never a service-account credential. */
    public static String getCurrentIdToken() {
        return idToken;
    }

    private static JsonNode getUserProfile(String uid, String token) throws IOException {
        String url = FIRESTORE_BASE + "/projects/" + encoded(projectId())
                + "/databases/(default)/documents/users/" + encoded(uid);
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + token).get().build();
        try (Response response = HTTP.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) throw httpError("Lecture du profil universitaire", response.code(), body);
            return JSON_MAPPER.readTree(body);
        }
    }

    private static JsonNode post(String url, JsonNode payload, String token) throws IOException {
        Request.Builder builder = new Request.Builder().url(url).post(RequestBody.create(payload.toString(), JSON));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return executeJson(builder.build(), "Authentification Firebase");
    }

    private static void patch(String url, JsonNode payload, String token) throws IOException {
        Request request = new Request.Builder().url(url).header("Authorization", "Bearer " + token)
                .patch(RequestBody.create(payload.toString(), JSON)).build();
        executeJson(request, "Création du profil étudiant Firestore");
    }

    private static JsonNode executeJson(Request request, String operation) throws IOException {
        try (Response response = HTTP.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) throw httpError(operation, response.code(), body);
            return body.isBlank() ? JSON_MAPPER.createObjectNode() : JSON_MAPPER.readTree(body);
        }
    }

    private static IOException httpError(String operation, int code, String body) {
        String detail = "";
        try {
            JsonNode root = JSON_MAPPER.readTree(body);
            detail = root.path("error").path("message").asText("");
        } catch (Exception ignored) {}
        if (detail.isBlank()) detail = "HTTP " + code;
        return new IOException(operation + " échouée : " + detail + " (HTTP " + code + "). Vérifiez la configuration et les règles Firebase.");
    }

    private static String field(JsonNode document, String fieldName) {
        JsonNode value = document.path("fields").path(fieldName);
        if (value.has("stringValue")) return value.path("stringValue").asText("");
        if (value.has("integerValue")) return value.path("integerValue").asText("");
        if (value.has("booleanValue")) return value.path("booleanValue").asText("");
        return "";
    }

    private static void putString(ObjectNode fields, String key, String value) {
        fields.putObject(key).put("stringValue", value == null ? "" : value.trim());
    }

    private static String roleForUi(String role) {
        return switch (role == null ? "" : role.toLowerCase(Locale.ROOT)) {
            case "delegate" -> "DELEGATE";
            case "teacher" -> "TEACHER";
            case "admin" -> "ADMIN";
            case "student" -> "student";
            default -> "";
        };
    }

    private static String requiredText(JsonNode node, String key) throws IOException {
        String value = node.path(key).asText("");
        if (value.isBlank()) throw new IOException("Réponse Firebase incomplète : champ " + key + " absent.");
        return value;
    }

    private static String[] splitName(String fullName) {
        String normalized = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        int separator = normalized.indexOf(' ');
        if (separator < 0) return new String[]{normalized, ""};
        return new String[]{normalized.substring(0, separator), normalized.substring(separator + 1)};
    }
    private static String apiKey() {
        return clientApiKey();
    }

    private static void requireConfiguration() throws IOException {
        String message = missingConfigurationMessage();
        if (message != null) throw new IOException(message);
        if (projectId().isBlank()) throw new IOException("Configuration Firebase manquante : projectId vide.");
    }

    private static String encoded(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value.trim();
        return null;
    }
}
