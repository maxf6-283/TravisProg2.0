package Display;

import java.util.logging.Level;
import javax.swing.JOptionPane;

public class WarningDialog extends JOptionPane {
    public WarningDialog(Throwable throwable, Returnable returnTo) {
        this(throwable, "", returnTo);
    }

    public WarningDialog(Throwable throwable, String text, Returnable returnTo) {
        returnTo.returnTo();
        Screen.logger.log(Level.WARNING, text + "\n" + throwable.getMessage(), throwable);
        showMessageDialog(Screen.screen, text,
                "Warning: "
                        + throwable.getClass().getName().substring(throwable.getClass().getName().lastIndexOf('.') + 1),
                JOptionPane.WARNING_MESSAGE);
        repaint();
        invalidate();
        setVisible(false);
        Screen.screen.repaint();
    }

    public WarningDialog() {
        super();
    }

    public int createYesOrNoWarningDialog(String text, String title) {
        return showConfirmDialog(Screen.screen, text, title, OK_CANCEL_OPTION, WARNING_MESSAGE);
    }
}
