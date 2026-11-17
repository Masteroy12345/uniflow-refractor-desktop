package cm.uniflow.desktop.ui.components;

import cm.uniflow.desktop.config.AppConfig;
import cm.uniflow.desktop.models.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Sidebar extends VBox {

    public enum NavSection {
        DASHBOARD("Tableau de bord", "/icons/dashboard.png", "PÉDAGOGIE"),
        SCHEDULE("Emploi du temps", "/icons/schedule.png", "PÉDAGOGIE"),
        COURSES("Mes Cours & UEs", "/icons/courses.png", "PÉDAGOGIE"),
        ASSIGNMENTS("Devoirs & Livrables", "/icons/assignments.png", "PÉDAGOGIE"),
        GRADES("Notes & Relevé", "/icons/grades.png", "PÉDAGOGIE"),

        ACTUALITES("Actualités Campus", "/icons/analytics.png", "COMMUNICATION"),
        VISIO("Visioconférence · état", "/icons/visio.png", "COMMUNICATION"),

        ATTENDANCE("Présences", "/icons/attendance.png", "SERVICES CAMPUS"),
        ATTENDANCE_MANAGE("Émargement Délégué", "/icons/coordination.png", "SERVICES CAMPUS"),
        ZLIBRARY("Z-Library", "/icons/library.png", "SERVICES CAMPUS"),
        SENTINELLE("Sentinelle · état", "/icons/sentinelle.png", "SERVICES CAMPUS"),
        CLASSROOMS("Salles & Amphis", "/icons/rooms.png", "SERVICES CAMPUS"),
        DIRECTORY("Annuaire Campus", "/icons/students.png", "SERVICES CAMPUS"),
        PAYMENTS("Scolarité & Bourses", "/icons/paiements.png", "SERVICES CAMPUS"),

        SETTINGS("Paramètres", "/icons/settings.png", "SYSTÈME");

        private final String title;
        private final String iconPath;
        private final String category;

        NavSection(String title, String iconPath, String category) {
            this.title = title;
            this.iconPath = iconPath;
            this.category = category;
        }

        public String getTitle() { return title; }
        public String getIconPath() { return iconPath; }
        public String getCategory() { return category; }
    }

    private final Consumer<NavSection> navigationHandler;
    private final Runnable logoutHandler;
    private final Map<NavSection, Button> navButtons = new HashMap<>();
    private NavSection currentSection = NavSection.DASHBOARD;
    private final Label userNameLabel = new Label();
    private final Label userMailLabel = new Label();
    private final Label avatarInitials = new Label();

    public Sidebar(Consumer<NavSection> navigationHandler, Runnable logoutHandler) {
        this.navigationHandler = navigationHandler;
        this.logoutHandler = logoutHandler;
        setPrefWidth(270);
        setMinWidth(250);
        getStyleClass().add("sidebar");
        setSpacing(4);

        buildHeader();
        buildNavigation();
        buildFooter();

        updateUserProfile(AppConfig.getCurrentUser());
        AppConfig.currentUserProperty().addListener((obs, oldVal, newVal) -> updateUserProfile(newVal));
    }

    private void buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(6, 6, 12, 6));

        ImageView logoView = loadIcon("/brand/uniflow_logo_final.png", 36, 36);
        if (logoView == null) {
            logoView = loadIcon("/brand/uniflow_logo.png", 36, 36);
        }

        VBox titleBox = new VBox(2);
        Label title = new Label("UniFlow");
        title.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("Desktop Enterprise · UY1");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-font-weight: 600;");
        titleBox.getChildren().addAll(title, subtitle);

        if (logoView != null) {
            header.getChildren().add(logoView);
        }
        header.getChildren().add(titleBox);

        getChildren().add(header);
    }

    private void buildNavigation() {
        VBox navBox = new VBox(3);
        navBox.setPadding(new Insets(0, 4, 0, 0));

        String lastCategory = "";
        for (NavSection section : NavSection.values()) {
            if (!section.getCategory().equals(lastCategory)) {
                lastCategory = section.getCategory();
                Label catHeader = new Label(lastCategory);
                catHeader.setStyle("-fx-font-size: 10px; -fx-font-weight: 800; -fx-text-fill: #94A3B8; -fx-padding: 8 6 3 6;");
                navBox.getChildren().add(catHeader);
            }

            Button btn = createNavButton(section);
            navButtons.put(section, btn);
            navBox.getChildren().add(btn);
        }

        ScrollPane scroll = new ScrollPane(navBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        scroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().add(scroll);
    }

    private Button createNavButton(NavSection section) {
        Button btn = new Button(section.getTitle());
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("sidebar-nav-btn");

        ImageView icon = loadIcon(section.getIconPath(), 20, 20);
        if (icon != null) {
            btn.setGraphic(icon);
            btn.setGraphicTextGap(10);
        }

        btn.setOnAction(e -> selectSection(section));
        return btn;
    }

    public void selectSection(NavSection section) {
        this.currentSection = section;
        for (Map.Entry<NavSection, Button> entry : navButtons.entrySet()) {
            Button b = entry.getValue();
            b.getStyleClass().remove("sidebar-nav-btn-active");
            if (entry.getKey() == section) {
                b.getStyleClass().add("sidebar-nav-btn-active");
            }
        }
        if (navigationHandler != null) {
            navigationHandler.accept(section);
        }
    }

    private void buildFooter() {
        VBox footer = new VBox(8);
        footer.setPadding(new Insets(10, 6, 6, 6));

        // Bento User Profile Card
        HBox profileCard = new HBox(10);
        profileCard.setAlignment(Pos.CENTER_LEFT);
        profileCard.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 10;");

        avatarInitials.setMinSize(36, 36);
        avatarInitials.setPrefSize(36, 36);
        avatarInitials.setAlignment(Pos.CENTER);
        avatarInitials.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 18; -fx-text-fill: #1E3A8A; -fx-font-size: 12px; -fx-font-weight: 800;");

        VBox userTextBox = new VBox(2);
        userNameLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 12px; -fx-text-fill: #0F172A;");
        userMailLabel.setStyle("-fx-font-size: 10.5px; -fx-text-fill: #2563EB; -fx-font-weight: 600;");
        userTextBox.getChildren().addAll(userNameLabel, userMailLabel);
        HBox.setHgrow(userTextBox, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪");
        logoutBtn.setTooltip(new Tooltip("Se déconnecter"));
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 4;");
        logoutBtn.setOnAction(e -> {
            if (logoutHandler != null) logoutHandler.run();
        });

        profileCard.getChildren().addAll(avatarInitials, userTextBox, logoutBtn);

        footer.getChildren().add(profileCard);
        getChildren().add(footer);
    }

    private void updateUserProfile(User user) {
        if (user != null) {
            userNameLabel.setText(user.getName());
            userMailLabel.setText(user.getEmail() == null || user.getEmail().isBlank()
                    ? "Adresse e-mail non renseignée" : user.getEmail());
            avatarInitials.setText(initials(user.getName()));
        } else {
            userNameLabel.setText("Non connecté");
            userMailLabel.setText("connexion requise");
            avatarInitials.setText("UF");
        }
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "UF";
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    private ImageView loadIcon(String path, double w, double h) {
        try {
            var url = getClass().getResource(path);
            if (url != null) {
                ImageView iv = new ImageView(new Image(url.toExternalForm(), w, h, true, true));
                iv.setFitWidth(w);
                iv.setFitHeight(h);
                return iv;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
