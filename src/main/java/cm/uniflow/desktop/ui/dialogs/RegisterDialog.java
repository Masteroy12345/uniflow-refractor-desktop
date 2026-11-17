package cm.uniflow.desktop.ui.dialogs;

import cm.uniflow.desktop.models.User;
import cm.uniflow.desktop.services.FirebaseAuthService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class RegisterDialog extends Dialog<User> {

    public RegisterDialog() {
        setTitle("Inscription universitaire — UniFlow");
        setHeaderText("Demander un compte étudiant — validation requise par l'université");

        VBox form = new VBox(9);
        form.setPadding(new Insets(20));
        form.setPrefWidth(430);

        TextField nameField = new TextField();
        nameField.setPromptText("Nom complet");
        TextField emailField = new TextField();
        emailField.setPromptText("Email universitaire");
        TextField universityField = new TextField();
        universityField.setPromptText("Identifiant officiel de l’université");
        TextField facultyField = new TextField();
        facultyField.setPromptText("Faculté");
        TextField studentNumberField = new TextField();
        studentNumberField.setPromptText("Matricule étudiant");
        TextField programField = new TextField();
        programField.setPromptText("Filière / programme");
        TextField levelField = new TextField();
        levelField.setPromptText("Niveau universitaire");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe (8 caractères minimum)");
        Label error = new Label();
        error.setStyle("-fx-text-fill: #B91C1C; -fx-wrap-text: true;");
        error.setVisible(false);
        error.setManaged(false);

        form.getChildren().addAll(
                new Label("Nom complet :"), nameField,
                new Label("Email institutionnel :"), emailField,
                new Label("Université :"), universityField,
                new Label("Faculté (identifiant officiel) :"), facultyField,
                new Label("Matricule :"), studentNumberField,
                new Label("Filière / programme :"), programField,
                new Label("Niveau :"), levelField,
                new Label("Mot de passe :"), passwordField,
                error
        );
        getDialogPane().setContent(form);
        ButtonType registerBtnType = new ButtonType("Envoyer la demande", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(registerBtnType, ButtonType.CANCEL);
        Button regBtn = (Button) getDialogPane().lookupButton(registerBtnType);
        regBtn.getStyleClass().add("btn-primary");

        setResultConverter(btn -> {
            if (btn != registerBtnType) return null;
            if (passwordField.getText().length() < 8) {
                error.setText("Le mot de passe doit contenir au moins 8 caractères.");
                error.setVisible(true); error.setManaged(true); return null;
            }
            try {
                return FirebaseAuthService.registerStudent(nameField.getText().trim(), emailField.getText().trim(),
                        passwordField.getText(), universityField.getText().trim(), facultyField.getText().trim(),
                        programField.getText().trim(), levelField.getText().trim(), studentNumberField.getText().trim());
            } catch (Exception ex) {
                error.setText(ex.getMessage() == null ? "Échec de la demande d'inscription." : ex.getMessage());
                error.setVisible(true); error.setManaged(true); return null;
            }
        });
    }
}
