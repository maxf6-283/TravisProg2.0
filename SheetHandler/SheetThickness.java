package SheetHandler;

import java.io.File;

public enum SheetThickness {
    METAL_030(new File(Settings.settings.get("Holes.METAL_030")), "Metal - .030\""), // 030 metal sheet
    METAL_060(new File(Settings.settings.get("Holes.METAL_060")), "Metal - .060\""), // 060 metal sheet
    METAL_090(new File(Settings.settings.get("Holes.METAL_090")), "Metal - .090\""), // 090 metal sheet
    METAL_125(new File(Settings.settings.get("Holes.METAL_125")), "Metal - 0.125\""), // .125 metal sheet
    METAL_188(new File(Settings.settings.get("Holes.METAL_188")), "Metal - 0.1875\""), // .1875 metal sheet
    METAL_250(new File(Settings.settings.get("Holes.METAL_250")), "Metal - .0.25\""), // .25 metal sheet
    METAL_313(new File(Settings.settings.get("Holes.METAL_313")), "Metal - 0.3125\""), // .3125 metal sheet
    METAL_375(new File(Settings.settings.get("Holes.METAL_375")), "Metal - 0.375\""); // .375 metal sheet

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
