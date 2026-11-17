package cm.uniflow.desktop.ui.views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class SentinelleView extends ScrollPane {
    public SentinelleView() {
        setFitToWidth(true);
        getStyleClass().add("scroll-pane");
        VBox content = new VBox(14);
        content.setPadding(new Insets(24));
        Label title = new Label("Services Sentinelle");
        title.setStyle("-fx-font-size: 23px; -fx-font-weight: 800; -fx-text-fill: #12345B;");
        Label status = new Label("Aucun équipement, caméra ou flux IoT n’est connecté depuis ce client. Les illustrations de ressources ne représentent pas un état opérationnel du campus.");
        status.setWrapText(true);
        status.setStyle("-fx-text-fill: #64748B; -fx-padding: 16; -fx-background-color: white; -fx-background-radius: 12;");
        content.getChildren().addAll(title, status);
        setContent(content);
    }
}
