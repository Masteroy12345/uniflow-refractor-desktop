package cm.uniflow.desktop.ui.views;

import cm.uniflow.desktop.config.AppConfig;
import cm.uniflow.desktop.models.User;
import cm.uniflow.desktop.services.FirebaseAuthService;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class LoginView extends HBox {

    private final Runnable onLoginSuccess;
    private final Runnable onGoToRegister;

    private final TextField identifierField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label errorLabel = new Label();
    private final Button loginBtn = new Button("Se connecter");
    private final ProgressIndicator spinner = new ProgressIndicator();

    public LoginView(Runnable onLoginSuccess, Runnable onGoToRegister) {
        this.onLoginSuccess = onLoginSuccess;
        this.onGoToRegister = onGoToRegister;

        setAlignment(Pos.CENTER);
        getStyleClass().add("app-container");

        buildShowcasePane();
        buildLoginCard();
    }

    private void buildShowcasePane() {
        VBox showcase = new VBox(24);
        showcase.setAlignment(Pos.CENTER_LEFT);
        showcase.setPadding(new Insets(48));
        showcase.setPrefWidth(550);
        showcase.setStyle("-fx-background-color: linear-gradient(to bottom right, #1E3A8A 0%, #0D9488 100%);");
        HBox.setHgrow(showcase, Priority.ALWAYS);

        // Logo
        HBox logoBox = new HBox(12);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        try {
            var url = getClass().getResource("/brand/uniflow-campus-mark.png");
            if (url != null) {
                ImageView iv = new ImageView(new Image(url.toExternalForm(), 42, 42, true, true));
                logoBox.getChildren().add(iv);
            }
        } catch (Exception ignored) {}
        Label brand = new Label("UniFlow");
        brand.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #FFFFFF;");
        Label edition = new Label("Campus numérique");
        edition.setStyle("-fx-font-size: 13px; -fx-text-fill: #5EEAD4; -fx-font-weight: 700;");
        logoBox.getChildren().addAll(brand, edition);

        Label headline = new Label("L'excellence universitaire africaine unifiée en un seul espace.");
        headline.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #FFFFFF; -fx-line-spacing: 4;");
        headline.setWrapText(true);

        Label sub = new Label("Accès institutionnel aux emplois du temps, cours, présences, résultats et services universitaires.");
        sub.setStyle("-fx-font-size: 14px; -fx-text-fill: #E0F2FE; -fx-line-spacing: 3;");
        sub.setWrapText(true);

        // Pillars
        VBox pillars = new VBox(10);
        pillars.getChildren().addAll(
                createPillarRow("01", "Emplois du temps et salles de cours"),
                createPillarRow("02", "Présence et suivi universitaire"),
                createPillarRow("03", "Cours, notes et devoirs"),
                createPillarRow("04", "Bourses et frais de scolarité")
        );

        HBox brandMark = new HBox(12);
        brandMark.setAlignment(Pos.CENTER_LEFT);
        try {
            var url = getClass().getResource("/brand/uniflow-campus.png");
            if (url != null) {
                ImageView iv = new ImageView(new Image(url.toExternalForm(), 280, 70, true, true));
                brandMark.getChildren().add(iv);
            }
        } catch (Exception ignored) {}

        Region sp = new Region();
        VBox.setVgrow(sp, Priority.ALWAYS);

        Label copy = new Label("Conçu et propulsé par KERNEL FORGE · Université de Yaoundé I");
        copy.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255, 255, 255, 0.65);");

        showcase.getChildren().addAll(logoBox, headline, sub, pillars, brandMark, sp, copy);
        getChildren().add(showcase);
    }

    private HBox createPillarRow(String emoji, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255, 255, 255, 0.12); -fx-border-color: rgba(255, 255, 255, 0.18); -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 8 14;");
        Label em = new Label(emoji);
        em.setMinWidth(28);
        em.setStyle("-fx-font-size: 11px; -fx-text-fill: #5EEAD4; -fx-font-weight: 800;");
        Label t = new Label(text);
        t.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 12.5px; -fx-font-weight: 600;");
        row.getChildren().addAll(em, t);
        return row;
    }

    private void buildLoginCard() {
        VBox rightPane = new VBox();
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setPadding(new Insets(48));
        rightPane.setPrefWidth(500);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        VBox card = new VBox(18);
        card.setMaxWidth(420);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-radius: 24; -fx-background-radius: 24; -fx-padding: 36; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 20, 0, 0, 6);");

        Label eyebrow = new Label("PORTAIL D'ACCÈS SÉCURISÉ");
        eyebrow.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: #2563EB; -fx-letter-spacing: 1px;");

        Label title = new Label("Bienvenue sur votre espace campus");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #0F172A;");

        Label subtitle = new Label("Connectez-vous pour retrouver vos cours, vos notes et vos présences.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        subtitle.setWrapText(true);

        // Error message container
        errorLabel.setStyle("-fx-background-color: #FEF2F2; -fx-border-color: #FECACA; -fx-text-fill: #B91C1C; -fx-font-weight: 700; -fx-font-size: 11.5px; -fx-padding: 8 12; -fx-background-radius: 10;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Fields
        VBox idBox = new VBox(4);
        Label idLbl = new Label("Email universitaire :");
        idLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #334155;");
        identifierField.getStyleClass().add("search-field");
        idBox.getChildren().addAll(idLbl, identifierField);

        VBox pwdBox = new VBox(4);
        Label pwdLbl = new Label("Mot de passe :");
        pwdLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #334155;");
        passwordField.getStyleClass().add("search-field");
        pwdBox.getChildren().addAll(pwdLbl, passwordField);

        // Options
        HBox opts = new HBox();
        opts.setAlignment(Pos.CENTER_LEFT);
        CheckBox rememberMe = new CheckBox("Rester connecté");
        rememberMe.setSelected(true);
        rememberMe.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #475569;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Hyperlink forgotLink = new Hyperlink("Mot de passe oublié ?");
        forgotLink.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #1E3A8A; -fx-font-weight: 600;");

        opts.getChildren().addAll(rememberMe, sp, forgotLink);

        // Submit Button
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 14;");
        spinner.setPrefSize(18, 18);
        spinner.setVisible(false);
        spinner.setManaged(false);

        loginBtn.setOnAction(e -> handleLogin());

        // Footer register link
        HBox footer = new HBox(6);
        footer.setAlignment(Pos.CENTER);
        Label noAcc = new Label("Pas encore de compte ?");
        noAcc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        Hyperlink regLink = new Hyperlink("Créer mon espace");
        regLink.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #1E3A8A;");
        regLink.setOnAction(e -> onGoToRegister.run());
        footer.getChildren().addAll(noAcc, regLink);

        card.getChildren().addAll(eyebrow, title, subtitle, errorLabel, idBox, pwdBox, opts, loginBtn, footer);
        rightPane.getChildren().add(card);
        getChildren().add(rightPane);
    }

    private void handleLogin() {
        String email = identifierField.getText().trim();
        String pwd = passwordField.getText();

        if (email.isEmpty() || pwd.isEmpty()) {
            showError("Veuillez renseigner vos identifiants de connexion.");
            return;
        }

        loginBtn.setDisable(true);
        loginBtn.setText("Connexion en cours...");

        new Thread(() -> {
            try {
                User user = FirebaseAuthService.login(email, pwd);
                javafx.application.Platform.runLater(() -> {
                    AppConfig.setCurrentUser(user);
                    onLoginSuccess.run();
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setText("Se connecter");
                    showError(ex.getMessage());
                });
            }
        }).start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
}
