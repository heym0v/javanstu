package poi;

import javax.swing.SwingUtilities;

/** Точка входа: отсюда запускается программа. */
public class Main {
    public static void main(String[] args) {
        // Окна Swing создаются в специальном потоке интерфейса (EDT)
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
