import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import Display.WarningDialog;

/**
 * Stores and reads the parameters of the program in a json file.
 * A new Settings object should only be instantiated to access a different file while still maintaining access to the original <code>Settings.settings</code> file.
 */
public class Settings {
    public static Settings settings;

    static {
        settings = new Settings(new File("./settings.json"));
    }

    private File settingsFile;

    public Settings(File file) {
        settingsFile = file;
        readFile();
    }

    public File outputFile;

    /**
     * Reads the contents of the settings file. This is automatically called on instantiation, but not when calling <code>setFile</code>
     */
    public void readFile() {
        try {
            FileInputStream fin = new FileInputStream(settingsFile);
            //TODO: actually read file
        } catch (FileNotFoundException e) {
            new WarningDialog(e, "No settings file found, creating one with default settings", null);
            //TODO: create file with default settings
        }
    }

    public void saveFile() {

    }

    /**
     * Changes the file being accessed by the Settings object, does not read the file.
     * @param file the file to switch to.
     */
    public void setFile(File file) {
        settingsFile = file;
    }

    /**
     * @return the file the Settings object currently is pointing to.
     */
    public File getFile() {
        return settingsFile;
    }
}
