package cm.uniflow.desktop;

import cm.uniflow.desktop.config.AppConfig;
import cm.uniflow.desktop.config.ThemeManager;
import cm.uniflow.desktop.services.FirebaseAuthService;
import cm.uniflow.desktop.ui.components.Sidebar;
import cm.uniflow.desktop.ui.components.TopBar;
import cm.uniflow.desktop.ui.views.*;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class UniFlowApp extends Application {

    private Stage primaryStage;
    private StackPane rootContainer;
    private BorderPane mainLayout;
    private StackPane contentArea;
    private Sidebar sidebar;
    private TopBar topBar;

    private final Map<Sidebar.NavSection, Node> viewCache = new HashMap<>();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("UniFlow Desktop — Portail Académique & Campus Numérique · KERNEL FORGE");

        // Application Icon
        try {
            var iconUrl = getClass().getResource("/brand/uniflow-campus-mark.png");
            if (iconUrl != null) {
                stage.getIcons().add(new Image(iconUrl.toExternalForm()));
            }
        } catch (Exception ignored) {}

        rootContainer = new StackPane();
        rootContainer.getStyleClass().add("app-container");

        initMainLayout();

        Scene scene = new Scene(rootContainer, 1340, 860);
        ThemeManager.applyTheme(ThemeManager.Theme.PRIMER_LIGHT, scene);

        stage.setScene(scene);
        stage.setMinWidth(1060);
        stage.setMinHeight(720);

        // Check if user is logged in
        if (AppConfig.getCurrentUser() != null) {
            showAppShell();
        } else {
            showOnboarding();
        }

        stage.show();
    }

    private void initMainLayout() {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("app-container");

        contentArea = new StackPane();

        sidebar = new Sidebar(this::navigateTo, this::logout);
        mainLayout.setLeft(sidebar);

        topBar = new TopBar(this::toggleTheme, this::showLogin);
        mainLayout.setTop(topBar);

        mainLayout.setCenter(contentArea);
    }

    public void showOnboarding() {
        OnboardingView onboarding = new OnboardingView(this::showLogin);
        setRootAnimated(onboarding);
    }

    public void showLogin() {
        LoginView loginView = new LoginView(this::showAppShell, this::showRegister);
        setRootAnimated(loginView);
    }

    public void showRegister() {
        RegisterView registerView = new RegisterView(this::showLogin, this::showLogin);
        setRootAnimated(registerView);
    }

    public void showAppShell() {
        if (AppConfig.getCurrentUser() == null
                || FirebaseAuthService.getCurrentIdToken() == null
                || FirebaseAuthService.getCurrentIdToken().isBlank()) {
            showLogin();
            return;
        }
        viewCache.clear();
        setRootAnimated(mainLayout);
        navigateTo(Sidebar.NavSection.DASHBOARD);
    }

    public void logout() {
        FirebaseAuthService.logout();
        viewCache.clear();
        showLogin();
    }

    private void setRootAnimated(Node newRoot) {
        newRoot.setOpacity(0);
        rootContainer.getChildren().setAll(newRoot);
        FadeTransition ft = new FadeTransition(Duration.millis(260), newRoot);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    public void navigateTo(Sidebar.NavSection section) {
        Node view = viewCache.computeIfAbsent(section, this::createViewForSection);

        view.setOpacity(0);
        contentArea.getChildren().setAll(view);
        FadeTransition ft = new FadeTransition(Duration.millis(180), view);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        String subtitle = switch (section) {
            case DASHBOARD -> "Portail universitaire · données de votre campus";
            case SCHEDULE -> "Séances publiées par votre établissement";
            case COURSES -> "Catalogue académique publié par votre établissement";
            case ASSIGNMENTS -> "Devoirs et dépôts académiques";
            case GRADES -> "Résultats autorisés par votre établissement";
            case ATTENDANCE -> "Historique des présences autorisées";
            case ATTENDANCE_MANAGE -> "Séances de présence du campus";
            case ZLIBRARY -> "Ressources académiques publiées";
            case ACTUALITES -> "Annonces & Événements Officiels du Campus";
            case VISIO -> "État du service visioconférence";
            case SENTINELLE -> "État de connexion des services Sentinelle";
            case CLASSROOMS -> "Informations des salles du campus";
            case DIRECTORY -> "Annuaire des Enseignants, Délégués & Étudiants";
            case PAYMENTS -> "Consultation des frais et bourses";
            case SETTINGS -> "Paramètres & Configuration Système";
        };

        topBar.updateTitle(section.getTitle(), subtitle);
    }

    private Node createViewForSection(Sidebar.NavSection section) {
        return switch (section) {
            case DASHBOARD -> new DashboardView();
            case SCHEDULE -> new ScheduleView();
            case COURSES -> new CoursesView();
            case ASSIGNMENTS -> new AssignmentsView();
            case GRADES -> new GradesView();
            case ATTENDANCE -> new AttendanceView();
            case ATTENDANCE_MANAGE -> new AttendanceManageView();
            case ZLIBRARY -> new ZLibraryView();
            case ACTUALITES -> new ActualitesView();
            case VISIO -> new VisioView();
            case SENTINELLE -> new SentinelleView();
            case CLASSROOMS -> new ClassroomsView();
            case DIRECTORY -> new DirectoryView();
            case PAYMENTS -> new PaymentsView();
            case SETTINGS -> new SettingsView(this::showLogin, this::showOnboarding, this::logout);
        };
    }

    private void toggleTheme() {
        if (primaryStage.getScene() == null) return;
        ThemeManager.Theme next = ThemeManager.isDark() ? ThemeManager.Theme.PRIMER_LIGHT : ThemeManager.Theme.PRIMER_DARK;
        ThemeManager.applyTheme(next, primaryStage.getScene());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
