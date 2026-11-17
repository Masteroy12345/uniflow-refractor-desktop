package cm.uniflow.desktop;

import cm.uniflow.desktop.config.AppConfig;
import cm.uniflow.desktop.models.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {
    @Test
    void profileKeepsCampusRoleAndAdminSemanticsLowercase() {
        User student = new User("uid-1", "Awa Etudiant", "awa@example.edu", "student", "S001", "prog-1", "L1");
        assertEquals("student", student.getRole());
        assertFalse(student.isAdmin());
        User admin = new User("uid-2", "Admin Campus", "admin@example.edu", "admin", "", "", "");
        assertTrue(admin.isAdmin());
        assertFalse(new User("uid-3", "Delegate", "d@example.edu", "delegate", "", "", "").isAdmin());
    }

    @Test
    void applicationAndStoreIdentityArePreservedExactly() {
        assertEquals("UniFlow Desktop", AppConfig.APP_NAME);
        assertEquals("KERNEL FORGE", AppConfig.ORG_NAME);
        assertEquals("KERNELFORGE.Uniflowwork", AppConfig.STORE_PACKAGE_NAME);
        assertEquals("CN=200A91F7-4B15-441F-9332-83F216885B09", AppConfig.STORE_PUBLISHER);
        assertEquals("KERNEL FORGE", AppConfig.STORE_PUBLISHER_DISPLAY_NAME);
        assertEquals("KERNELFORGE.Uniflowwork_3wyzhk1x8xr0a", AppConfig.STORE_PACKAGE_FAMILY_NAME);
        assertEquals("9NGZKFGK98K6", AppConfig.STORE_ID);
    }
}
