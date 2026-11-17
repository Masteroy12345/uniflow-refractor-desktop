package cm.uniflow.desktop.ui.views;

import cm.uniflow.desktop.services.CampusFirestoreService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Shared professional list presentation for records read directly from Firestore. */
public class CampusCollectionView extends ScrollPane {
    protected final String collection;
    protected final VBox contentRoot = new VBox(18);
    private final String title;
    private final String description;
    private final VBox records = new VBox(10);
    private final Label state = new Label("Connexion à Firestore…");
    private final TextField search = new TextField();
    private final List<Map<String, Object>> loaded = new ArrayList<>();

    public CampusCollectionView(String collection, String title, String description) {
        this.collection = collection;
        this.title = title;
        this.description = description;
        setFitToWidth(true);
        getStyleClass().add("scroll-pane");
        contentRoot.setPadding(new Insets(24));
        contentRoot.setStyle("-fx-background-color: transparent;");

        VBox heading = new VBox(7);
        heading.setStyle("-fx-background-color: linear-gradient(to bottom right, #12345B, #0D9488); -fx-background-radius: 18; -fx-padding: 22 26;");
        Label eyebrow = new Label("ESPACE UNIVERSITAIRE · DONNÉES SYNCHRONISÉES");
        eyebrow.setStyle("-fx-text-fill: #B6F2EA; -fx-font-size: 10px; -fx-font-weight: 800;");
        Label headingTitle = new Label(title);
        headingTitle.setStyle("-fx-text-fill: white; -fx-font-size: 23px; -fx-font-weight: 800;");
        Label subtitle = new Label(description);
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: #E0F2FE; -fx-font-size: 12px;");
        heading.getChildren().addAll(eyebrow, headingTitle, subtitle);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        search.setPromptText("Rechercher dans les données chargées…");
        search.getStyleClass().add("search-field");
        HBox.setHgrow(search, Priority.ALWAYS);
        search.textProperty().addListener((o, oldValue, newValue) -> render());
        Button refresh = new Button("Actualiser");
        refresh.getStyleClass().add("btn-secondary");
        refresh.setOnAction(event -> load());
        toolbar.getChildren().addAll(search, refresh);

        state.setWrapText(true);
        state.setStyle("-fx-text-fill: #64748B; -fx-padding: 16 4; -fx-font-size: 12px;");
        records.getChildren().add(state);
        contentRoot.getChildren().addAll(heading, toolbar, records);
        setContent(contentRoot);
        load();
    }

    protected final void appendSection(Node section) {
        contentRoot.getChildren().add(section);
    }

    protected final void load() {
        loaded.clear();
        records.getChildren().setAll(state);
        state.setText("Connexion à Firestore…");
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(28, 28);
        records.getChildren().add(progress);
        Thread worker = new Thread(() -> {
            try {
                List<Map<String, Object>> data = CampusFirestoreService.list(collection);
                Platform.runLater(() -> {
                    loaded.clear();
                    loaded.addAll(data);
                    render();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    records.getChildren().setAll();
                    state.setText("Données indisponibles : " + safeMessage(ex));
                    state.setStyle("-fx-text-fill: #9F1239; -fx-background-color: #FFF1F2; -fx-background-radius: 10; -fx-padding: 14; -fx-font-size: 12px;");
                    records.getChildren().add(state);
                });
            }
        }, "firestore-read-" + collection);
        worker.setDaemon(true);
        worker.start();
    }

    private void render() {
        records.getChildren().clear();
        state.setStyle("-fx-text-fill: #64748B; -fx-padding: 16 4; -fx-font-size: 12px;");
        String query = search.getText() == null ? "" : search.getText().trim().toLowerCase();
        List<Map<String, Object>> filtered = loaded.stream()
                .filter(row -> query.isEmpty() || row.toString().toLowerCase().contains(query))
                .sorted(Comparator.comparing(row -> String.valueOf(row.getOrDefault("title", row.getOrDefault("name", row.getOrDefault("code", "")))), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        if (filtered.isEmpty()) {
            state.setText(loaded.isEmpty()
                    ? "Aucune donnée publiée pour le moment. Cette vue n'affiche pas de contenu de démonstration."
                    : "Aucun résultat pour cette recherche.");
            records.getChildren().add(state);
            return;
        }
        Label count = new Label(filtered.size() + " enregistrement(s) chargé(s) depuis Firestore · " + collection);
        count.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px; -fx-padding: 0 4 4 4;");
        records.getChildren().add(count);
        for (Map<String, Object> row : filtered) records.getChildren().add(recordCard(row));
    }

    private VBox recordCard(Map<String, Object> row) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #DCE7EF; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 14 16;");
        String headline = first(row, "title", "name", "courseTitle", "courseName", "label", "code", "studentName", "displayName", "reference", "_id");
        Label titleLabel = new Label(headline);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-text-fill: #12345B; -fx-font-size: 14px; -fx-font-weight: 800;");
        card.getChildren().add(titleLabel);
        HBox values = new HBox(16);
        values.setAlignment(Pos.CENTER_LEFT);
        values.setStyle("-fx-padding: 2 0 0 0;");
        int shown = 0;
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey().startsWith("_") || entry.getKey().equals("title") || entry.getKey().equals("name")
                    || entry.getKey().equals("courseTitle") || entry.getKey().equals("courseName")
                    || entry.getKey().equals("label") || entry.getKey().equals("code")
                    || entry.getKey().equals("studentName") || entry.getKey().equals("displayName")) continue;
            if (entry.getValue() == null || String.valueOf(entry.getValue()).isBlank()) continue;
            Label value = new Label(humanize(entry.getKey()) + " : " + String.valueOf(entry.getValue()));
            value.setWrapText(true);
            value.setStyle("-fx-text-fill: #536779; -fx-font-size: 11px;");
            values.getChildren().add(value);
            if (++shown >= 5) break;
        }
        if (shown > 0) card.getChildren().add(values);
        return card;
    }

    protected static String first(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            Object value = row.get(key);
            if (value != null && !String.valueOf(value).isBlank()) return String.valueOf(value);
        }
        return "Enregistrement " + row.getOrDefault("_id", "Firestore");
    }

    private static String humanize(String key) {
        String spaced = key.replaceAll("([a-z])([A-Z])", "$1 $2").replace('_', ' ');
        return spaced.isEmpty() ? spaced : Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private static String safeMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? ex.getClass().getSimpleName() : ex.getMessage();
    }
}
