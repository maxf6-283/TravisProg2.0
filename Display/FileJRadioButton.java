package Display;

import java.io.File;

import javax.swing.JRadioButton;

import Display.Screen.SheetMenuState;

public class FileJRadioButton extends JRadioButton {
    File file;
    SheetMenuState type;

    public FileJRadioButton(String text, SheetMenuState type){
        super(text);
        setType(type);
    }

    public FileJRadioButton(String text) {
        super(text);
    }

    public SheetMenuState getType() {
        return type;
    }

    public void setType(SheetMenuState type) {
        this.type = type;
    }

    public void setFile(File newFile) {
        file = newFile;
    }

    public File getFile() {
        return file;
    }
}
