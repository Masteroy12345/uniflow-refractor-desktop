package cm.uniflow.desktop.ui.views;

import cm.uniflow.desktop.config.AppConfig;
import cm.uniflow.desktop.services.CampusFirestoreService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.Map;

public class DashboardView extends ScrollPane {
    private final VBox summaries = new VBox(10);
    public DashboardView() {
        setFitToWidth(true); getStyleClass().add("scroll-pane");
        VBox root = new VBox(18); root.setPadding(new Insets(24));
        VBox heading = new VBox(7); heading.setStyle("-fx-background-color: linear-gradient(to bottom right, #12345B, #0D9488); -fx-background-radius: 18; -fx-padding: 22 26;");
        Label title = new Label("Tableau de bord universitaire"); title.setStyle("-fx-text-fill: white; -fx-font-size: 23px; -fx-font-weight: 800;");
        String name = AppConfig.getCurrentUser() == null ? "" : AppConfig.getCurrentUser().getName();
        Label subtitle = new Label((name.isBlank() ? "" : name + " · ") + "Synthèse issue des données réellement accessibles dans Firestore.");
        subtitle.setWrapText(true); subtitle.setStyle("-fx-text-fill: #E0F2FE; -fx-font-size: 12px;"); heading.getChildren().addAll(title, subtitle);
        GridPane panels = new GridPane(); panels.setHgap(14); panels.setVgap(14);
        String[][] sources = {{"academic_courses","Cours & unités d’enseignement"},{"academic_schedules","Emploi du temps"},{"attendance_records","Présences"},{"notifications","Notifications"}};
        for (int i=0;i<sources.length;i++) {
            VBox card = new VBox(8); card.setStyle("-fx-background-color: white; -fx-border-color: #DCE7EF; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16;");
            Label h = new Label(sources[i][1]); h.setStyle("-fx-text-fill: #12345B; -fx-font-weight: 800; -fx-font-size: 14px;");
            Label status = new Label("Lecture Firestore en cours…"); status.setWrapText(true); status.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;"); card.getChildren().addAll(h,status);
            int col=i%2,row=i/2; panels.add(card,col,row); GridPane.setHgrow(card, Priority.ALWAYS); GridPane.setFillWidth(card,true);
            String collection=sources[i][0]; Thread worker=new Thread(() -> { try { List<Map<String,Object>> data=CampusFirestoreService.list(collection); Platform.runLater(() -> status.setText(data.isEmpty()?"Aucune donnée publiée.":data.size()+" enregistrement(s) chargé(s) depuis Firestore.")); } catch(Exception ex) { Platform.runLater(() -> status.setText("Indisponible : "+(ex.getMessage()==null?"erreur Firestore":ex.getMessage()))); } },"dashboard-"+collection); worker.setDaemon(true); worker.start();
        }
        Label note = new Label("Les compteurs reflètent seulement les documents retournés par les règles Firestore. Aucun chiffre de démonstration n’est utilisé."); note.setWrapText(true); note.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        summaries.getChildren().add(note); root.getChildren().addAll(heading,panels,summaries); setContent(root);
    }
}
