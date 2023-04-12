package SheetHandler;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.awt.Color;

import Parser.Sheet.SheetParser;

public class Sheet {
    private ArrayList<Cut> cuts;
    private Cut activeCut;
    private double width, height; // in inches
    private File sheetFile, holeFile, activeCutFile, parentFile;
    public static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

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
        if (!activeCutFile.exists()) {
            activeCutFile = null;
        }

        // time to get the parts
        parentFile = sheetFile.getParentFile();
        for (File cutFile : parentFile.listFiles()) {
            if (!cutFile.getName().endsWith(".cut")) {
                continue;
            }
            Cut newCut = new Cut(cutFile, holeFile);
            cuts.add(newCut);
            if (activeCutFile != null && cutFile.getName().equals(activeCutFile.getName())) {
                activeCut = newCut;
            }
        }
    }

    public File getParentFile() {
        return parentFile;
    }

    public ArrayList<Cut> getCuts() {
        return cuts;
    }

    public void changeActiveCutFile(File newCut) {
        if (newCut == null || newCut == activeCutFile) {
            return;
        }

        if (!Arrays.stream(parentFile.listFiles()).anyMatch(newCut::equals)) {
            throw new IllegalArgumentException("Cut file is not available!, Internal Error");
        }

        activeCut = cuts.stream().filter(e -> e.getCutFile().equals(newCut)).findFirst().get();
        activeCutFile = activeCut.getCutFile();
    }

    public void removePart(Part part) {
        activeCut.parts.remove(part);
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

    /**
     * Emits the gCode from the active cut into the given file
     * 
     * @param gCodeFile - the file to put the GCode into.
     * @param string
     */
    public void emitGCode(File gCodeFile, String suffix) {

        // specific mechanics: sandwich each part between a translation to and from
        // their position

        // additionally, for each header that's the same aside from comments, merge it
        // and put it at the start

        // and same for footers except put them at the end
        try {
            gCodeFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(gCodeFile));
            String header = "";
            String footer = "";
            for (Part part : activeCut) {
                // ignore parts without the requisite suffix
                if (!part.setSelectedGCode(suffix)) {
                    continue;
                }
                // if the footer changes, write out the old one and remember the new one
                String newFooter = removeGCodeSpecialness(part.getNgcDocument().getGCodeFooter());
                if (!newFooter.equals(footer)) {
                    writer.write(footer);
                    footer = newFooter;
                }

                // if the header changes, write it out
                String newHeader = removeGCodeSpecialness(part.getNgcDocument().getGCodeHeader());
                if (!header.equals(newHeader)) {
                    header = newHeader;
                    writer.write(newHeader);
                }

                // the actual fun stuff
                writer.write(gCodeTranslateTo(part));
                writer.write(part.getNgcDocument().getGCodeBody());
                writer.write(gCodeTranslateFrom(part));
            }

            // write the last footer
            writer.write(footer);

            writer.flush();
            System.out.println("This code is running!");
            writer.close();

        } catch (IOException e) {
            System.err.println("Could not emit GCode into file");

            e.printStackTrace();
        }
    }

    private String gCodeTranslateTo(Part part) {
        double x = part.getX();
        double y = part.getY();
        double rot = part.getRot();

        return String.format("\nG10 L2 P9 X[#5221+%f] Y[#5222+%f] Z[#5223] R%f\nG59.3\n", x, y, rot);
    }

    private String gCodeTranslateFrom(Part part) {
        double x = -part.getX();
        double y = -part.getY();
        double rot = -part.getRot();

        return String.format("\nG10 L2 P9 X[#5221+%f] Y[#5222+%f] Z[#5223] R%f\nG59.3\n", x, y, rot);
    }

    private String removeGCodeSpecialness(String gCode) {
        return gCode.replaceAll("\\(.*\\)", "");
    }

    public Cut getActiveCut() {
        return activeCut;
    }
}
