package SheetHandler;

import java.awt.Graphics;
import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;

import Parser.Sheet.SheetParser;

public class Sheet {
    private ArrayList<Part> parts;
    private ArrayList<Hole> holes;
    private double width, height; // in inches
    private File sheetFile, holeFile, activeCutFile;

    public Sheet(File sheetFile) {
        this.sheetFile = sheetFile;
        
        HashMap<String, String> decodedFile = SheetParser.parseSheetFile(sheetFile);

        width = Double.parseDouble(decodedFile.get("w"));
        height = Double.parseDouble(decodedFile.get("h"));
        holeFile = new File(decodedFile.get("hole_file"));
        activeCutFile = new File(decodedFile.get("active"));
        

        // time to get the parts
        File parentFile = sheetFile.getParentFile();
        SheetParser.parseCutFiles(parentFile, parts, holes);
    }

    public void addPart(Part part) {
        parts.add(part);
        part.setParentSheet(this);
    }

    public void draw(Graphics g) {
        // TODO: add rectangle for sheet
        for (Part part : parts) {
            part.draw(g);
        }
    }

    public void saveToFile() {
        // TODO: add sheet saving to file
    }
}
