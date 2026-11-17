package cm.uniflow.desktop.ui.views;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class PaymentsView extends ScrollPane {
    public PaymentsView() {
        setFitToWidth(true); getStyleClass().add("scroll-pane");
        VBox root=new VBox(12); root.setPadding(new Insets(24));
        VBox intro=new VBox(7); intro.setStyle("-fx-background-color: linear-gradient(to bottom right, #12345B, #0D9488); -fx-background-radius: 18; -fx-padding: 22 26;");
        Label title=new Label("Frais de scolarité & bourses"); title.setStyle("-fx-text-fill: white; -fx-font-size: 23px; -fx-font-weight: 800;");
        Label note=new Label("Consultation des dossiers Firestore. Le paiement Mobile Money et l’attribution d’une bourse ne sont pas exécutés par cette application."); note.setWrapText(true); note.setStyle("-fx-text-fill: #E0F2FE; -fx-font-size: 12px;"); intro.getChildren().addAll(title,note);
        CampusCollectionView fees=new CampusCollectionView("tuition_payments", "Frais & échéances", "Montants et échéances réellement enregistrés dans Firestore."); fees.setPrefHeight(360);
        CampusCollectionView scholarships=new CampusCollectionView("scholarships", "Bourses", "Demandes et décisions de bourse publiées dans Firestore."); scholarships.setPrefHeight(360);
        root.getChildren().addAll(intro,fees,scholarships); setContent(root);
    }
}
