package Display;

import javax.swing.JOptionPane;

public class ErrorDialog extends JOptionPane {
    public ErrorDialog(Throwable throwable) {
        showMessageDialog(Screen.screen, throwable.getMessage(), "Fatal Error: " +throwable.getClass().getName().substring(throwable.getClass().getName().lastIndexOf('.')+1), JOptionPane.ERROR_MESSAGE);
    }
}
