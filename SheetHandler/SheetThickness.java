package SheetHandler;

import java.io.File;

public enum SheetThickness {
    _030 (new File(""), ".030\""),
    _060 (new File(""), ".060\""),
    _090 (new File(""), ".090\""),
    _125 (new File(""), "0.125\""),
    _188 (new File(""), "0.188\""),
    _250 (new File(""), ".0.25\"");

    public final File holesFile;
    public final String name;

    SheetThickness(File holesFile, String name) {
        this.holesFile = holesFile;
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
