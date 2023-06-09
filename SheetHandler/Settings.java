package SheetHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import Display.Screen;

/**
 * Stores and reads the parameters of the program in a json file.
 * A new Settings object should only be instantiated to access a different file
 * while still maintaining access to the original <code>Settings.settings</code>
 * file.
 */
public class Settings {
    public static Settings settings;
    // stores current settings
    private HashMap<String, String> settingsMap = new HashMap<>();
    // stores default settings
    private static final HashMap<String, String> defaultSettingsMap = new HashMap<>();

    static {
        // put all default setting into hashmap
        defaultSettingsMap.put("Holes.METAL_030", "assets/holes/holes_030.ngc");
        defaultSettingsMap.put("Holes.METAL_060", "assets/holes/holes_060.ngc");
        defaultSettingsMap.put("Holes.METAL_090", "assets/holes/holes_090.ngc");
        defaultSettingsMap.put("Holes.METAL_125", "assets/holes/holes_125.ngc");
        defaultSettingsMap.put("Holes.METAL_188", "assets/holes/holes_1875.ngc");
        defaultSettingsMap.put("Holes.METAL_250", "assets/holes/holes_250.ngc");
        defaultSettingsMap.put("Holes.METAL_313", "assets/holes/holes_3125.ngc");
        defaultSettingsMap.put("Holes.METAL_375", "assets/holes/holes_375.ngc");

        defaultSettingsMap.put("DebugMode", "false");
        defaultSettingsMap.put("LoggerFile", "logger.log");
        defaultSettingsMap.put("SheetParentFolder", "./sheets");
        defaultSettingsMap.put("IconImageFile", "Display/971 Icon.png");
        defaultSettingsMap.put("SettingsIconImage", "Display/gear.png");
        defaultSettingsMap.put("SettingsIconImage2", "Display/gear2.png");
        defaultSettingsMap.put("ScrewHeadSize", "0.4");

        settings = new Settings(new File("./settings.json"));
    }

    private File settingsFile;

    public Settings(File file) {
        settingsFile = file;
        readFile();
    }

    /**
     * @param key settings key
     * @return returns value in the active settings.json or the default value
     * 
     * @see HashMap#get(Object)
     */
    public String get(String key) {
        String output = settingsMap.get(key);
        if (output == null) {
            output = defaultSettingsMap.get(key);
            if (output != null) {
                settingsMap.put(key, output);
            }
        }
        return output;
    }

    /**
     * @return a set of all the key Strings in the current settings
     * 
     * @see HashMap#keySet()
     */
    public Set<String> keySet() {
        return settingsMap.keySet();
    }

    /**
     * @param key
     * @param value
     * 
     * @see HashMap#put(Object, Object)
     */
    public void put(String key, String value) {
        settingsMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
    /**
     * sets current settings HashMap to a clone of the static, final default one
     */
    public void resetToDefault() {
        settingsMap = (HashMap<String, String>) defaultSettingsMap.clone();
    }

    /**
     * Reads the contents of the settings file. This is automatically called on
     * instantiation
     */
    @SuppressWarnings("unchecked")
    public void readFile() {
        try (FileInputStream fin = new FileInputStream(settingsFile)) {
            boolean hasStartBracket = false;
            while (fin.available() > 0) {
                // start parsing only after first open bracket
                if (!hasStartBracket) {
                    hasStartBracket = (char) fin.read() == '{';
                } else {
                    // start of file
                    int read = fin.read();

                    if ((char) read == '}') {// end of file
                        break;
                    }

                    String key = "";
                    String value = "";

                    // if a " is read, add key for the the chars until the next "
                    // then skips chars until the next " which is read until the next " for the
                    // value
                    // then puts the key and value interpreted into the Hashmap
                    if ((char) read == '\"') {
                        while ((char) (read = fin.read()) != '\"') {
                            key += (char) read;
                        }

                        while ((char) (read = fin.read()) != '\"') {

                        }

                        while ((char) (read = fin.read()) != '\"') {
                            value += (char) read;
                        }

                        settingsMap.put(key, value);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // no settings.json, create a new one
            settingsMap = (HashMap<String, String>) defaultSettingsMap.clone();
            saveFile();
        } catch (IOException e) {
            Screen.logger.severe(e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
        if (Boolean.parseBoolean(settingsMap.get("DebugMode")))
            settingsMap.forEach((key, value) -> System.out.println(key + " " + value));
    }

    /**
     * saves the current settings in the HashMap to the settings.json file(overwrites)
     */
    public void saveFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFile))) {
            //writes open bracket
            writer.write("{\n");
            ArrayList<String> iteratorList = new ArrayList<>(Settings.settings.keySet());
            Collections.sort(iteratorList);
            //writes key and values in json format in a sorted order(does create trailing comma but doesn't affect anything)
            for (String key : iteratorList) {
                writer.write("\t\"" + key + "\": \"" + settingsMap.get(key) + "\",\n");
            }
            //closes json file
            writer.write("}");
        } catch (IOException e) {
            e.printStackTrace();
            Screen.logger.severe(e.getMessage());
            System.exit(-1);
        }
    }
}
