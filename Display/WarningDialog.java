package Display;

import java.util.logging.Level;
import javax.swing.JOptionPane;

public class WarningDialog extends JOptionPane {
    public WarningDialog(Throwable throwable) {
        Screen.logger.log(Level.WARNING, throwable.getMessage(), throwable);
        showMessageDialog(Screen.screen, throwable.getMessage(),
                "Warning: "
                        + throwable.getClass().getName().substring(throwable.getClass().getName().lastIndexOf('.') + 1),
                JOptionPane.WARNING_MESSAGE);
        repaint();
        invalidate();
        setVisible(false);
        Screen.screen.repaint();
    }

    public WarningDialog(Throwable throwable, String text) {
        Screen.logger.log(Level.WARNING, text + "\n" +throwable.getMessage(), throwable);
        showMessageDialog(Screen.screen, text,
                "Warning: "
                        + throwable.getClass().getName().substring(throwable.getClass().getName().lastIndexOf('.') + 1),
                JOptionPane.WARNING_MESSAGE);
        repaint();
        invalidate();
        setVisible(false);
        Screen.screen.repaint();
    }
}
