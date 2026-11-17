package cm.uniflow.desktop.ui.views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class VisioView extends ScrollPane {
    public VisioView() {
        setFitToWidth(true);
        getStyleClass().add("scroll-pane");
        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        Label title = new Label("Visioconférence");
        title.setStyle("-fx-font-size: 23px; -fx-font-weight: 800; -fx-text-fill: #12345B;");
        Label status = new Label("Aucun fournisseur de visioconférence n’est configuré. Cette application ne démarre pas de réunion, caméra ou microphone.");
        status.setWrapText(true);
        status.setStyle("-fx-text-fill: #64748B; -fx-padding: 16; -fx-background-color: white; -fx-background-radius: 12;");
        content.getChildren().addAll(title, status);
        setContent(content);
    }
}
