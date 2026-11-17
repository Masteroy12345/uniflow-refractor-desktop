package cm.uniflow.desktop.ui.views;

import cm.uniflow.desktop.config.AppConfig;
import cm.uniflow.desktop.config.ThemeManager;
import cm.uniflow.desktop.models.User;
import cm.uniflow.desktop.services.FirebaseAuthService;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class SettingsView extends ScrollPane {
    public SettingsView(Runnable onLoginClick, Runnable onReplayOnboarding, Runnable onLogout) {
        setFitToWidth(true); getStyleClass().add("scroll-pane");
        VBox root=new VBox(16); root.setPadding(new Insets(24));
        Label title=new Label("Paramètres de l’application"); title.setStyle("-fx-font-size: 23px; -fx-font-weight: 800; -fx-text-fill: #12345B;");
        VBox themeCard=new VBox(10); themeCard.setStyle("-fx-background-color: white; -fx-border-color: #DCE7EF; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;");
        Label themeLabel=new Label("Apparence"); themeLabel.setStyle("-fx-font-weight: 800; -fx-text-fill: #12345B;");
        ComboBox<ThemeManager.Theme> themes=new ComboBox<>(); themes.getItems().setAll(ThemeManager.Theme.values()); themes.setValue(ThemeManager.getCurrentTheme()); themes.setOnAction(e->{if(getScene()!=null)ThemeManager.applyTheme(themes.getValue(),getScene());});
        themeCard.getChildren().addAll(themeLabel,themes);
        User user=AppConfig.getCurrentUser();
        VBox profile=new VBox(6); profile.setStyle("-fx-background-color: white; -fx-border-color: #DCE7EF; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;");
        Label pTitle=new Label("Profil universitaire"); pTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #12345B;");
        profile.getChildren().addAll(pTitle,new Label("Nom : "+(user==null?"—":user.getName())),new Label("Email : "+(user==null?"—":user.getEmail())),new Label("Rôle campus : "+(user==null?"—":user.getRole())),new Label("Statut : "+(user==null?"—":user.getStatus())));
        VBox firebase=new VBox(6); firebase.setStyle("-fx-background-color: white; -fx-border-color: #DCE7EF; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;");
        Label fTitle=new Label("Connexion Firebase"); fTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #12345B;");
        Label project=new Label("Projet : "+FirebaseAuthService.projectId());
        Label api=new Label(FirebaseAuthService.clientApiKey()==null?FirebaseAuthService.missingConfigurationMessage():"Clé API client chargée depuis la configuration d’exécution."); api.setWrapText(true);
        Label token=new Label(FirebaseAuthService.getCurrentIdToken()==null?"Session Firebase : inactive":"Session Firebase : ID token utilisateur actif");
        firebase.getChildren().addAll(fTitle,project,api,token);
        Label note=new Label("Aucun secret Admin SDK, compte de service ou fichier .env n’est inclus dans le dépôt."); note.setWrapText(true); note.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        root.getChildren().addAll(title,themeCard,profile,firebase,note); setContent(root);
    }
}
