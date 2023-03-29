package SheetHandler;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
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
     * 
     * @param sheetFile - the .sheet file to get the information from
     */
    public Sheet(File sheetFile) {
        this.sheetFile = sheetFile;
        cuts = new ArrayList<>();

        HashMap<String, String> decodedFile = SheetParser.parseSheetFile(sheetFile);

        width = Double.parseDouble(decodedFile.get("w"));
        height = Double.parseDouble(decodedFile.get("h"));
        holeFile = new File(decodedFile.get("hole_file"));
        activeCutFile = new File(decodedFile.get("active"));

        // time to get the parts
        File parentFile = sheetFile.getParentFile();
        for (File cutFile : parentFile.listFiles()) {
            if (!cutFile.getName().endsWith(".cut")) {
                continue;
            }
            Cut newCut = new Cut(cutFile, holeFile);
            cuts.add(newCut);
            if (cutFile.getName().equals(activeCutFile.getName())) {
                activeCut = newCut;
            }
        }
    }

    /**
     * Declare a new sheet from a path to the list of sheets and the name of the
     * sheet
     */
    public Sheet(File sheetFolder, String sheetName, double width, double height, SheetThickness thickness) {
        try {
            File parentFile = new File(sheetFolder, sheetName);
            parentFile.mkdir();
            sheetFile = new File(parentFile, sheetName + ".sheet");
            HashMap<String, String> sheetInfo = new HashMap<>();
            sheetInfo.put("w", "" + width);
            sheetInfo.put("h", "" + height);
            sheetInfo.put("hole_file", thickness.holesFile.getPath());
            SheetParser.saveSheetInfo(sheetFile, sheetInfo);
        } catch (Exception e) {
            System.err.println("Could not create sheet file\n\n");
        }

        this.width = width;
        this.height = height;
    }

    /**
     * returns the width of the sheet
     * 
     * @return the width of the sheet
     */
    public double getWidth() {
        return width;
    }

    /**
     * returns the height of the sheet
     * 
     * @return the height of the sheet
     */
    public double getHeight() {
        return height;
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
        activeCut.parts.add(hole);
    }

    /**
     * Adds a cut to the list and sets it as active
     */
    public void addCut(Cut cut) {
        activeCut = cut;
        cuts.add(cut);
    }

    /**
     * Draw the sheet to the screen
     */
    public void draw(Graphics g) {
        g.setColor(Color.ORANGE);
        g.drawRect(0, 0, (int) Math.abs(width), (int) (height));
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(-width, height);
        for (Cut cut : cuts) {
            if (cut == activeCut) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.BLUE);
            }
            cut.draw(g);
        }
        g2d.translate(width, -height);
    }

    /**
     * save the sheet and its cuts
     */
    public void saveToFile() {
        HashMap<String, String> sheetInfo = new HashMap<>();
        sheetInfo.put("w", "" + (width));
        sheetInfo.put("h", "" + (height));
        sheetInfo.put("hole_file", holeFile.getPath());
        sheetInfo.put("active", activeCutFile.getPath());

        // save sheet info
        SheetParser.saveSheetInfo(sheetFile, sheetInfo);

        // save cuts
        for (Cut cut : cuts) {
            SheetParser.saveCutInfo(cut);
        }
    }

    public Part contains(Point2D point) {
        if (activeCut == null) {
            return null;
        }
        Point2D pointToCheck = new Point2D.Double(-width - point.getX(), height - point.getY());
        for (Part part : activeCut) {
            if (part.contains(pointToCheck)) {
                return part;
            }
        }
        return null;
    }

    public File getActiveCutFile() {
        return activeCutFile;
    }

    public File getSheetFile() {
        return sheetFile;
    }
}
