package cm.uniflow.desktop.ui.views;

import cm.uniflow.desktop.services.CampusFirestoreService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class AssignmentsView extends CampusCollectionView {
    public AssignmentsView() {
        super("academic_assignments", "Devoirs & livrables", "Devoirs et échéances publiés dans Firestore. Les fichiers ne sont pas téléversés par ce client.");
        VBox form = new VBox(8); form.setPadding(new Insets(14)); form.setStyle("-fx-background-color: white; -fx-border-color: #DCE7EF; -fx-border-radius: 12; -fx-background-radius: 12;");
        Label heading = new Label("Enregistrer un dépôt"); heading.setStyle("-fx-text-fill: #12345B; -fx-font-weight: 800;");
        Label warning = new Label("Ce parcours enregistre uniquement des métadonnées de dépôt dans Firestore ; le contenu du fichier n’est pas envoyé."); warning.setWrapText(true); warning.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        TextField assignment = new TextField(); assignment.setPromptText("Identifiant du devoir (Firestore)");
        Label fileLabel = new Label("Aucun fichier sélectionné"); final File[] chosen = {null};
        Button choose = new Button("Choisir un fichier"); choose.getStyleClass().add("btn-secondary"); choose.setOnAction(e -> { FileChooser fc=new FileChooser(); fc.setTitle("Choisir le livrable (métadonnées uniquement)"); File f=fc.showOpenDialog(getScene()==null?null:getScene().getWindow()); if(f!=null){chosen[0]=f;fileLabel.setText(f.getName()+" · "+f.length()+" octets");} });
        Label status = new Label(); status.setWrapText(true);
        Button submit = new Button("Enregistrer le dépôt dans Firestore"); submit.getStyleClass().add("btn-primary"); submit.setOnAction(e -> {
            if(assignment.getText().isBlank()||chosen[0]==null){status.setText("Indiquez l’identifiant du devoir et choisissez le fichier.");return;}
            submit.setDisable(true); status.setText("Enregistrement…"); Map<String,Object> fields=new LinkedHashMap<>(); fields.put("assignmentId",assignment.getText().trim()); fields.put("fileName",chosen[0].getName()); fields.put("fileSizeBytes",chosen[0].length()); fields.put("status","submitted");
            Thread worker=new Thread(() -> {try{CampusFirestoreService.createSubmission(fields);Platform.runLater(()->{status.setText("Métadonnées enregistrées dans Firestore. Aucun octet de fichier n’a été téléversé.");submit.setDisable(false);load();});}catch(Exception ex){Platform.runLater(()->{status.setText("Échec : "+ex.getMessage());submit.setDisable(false);});}},"firestore-submission");worker.setDaemon(true);worker.start();
        });
        form.getChildren().addAll(heading,warning,assignment,choose,fileLabel,submit,status); appendSection(form);
    }
}
