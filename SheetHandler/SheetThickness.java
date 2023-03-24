package SheetHandler;

import java.io.File;

public enum SheetThickness {
    METAL_030 (new File("assets/holes/holes_030.ngc"), "Metal - .030\""), //030 metal sheet
    METAL_060 (new File("assets/holes/holes_060.ngc"), "Metal - .060\""), //060 metal sheet
    METAL_090 (new File("assets/holes/holes_090.ngc"), "Metal - .090\""), //090 metal sheet
    METAL_125 (new File("assets/holes/holes_125.ngc"), "Metal - 0.125\""), //.125 metal sheet
    METAL_188 (new File("assets/holes/holes_1875.ngc"), "Metal - 0.1875\""), //.1875 metal sheet
    METAL_250 (new File("assets/holes/holes_250.ngc"), "Metal - .0.25\""), //.25 metal sheet
    METAL_313 (new File("assets/holes/holes_3125.ngc"), "Metal - 0.3125\""), //.3125 metal sheet
    METAL_375 (new File("assets/holes/holes_375.ngc"), "Metal - 0.375\""); //.375 metal sheet

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
