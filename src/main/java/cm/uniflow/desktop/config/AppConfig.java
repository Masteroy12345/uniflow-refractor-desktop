package cm.uniflow.desktop.config;

import cm.uniflow.desktop.models.User;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class AppConfig {
    public static final String APP_NAME = "UniFlow Desktop";
    public static final String APP_VERSION = "2.5.0";
    public static final String ORG_NAME = "KERNEL FORGE";
    // Firebase client config only: no service-account credentials or private keys.
    public static final String FIREBASE_PROJECT_ID = "uniflow-d79e0";
    public static final String FIREBASE_API_KEY_ENV = "UNIFLOW_FIREBASE_API_KEY";
    public static final String FIREBASE_API_KEY_PROPERTY = "uniflow.firebase.apiKey";

    // Registered Microsoft Store identity — do not rename/re-identify this package.
    public static final String STORE_PACKAGE_NAME = "KERNELFORGE.Uniflowwork";
    public static final String STORE_PUBLISHER = "CN=200A91F7-4B15-441F-9332-83F216885B09";
    public static final String STORE_PUBLISHER_DISPLAY_NAME = "KERNEL FORGE";
    public static final String STORE_PACKAGE_FAMILY_NAME = "KERNELFORGE.Uniflowwork_3wyzhk1x8xr0a";
    public static final String STORE_ID = "9NGZKFGK98K6";

    private static final ObjectProperty<User> currentUser = new SimpleObjectProperty<>(null);

    private AppConfig() {}

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static ObjectProperty<User> currentUserProperty() {
        return currentUser;
    }
}
