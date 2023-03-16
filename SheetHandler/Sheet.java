package SheetHandler;

import java.awt.Graphics;
import java.util.ArrayList;
import java.io.File;
import java.util.HashMap;
import java.awt.Color;

import Parser.Sheet.SheetParser;

public class Sheet {
    private ArrayList<Cut> cuts;
    private Cut activeCut;
    private double width, height; // in inches
    private File sheetFile, holeFile, activeCutFile;

    /**
     * Declares a new sheet from a file
     * @param sheetFile - the .sheet file to get the information from
     */
    public Sheet(File sheetFile) {
        this.sheetFile = sheetFile;
        
        HashMap<String, String> decodedFile = SheetParser.parseSheetFile(sheetFile);

        width = Double.parseDouble(decodedFile.get("w"));
        height = Double.parseDouble(decodedFile.get("h"));
        holeFile = new File(decodedFile.get("hole_file"));
        activeCutFile = new File(decodedFile.get("active"));
        

        // time to get the parts
        File parentFile = sheetFile.getParentFile();
        for(File cutFile : parentFile.listFiles()) {
            if(cutFile.getName().endsWith(".cut")) {
                continue;
            }
            Cut newCut = new Cut(cutFile);
            cuts.add(newCut);
            if(cutFile.equals(activeCutFile)) {
                activeCut = newCut;
            }
        }
    }

    /**
     * Adds a part to the active cut;
     */
    public void addPart(Part part) {
        activeCut.parts.add(part);
    }

    /**
     * Adds a hole to the active cut
     */
    public void addHole(Hole hole) {
        activeCut.holes.add(hole);
    }

    /**
     * Draw the sheet to the screen
     */
    public void draw(Graphics g) {
        // TODO: add rectangle for sheet
        for(Cut cut : cuts) {
            if(cut == activeCut) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.BLUE);
            }
            cut.draw(g);
        }
    }

    /**
     * save the sheet and its cuts
     */
    public void saveToFile() {
        HashMap<String, String> sheetInfo = new HashMap<>();
        sheetInfo.put("w", ""+(width));
        sheetInfo.put("h", ""+(height));
        sheetInfo.put("hole_file", holeFile.getPath());
        sheetInfo.put("active", activeCutFile.getPath());

        //save sheet info
        SheetParser.saveSheetInfo(sheetFile, sheetInfo);

        //save cuts
        for(Cut cut : cuts) {
            SheetParser.saveCutInfo(cut);
        }
    }
}
