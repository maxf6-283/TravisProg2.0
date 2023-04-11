package Display;

import java.util.logging.Level;
import javax.swing.JOptionPane;

public class ErrorDialog extends JOptionPane {
    public ErrorDialog(Throwable throwable) {
        showMessageDialog(Screen.screen, throwable.getMessage(), "Fatal Error: " +throwable.getClass().getName().substring(throwable.getClass().getName().lastIndexOf('.')+1), JOptionPane.ERROR_MESSAGE);
        Screen.logger.log(Level.SEVERE, throwable.getMessage(), throwable);
    }
}
