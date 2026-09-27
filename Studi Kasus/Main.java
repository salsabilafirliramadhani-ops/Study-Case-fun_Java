import javax.swing.*;

/**
 * Entry point aplikasi "Futsal & Mini Soccer Arena Malang"
 */
public class Main {

    public static void main(String[] args) {
        // Aktifkan text antialiasing agar font terlihat tajam dan modern
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            FutsalApp app = new FutsalApp();
            app.setVisible(true);
        });
    }
}
