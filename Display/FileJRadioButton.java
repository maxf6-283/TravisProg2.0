package Display;

import java.io.File;

import javax.swing.JRadioButton;

class FileJRadioButton extends JRadioButton {
    File file;

    public FileJRadioButton(String text) {
        super(text);
    }

    public void setFile(File newFile) {
        file = newFile;
    }

    public File getFile() {
        return file;
    }
}
