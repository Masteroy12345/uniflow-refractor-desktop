package cm.uniflow.desktop.ui.components;

import cm.uniflow.desktop.config.AppConfig;
import cm.uniflow.desktop.config.ThemeManager;
import cm.uniflow.desktop.services.FirebaseAuthService;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class TopBar extends HBox {

    private final Label titleLabel = new Label("Tableau de bord");
    private final Label subtitleLabel = new Label("Portail Universitaire · Université de Yaoundé I");
    private final TextField searchField = new TextField();
    private final Label statusDot = new Label("●");
    private final Label statusText = new Label();
    private final HBox statusPill = new HBox(6);

    public TopBar(Runnable onThemeToggle, Runnable onLoginClick) {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(16);
        getStyleClass().add("topbar");

        VBox titleBox = new VBox(2);
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");
        subtitleLabel.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #64748B; -fx-font-weight: 500;");
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField.setPromptText("Rechercher un cours, un enseignant, une ressource...");
        searchField.setPrefWidth(300);
        searchField.getStyleClass().add("search-field");

        // Afficher uniquement l’état local de configuration/session Firebase, sans prétendre tester le réseau.
        statusPill.setAlignment(Pos.CENTER);
        statusPill.setStyle("-fx-background-color: #ECFDF5; -fx-border-color: #A7F3D0; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 4 12; -fx-cursor: hand;");
        statusDot.setStyle("-fx-text-fill: #10B981; -fx-font-size: 10px;");
        statusText.setStyle("-fx-text-fill: #047857; -fx-font-weight: 700; -fx-font-size: 11px;");
        statusPill.getChildren().addAll(statusDot, statusText);
        statusPill.setOnMouseClicked(e -> updateFirebaseStatus());
        AppConfig.currentUserProperty().addListener((obs, oldUser, newUser) -> updateFirebaseStatus());
        updateFirebaseStatus();

        // Theme Toggle Button
        Button themeBtn = new Button("🌓");
        themeBtn.getStyleClass().add("btn-secondary");
        themeBtn.setStyle("-fx-padding: 6 10; -fx-background-radius: 12;");
        themeBtn.setTooltip(new Tooltip("Basculer thème Clair / Sombre"));
        themeBtn.setOnAction(e -> onThemeToggle.run());

        // Account Button
        Button accountBtn = new Button("Mon Profil");
        accountBtn.getStyleClass().add("btn-secondary");
        accountBtn.setOnAction(e -> onLoginClick.run());

        getChildren().addAll(titleBox, spacer, searchField, statusPill, themeBtn, accountBtn);
    }

    private void updateFirebaseStatus() {
        boolean configured = FirebaseAuthService.missingConfigurationMessage() == null;
        boolean signedIn = FirebaseAuthService.getCurrentIdToken() != null;
        statusDot.setStyle("-fx-text-fill: " + (configured ? "#0D9488" : "#D97706") + "; -fx-font-size: 10px;");
        statusText.setText(!configured ? "Firebase à configurer" : signedIn ? "Firebase · session active" : "Firebase · déconnecté");
        statusText.setStyle("-fx-text-fill: " + (configured ? "#0F766E" : "#B45309") + "; -fx-font-weight: 700; -fx-font-size: 11px;");
        statusPill.setStyle("-fx-background-color: " + (configured ? "#F0FDFA" : "#FFFBEB")
                + "; -fx-border-color: " + (configured ? "#99F6E4" : "#FDE68A")
                + "; -fx-border-radius: 14; -fx-background-radius: 14; -fx-padding: 4 12;");
        Tooltip.install(statusPill, new Tooltip(configured
                ? "Configuration Firebase présente ; ce statut n'est pas un contrôle de connectivité."
                : FirebaseAuthService.missingConfigurationMessage()));
    }

    public void updateTitle(String title, String subtitle) {
        titleLabel.setText(title);
        subtitleLabel.setText(subtitle);
    }
}
