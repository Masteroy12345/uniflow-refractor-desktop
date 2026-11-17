package cm.uniflow.desktop.ui.views;

import cm.uniflow.desktop.models.User;
import cm.uniflow.desktop.services.FirebaseAuthService;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class RegisterView extends HBox {

    private final Runnable onRegisterSuccess;
    private final Runnable onGoToLogin;

    private int currentStep = 1;

    // Institutional identity: public self-sign-up is student-only and remains pending review.
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField universityField = new TextField();
    private final TextField facultyField = new TextField();
    private final TextField matriculeField = new TextField();
    private final TextField programField = new TextField();
    private final TextField levelField = new TextField();

    // Fields Step 2
    private final PasswordField pwdField = new PasswordField();
    private final PasswordField confirmPwdField = new PasswordField();
    private final CheckBox termsCheck = new CheckBox("J'accepte la charte informatique et le règlement de l'université.");

    private final VBox step1Box = new VBox(12);
    private final VBox step2Box = new VBox(12);
    private final StackPane stepContainer = new StackPane();

    private final Label stepIndicator = new Label("Étape 1 sur 2 : Identité & Espace Académique");
    private final Label errorLabel = new Label();
    private final Button actionBtn = new Button("Continuer vers la sécurisation →");

    public RegisterView(Runnable onRegisterSuccess, Runnable onGoToLogin) {
        this.onRegisterSuccess = onRegisterSuccess;
        this.onGoToLogin = onGoToLogin;

        setAlignment(Pos.CENTER);
        getStyleClass().add("app-container");

        buildShowcasePane();
        buildFormCard();
    }

    private void buildShowcasePane() {
        VBox showcase = new VBox(24);
        showcase.setAlignment(Pos.CENTER_LEFT);
        showcase.setPadding(new Insets(48));
        showcase.setPrefWidth(520);
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
        logoBox.getChildren().add(brand);

        Label headline = new Label("Un accès universitaire, conçu pour votre campus.");
        headline.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #FFFFFF; -fx-line-spacing: 4;");
        headline.setWrapText(true);

        Label sub = new Label("Soumettez vos informations institutionnelles. L'accès est activé après validation par votre université.");
        sub.setStyle("-fx-font-size: 14px; -fx-text-fill: #E0F2FE; -fx-line-spacing: 3;");
        sub.setWrapText(true);

        Region sp = new Region();
        VBox.setVgrow(sp, Priority.ALWAYS);

        Label copy = new Label("KERNEL FORGE · Système d'Excellence Académique");
        copy.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255, 255, 255, 0.65);");

        showcase.getChildren().addAll(logoBox, headline, sub);
        showcase.getChildren().addAll(sp, copy);

        getChildren().add(showcase);
    }

    private void buildFormCard() {
        VBox rightPane = new VBox();
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setPadding(new Insets(40));
        rightPane.setPrefWidth(560);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        VBox card = new VBox(16);
        card.setMaxWidth(480);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-radius: 24; -fx-background-radius: 24; -fx-padding: 32; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 20, 0, 0, 6);");

        stepIndicator.setStyle("-fx-font-size: 11.5px; -fx-font-weight: 800; -fx-text-fill: #2563EB;");

        Label title = new Label("Créer votre compte UniFlow");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #0F172A;");

        // Error Label
        errorLabel.setStyle("-fx-background-color: #FEF2F2; -fx-border-color: #FECACA; -fx-text-fill: #B91C1C; -fx-font-weight: 700; -fx-font-size: 11px; -fx-padding: 8 12; -fx-background-radius: 10;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        buildStep1();
        buildStep2();

        step2Box.setVisible(false);
        step2Box.setManaged(false);

        stepContainer.getChildren().addAll(step1Box, step2Box);

        // Buttons
        HBox btnRow = new HBox(12);
        Button backBtn = new Button("← Précédent");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setVisible(false);
        backBtn.setOnAction(e -> goToStep(1));

        actionBtn.getStyleClass().add("btn-primary");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(actionBtn, Priority.ALWAYS);
        actionBtn.setStyle("-fx-font-size: 13.5px; -fx-padding: 11; -fx-background-radius: 14;");

        actionBtn.setOnAction(e -> {
            if (currentStep == 1) {
                if (validateStep1()) {
                    backBtn.setVisible(true);
                    goToStep(2);
                }
            } else {
                handleRegister();
            }
        });

        btnRow.getChildren().addAll(backBtn, actionBtn);

        // Footer
        HBox footer = new HBox(6);
        footer.setAlignment(Pos.CENTER);
        Label already = new Label("Vous avez déjà un compte ?");
        already.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        Hyperlink loginLink = new Hyperlink("Se connecter");
        loginLink.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #1E3A8A;");
        loginLink.setOnAction(e -> onGoToLogin.run());
        footer.getChildren().addAll(already, loginLink);

        card.getChildren().addAll(stepIndicator, title, errorLabel, stepContainer, btnRow, footer);
        rightPane.getChildren().add(card);
        getChildren().add(rightPane);
    }

    private void buildStep1() {
        step1Box.getChildren().clear();

        // Name fields row
        HBox namesRow = new HBox(10);
        firstNameField.setPromptText("Prénom");
        firstNameField.getStyleClass().add("search-field");
        lastNameField.setPromptText("Nom");
        lastNameField.getStyleClass().add("search-field");
        HBox.setHgrow(firstNameField, Priority.ALWAYS);
        HBox.setHgrow(lastNameField, Priority.ALWAYS);
        namesRow.getChildren().addAll(firstNameField, lastNameField);

        emailField.setPromptText("Adresse email institutionnelle");
        emailField.getStyleClass().add("search-field");

        universityField.setPromptText("Identifiant officiel de l’université");
        universityField.getStyleClass().add("search-field");
        facultyField.setPromptText("Faculté");
        facultyField.getStyleClass().add("search-field");
        matriculeField.setPromptText("Matricule étudiant officiel");
        matriculeField.getStyleClass().add("search-field");

        programField.setPromptText("Identifiant officiel du programme");
        programField.getStyleClass().add("search-field");
        levelField.setPromptText("Niveau institutionnel (ex. L1, M1)");
        levelField.getStyleClass().add("search-field");

        step1Box.getChildren().addAll(
                new Label("Nom & Prénom :"), namesRow,
                new Label("Email universitaire :"), emailField,
                new Label("Université :"), universityField,
                new Label("Faculté (identifiant officiel) :"), facultyField,
                new Label("Matricule Étudiant :"), matriculeField,
                new Label("Filière & Parcours (identifiant) :"), programField,
                new Label("Niveau Universitaire :"), levelField
        );
    }

    private void buildStep2() {
        step2Box.getChildren().clear();

        pwdField.setPromptText("Mot de passe (8 caractères minimum)");
        pwdField.getStyleClass().add("search-field");

        confirmPwdField.setPromptText("Confirmez votre mot de passe");
        confirmPwdField.getStyleClass().add("search-field");

        termsCheck.setSelected(true);
        termsCheck.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #475569;");
        termsCheck.setWrapText(true);

        Label secHint = new Label("💡 Votre mot de passe protège votre clé de présence et vos attestations officielles.");
        secHint.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 11px; -fx-text-fill: #64748B;");
        secHint.setWrapText(true);

        step2Box.getChildren().addAll(
                new Label("Mot de passe :"), pwdField,
                new Label("Confirmer le mot de passe :"), confirmPwdField,
                secHint,
                termsCheck
        );
    }

    private boolean validateStep1() {
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            showError("Veuillez renseigner votre nom et prénom.");
            return false;
        }
        if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            showError("Veuillez saisir une adresse email valide.");
            return false;
        }
        if (universityField.getText().trim().isEmpty() || facultyField.getText().trim().isEmpty()
                || matriculeField.getText().trim().isEmpty() || programField.getText().trim().isEmpty() || levelField.getText().trim().isEmpty()) {
            showError("Université, faculté, filière, niveau et matricule sont obligatoires.");
            return false;
        }
        hideError();
        return true;
    }

    private void goToStep(int step) {
        this.currentStep = step;
        if (step == 1) {
            stepIndicator.setText("Étape 1 sur 2 : Identité & Espace Académique");
            actionBtn.setText("Continuer vers la sécurisation →");
            step2Box.setVisible(false);
            step2Box.setManaged(false);
            step1Box.setVisible(true);
            step1Box.setManaged(true);
        } else {
            stepIndicator.setText("Étape 2 sur 2 : Mot de passe & Validation");
            actionBtn.setText("Envoyer ma demande d'inscription");
            step1Box.setVisible(false);
            step1Box.setManaged(false);
            step2Box.setVisible(true);
            step2Box.setManaged(true);
        }
        FadeTransition ft = new FadeTransition(Duration.millis(250), stepContainer);
        ft.setFromValue(0.2);
        ft.setToValue(1.0);
        ft.play();
    }

    private void handleRegister() {
        String p1 = pwdField.getText();
        String p2 = confirmPwdField.getText();

        if (p1.length() < 8) {
            showError("Le mot de passe doit comporter au moins 8 caractères.");
            return;
        }
        if (!p1.equals(p2)) {
            showError("Les deux mots de passe ne correspondent pas.");
            return;
        }
        if (!termsCheck.isSelected()) {
            showError("Veuillez accepter le règlement pour continuer.");
            return;
        }

        actionBtn.setDisable(true);
        actionBtn.setText("Création du compte en cours...");

        new Thread(() -> {
            String fullName = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String matricule = matriculeField.getText().trim();

            try {
                User user = FirebaseAuthService.registerStudent(fullName, email, p1,
                        universityField.getText().trim(), facultyField.getText().trim(),
                        programField.getText().trim(), levelField.getText().trim(), matricule);
                javafx.application.Platform.runLater(() -> {
                    Alert submitted = new Alert(Alert.AlertType.INFORMATION);
                    submitted.setTitle("Demande envoyée");
                    submitted.setHeaderText("Inscription universitaire en attente");
                    submitted.setContentText("Votre demande a été transmise. Votre compte étudiant restera en attente jusqu'à sa validation par l'administration.");
                    submitted.showAndWait();
                    onRegisterSuccess.run();
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    actionBtn.setDisable(false);
                    actionBtn.setText("Envoyer ma demande d'inscription");
                    showError(ex.getMessage() == null ? "Échec de l'inscription Firebase." : ex.getMessage());
                });
            }
        }).start();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
