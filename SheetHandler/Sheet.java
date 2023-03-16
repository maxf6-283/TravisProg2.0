package SheetHandler;

import java.awt.Graphics;
import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;

import Parser.Sheet.SheetParser;

public class Sheet {
    private ArrayList<Cut> cuts;
    private Cut activeCut;
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
        SheetParser.parseCutFiles(parentFile, cuts);
    }

    public void addPart(Part part) {
        c.add(part);
        part.setParentSheet(this);
    }

    public void draw(Graphics g) {
        // TODO: add rectangle for sheet
        for (Part part : parts) {
            part.draw(g);
        }
    }

    public void saveToFile() {
        HashMap<String, String> savedInfo = new HashMap<>();
        savedInfo.put("w", ""+(width));
        savedInfo.put("h", ""+(height));
        savedInfo.put("hole_file", holeFile.getPath());
        savedInfo.put("active", activeCutFile.getPath());

        //save cuts
    }
}
