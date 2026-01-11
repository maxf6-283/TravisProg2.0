package SheetHandler;

import Display.WarningDialog;
import Parser.GCode.NGCDocument;
import Parser.GCode.NgcStrain;
import Parser.GCode.ToolInfo;
import Parser.Sheet.SheetParser;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class Sheet {
    private ArrayList<Cut> cuts;
    private Cut activeCut;
    private double width, height; // in inches
    private File sheetFile, holeFile, activeCutFile, parentFile;
    // static threadpool to avoid instantiation cost but allow multithreaded gcode
    // parsing
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
        String activeFile = decodedFile.getOrDefault("active", null);
        activeCutFile = activeFile == null ? null : new File(activeFile);

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

    public File getHolesFile() {
        return holeFile;
    }

    public void changeActiveCutFile(File newCut) {
        if (newCut == null || newCut == activeCutFile) {
            return;
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
    public Sheet(
            File sheetFolder, String sheetName, double width, double height, SheetThickness thickness) {
        try {
            File parentFile = new File(sheetFolder, sheetName);
            parentFile.mkdir();
            sheetFile = new File(parentFile, sheetName + ".sheet");
            HashMap<String, String> sheetInfo = new HashMap<>();
            sheetInfo.put("w", "" + width);
            sheetInfo.put("h", "" + height);
            sheetInfo.put("hole_file", thickness.holesFile.getPath().replace("\\", "/"));
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

    /** Adds a part to the active cut; */
    public void addPart(Part part) {
        activeCut.parts.add(part);
    }

    /** Creates a new part and adds it to the active cut */
    public Part addPart(File partFileToPlace, double x, double y) {
        Part part = new Part(partFileToPlace, x, y, 0);
        if (part != null)
            addPart(part);
        return part;
    }

    /** Adds a hole to the active cut */
    public void addHole(Hole hole) {
        activeCut.parts.add(hole);
    }

    /** Adds a new hole to the active cut at the specified position */
    public Hole addHole(double x, double y) {
        Hole hole = new Hole(holeFile, x, y, 0);
        addHole(hole);
        return hole;
    }

    /** Adds a cut to the list and sets it as active */
    public void addCut(Cut cut) {
        activeCut = cut;
        cuts.add(cut);
    }

    /** Draw the sheet to the screen */
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

    /** save the sheet and its cuts */
    public void saveToFile() {
        HashMap<String, String> sheetInfo = new HashMap<>();
        sheetInfo.put("w", "" + (width));
        sheetInfo.put("h", "" + (height));
        String path;
        path = holeFile.getPath().replace("\\", "/");
        sheetInfo.put("hole_file", path);
        path = activeCutFile.getPath().replace("\\", "/");
        sheetInfo.put("active", path.substring(path.indexOf("./")));

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
    public void emitGCode(File gCodeFile, String suffix, List<Integer>... toolOrderArr) {

        // specific mechanics: sandwich each part between a translation to and from
        // their position

        // additionally, for each header that's the same aside from comments, merge
        // it and put it at the start

        // and same for footers except put them at the end
        try {
            activeCut.stream().forEach((Part p) -> p.setSelectedGCode(suffix));
            var docs = activeCut.stream()
                    .map(Part::getNgcDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            var strains = docs.stream().map(NGCDocument::getNgcStrain).collect(Collectors.toList());

            if (strains.stream().distinct().count() > 1) {
                new WarningDialog(
                        new IllegalStateException(),
                        "Cut contains gcode for various machines: Undefined Behavior",
                        null);
            }
            NgcStrain predominantStrain = strains.stream().findAny().get();

            if (!areToolTablesConsistent(docs)) {
                new WarningDialog(
                        new IllegalArgumentException(),
                        "Tool tables for selected files are inconsistent: "
                                + "Undefined Behavior\n"
                                + docs.stream()
                                        .map(NGCDocument::getToolTable)
                                        .map(
                                                map -> map.entrySet().stream()
                                                        .filter(e -> !e.getKey().equals(0))
                                                        .map(e -> e.getKey() + "=" + e.getValue())
                                                        .collect(Collectors.joining(", ", "{", "}")))
                                        .collect(Collectors.joining(", ")),
                        null);
            }

            List<Integer> toolOrder;
            Map<Integer, ToolInfo> toolTable = getMasterToolTable(docs);

            if (predominantStrain != NgcStrain.router_971) {
                if (toolOrderArr.length > 1) {
                    throw new IllegalArgumentException("Only one tool order allowed");
                }
                if (toolOrderArr.length == 1) {
                    toolOrder = toolOrderArr[0];
                } else {
                    toolOrder = new ArrayList<>(toolTable.keySet());
                }
                if (!(toolTable.keySet().containsAll(toolOrder)
                        && toolOrder.containsAll(toolTable.keySet()))) {
                    throw new IllegalArgumentException("Master Tool Table and Tool Order aren't consistent.");
                }
            } else {
                toolOrder = Collections.singletonList(1);
            }
            gCodeFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(gCodeFile));
            final String openBracket = predominantStrain == NgcStrain.router_WinCNC ? "[" : "(";
            final String closeBracket = predominantStrain == NgcStrain.router_WinCNC ? "]" : ")";

            // 2. Write Master Tool Table with Dynamic Brackets
            writer.write(openBracket + "Master Tool Table" + closeBracket + "\n");
            toolTable.entrySet().stream()
                    .filter(e -> e.getKey() != 0)
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(
                            e -> {
                                try {
                                    double diam = e.getValue().toolRadius() * 2;
                                    // Format: (T# D=Diameter) or [T# D=Diameter]
                                    String comment = String.format(
                                            "%sT%d D=%.4f%s\n", openBracket, e.getKey(), diam, closeBracket);
                                    writer.write(comment);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            });

            String header = "";
            String footer = "";
            activeCut.stream().forEach(Part::nullify);
            ArrayList<Part> notEmittedParts = new ArrayList<>();
            for (Integer toolNum : toolOrder) {
                // --- STEP 1: Gather valid parts for this tool ---
                List<Part> partsForTool = new ArrayList<>();
                for (Part part : activeCut) {
                    // Ensure the part has the correct GCode selected
                    if (!part.setSelectedGCode(suffix)) {
                        // Only log warnings once (handled by the else block logic below if
                        // needed)
                        if (!(suffix.equals("holes")
                                || part instanceof Hole
                                || notEmittedParts.stream()
                                        .anyMatch(p -> p.partFile().getName().equals(part.partFile().getName())))) {
                            new WarningDialog(
                                    new FileNotFoundException(), part.partFile().getName() + " missing gcode", null);
                            notEmittedParts.add(part);
                        }
                        continue;
                    }

                    // Check if part uses this tool (code is not empty)
                    // Note: We use the tool-specific transform here to check validity
                    String testCode;
                    if (predominantStrain == NgcStrain.router_971)
                        testCode = predominantStrain.gCodeParser.gCodeTransformClean(part);
                    else
                        testCode = predominantStrain.gCodeParser.gCodeTransformClean(part, toolNum);

                    if (!testCode.trim().isEmpty()) {
                        partsForTool.add(part);
                    }
                }

                // --- STEP 2: Optimize the order for THIS tool ---
                List<Part> sortedParts = getOptimizedPartOrder(partsForTool);

                boolean toolChangeWritten = false;

                // --- STEP 3: Write the sorted parts ---
                for (Part part : sortedParts) {

                    // Re-fetch header/footer (state updates)
                    String newFooter = predominantStrain.gCodeParser.removeGCodeSpecialness(
                            part.getNgcDocument().getGCodeFooter());
                    if (!newFooter.equals(footer)) {
                        writer.write(footer);
                        footer = newFooter;
                    }
                    String newHeader = predominantStrain.gCodeParser.removeGCodeSpecialness(
                            part.getNgcDocument().getGCodeHeader());
                    if (!newHeader.equals(header)) {
                        writer.write(newHeader);
                        header = newHeader;
                    }

                    // Get final code
                    String cleanCode;
                    if (predominantStrain == NgcStrain.router_971)
                        cleanCode = predominantStrain.gCodeParser.gCodeTransformClean(part);
                    else
                        cleanCode = predominantStrain.gCodeParser.gCodeTransformClean(part, toolNum);

                    if (!cleanCode.trim().isEmpty()) {
                        // Write Tool Change (Once per group)
                        if (!toolChangeWritten) {
                            writer.write(predominantStrain.gCodeParser.getToolCode(toolNum) + "\n");
                            toolChangeWritten = true;
                        }

                        // Write Part
                        writer.write(openBracket + "Part: " + part.partFile().getName() + closeBracket);
                        writer.write(cleanCode);
                    }
                }
            }

            // write the last footer
            writer.write(footer);

            writer.flush();
            writer.close();

        } catch (IOException e) {
            System.err.println("Could not emit GCode into file");

            e.printStackTrace();
        }
    }

    public static boolean areToolTablesConsistent(List<NGCDocument> documents) {
        Map<Integer, ToolInfo> master = new HashMap<>();

        for (NGCDocument doc : documents) {
            for (var entry : doc.getToolTable().entrySet()) {
                // putIfAbsent returns the EXISTING value if present, or null if it was
                // just added. This does the lookup and insertion in a single
                // atomic-like step.
                ToolInfo existing = master.putIfAbsent(entry.getKey(), entry.getValue());

                // If existing is NOT null, it means we saw this tool before. Check for
                // conflict.
                if (existing != null && !existing.equals(entry.getValue())) {
                    return false; // Conflict found, stop immediately
                }
            }
        }
        return true;
    }

    public static Map<Integer, ToolInfo> getMasterToolTable(List<NGCDocument> documents) {
        Map<Integer, ToolInfo> master = new HashMap<>();

        for (NGCDocument doc : documents) {
            for (var entry : doc.getToolTable().entrySet()) {
                // putIfAbsent returns the EXISTING value if present, or null if it was
                // just added. This does the lookup and insertion in a single
                // atomic-like step.
                ToolInfo existing = master.putIfAbsent(entry.getKey(), entry.getValue());

                // If existing is NOT null, it means we saw this tool before. Check for
                // conflict.
                if (existing != null && !existing.equals(entry.getValue())) {
                    throw new IllegalStateException(
                            "Consistency should be checked before getting master table.");
                }
            }
        }
        return master;
    }

    /**
     * Optimizes part order by trying every part as a starting point and running
     * Nearest Neighbor.
     * Returns the sequence with the minimum total travel distance.
     */
    private List<Part> getOptimizedPartOrder(List<Part> parts) {
        if (parts.isEmpty())
            return new ArrayList<>();
        if (parts.size() == 1)
            return new ArrayList<>(parts);

        List<Part> bestOrder = null;
        double minTotalDistance = Double.MAX_VALUE;

        // Try starting at every possible part to find the best chain
        for (int i = 0; i < parts.size(); i++) {
            List<Part> currentOrder = new ArrayList<>();
            List<Part> unvisited = new ArrayList<>(parts);

            // Pick start node
            Part current = unvisited.remove(i);
            currentOrder.add(current);

            double currentTotalDist = 0;

            // Run Nearest Neighbor from this start node
            while (!unvisited.isEmpty()) {
                Part nearest = null;
                double minDist = Double.MAX_VALUE;

                for (Part p : unvisited) {
                    // Calculate Euclidean distance
                    double dx = current.getCenterPoint().getCenterX() - p.getCenterPoint().getCenterX();
                    double dy = current.getCenterPoint().getCenterY() - p.getCenterPoint().getCenterY();
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist < minDist) {
                        minDist = dist;
                        nearest = p;
                    }
                }

                currentTotalDist += minDist;

                // Optimization: Abort if we already exceed the best path found so far
                if (currentTotalDist >= minTotalDistance) {
                    break;
                }

                current = nearest;
                currentOrder.add(nearest);
                unvisited.remove(nearest);
            }

            // Check if this full path is the new best
            if (unvisited.isEmpty() && currentTotalDist < minTotalDistance) {
                minTotalDistance = currentTotalDist;
                bestOrder = currentOrder;
            }
        }
        return bestOrder;
    }

    public Cut getActiveCut() {
        return activeCut;
    }
}
