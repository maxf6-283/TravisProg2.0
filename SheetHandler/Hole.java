package SheetHandler;

import Display.WarningDialog;
import Parser.GCode.NGCDocument;
import Parser.GCode.NgcStrain;
import Parser.GCode.Parser;
import Parser.GCode.RelativePath2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/** Holes are essentially identical to Parts besides drawing */
public class Hole extends Part {
    public static final double HEAD_SIZE = Double.parseDouble(Settings.settings.get("ScrewHeadSize"));
    private static HashMap<Double, NGCDocument> cachedDrillCycleDocs = new HashMap<>();
    private final Path holeFile;
    private static boolean hasWarned = false;

    public Hole(Path holeFile, double x, double y, double rot) {
        super(holeFile.toFile(), x, y, rot);

        this.holeFile = holeFile;

        try {
            String fileName = holeFile.getFileName().toString();
            String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
            String originalExt = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";

            String altExt = originalExt.equalsIgnoreCase("ngc") ? "tap" : "ngc";
            Path altPath = holeFile.resolveSibling(baseName + "." + altExt);

            if (altPath.toFile().exists()) {
                NGCDocument altDoc = Parser.parse(altPath.toFile());
                getAllNgcDocuments().add(altDoc);
            }
        } catch (Exception e) {
            System.err.println("Failed to load alternative hole file.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean setSelectedGCode(String suffix, java.io.File outputFile, boolean useDrillCycle) {
        if (!suffix.equals("holes")) {
            emitNGCDoc = null;
            return false;
        }
        super.checkFutures();

        String outExt = "";
        double thickness = Double.parseDouble(
                "0."
                        + Pattern.compile("-?\\d+")
                                .matcher(holeFile.getFileName().toString())
                                .results()
                                .mapToInt(m -> Integer.parseInt(m.group()))
                                .max()
                                .orElse(0));

        if (outputFile != null) {
            String name = outputFile.getName();
            outExt = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
        }

        NgcStrain targetStrain;
        if (outExt.equalsIgnoreCase("tap")) {
            targetStrain = NgcStrain.router_WinCNC;
        } else {
            targetStrain = NgcStrain.router_971;
        }

        // 1. Force manual doc if drill cycle requested for WinCNC
        if (useDrillCycle && targetStrain == NgcStrain.router_WinCNC) {
            activeNgcDocs.add(generateManualWinCNCDoc(thickness));
            emitNGCDoc = activeNgcDocs.get(activeNgcDocs.size() - 1);
            return true;
        }

        // 2. Try to find a matching file
        for (NGCDocument doc : getAllNgcDocuments()) {
            if (doc.getNgcStrain() == targetStrain) {
                addActiveGcode(doc);
                emitNGCDoc = doc;
                return true;
            }
        }

        // 3. Fallback to manual doc for WinCNC if no file found
        if (targetStrain == NgcStrain.router_WinCNC) {
            if (!hasWarned)
                new WarningDialog(
                        new FileNotFoundException(),
                        "Hole file for this sheet not found for the shopsabre, defaulting to drill cycle: "
                                + holeFile.getFileName(),
                        null);
            hasWarned = true;
            activeNgcDocs.add(generateManualWinCNCDoc(thickness));
            emitNGCDoc = activeNgcDocs.get(activeNgcDocs.size() - 1);
            return true;
        }

        return true;
    }

    private NGCDocument generateManualWinCNCDoc(double thickness) {
        if (!cachedDrillCycleDocs.containsKey(thickness)) {
            NGCDocument doc = new NGCDocument(null, NgcStrain.router_WinCNC);
            double depth = thickness + 0.01;
            NgcStrain strain = NgcStrain.router_WinCNC;

            String[] lines = new String[] {
                    "[Drill Hole]",
                    String.format("[T5 D=0.125 CR=0. TAPER=118deg - ZMIN=-%.4f - drill]", depth),
                    "G90",
                    "G20",
                    "G53 Z",
                    "[Drill2 (2)]",
                    "T5",
                    "S8000",
                    "M3",
                    "G4 X4.",
                    "G90",
                    "G0 X0. Y0.",
                    "Z0.6",
                    "G0 Z0.2",
                    String.format("G83 X0. Y0. Z-%.4f R0.2 Q0.0312 F40.", depth),
                    "G80",
                    "Z0.6",
                    "G53 Z",
                    "M5",
                    "G53 P10"
            };
            int lineNum = 1;
            for (String line : lines) {
                doc.addToString(line);
                lineNum++;
                String parseLine = line;

                parseLine = strain.commentsParser.parse(parseLine, lineNum, doc);
                if (parseLine.contains("M")) {
                    parseLine = strain.mCodeParser.parse(parseLine, lineNum, doc);
                } else if (parseLine.length() > 2) {
                    parseLine = strain.gCodeParser.parse(parseLine, lineNum, doc);
                }
                parseLine = strain.endParser.parse(parseLine, lineNum, doc);
            }

            cachedDrillCycleDocs.put(thickness, doc);
        }

        NGCDocument doc = cachedDrillCycleDocs.get(thickness);
        emitNGCDoc = doc;
        return doc;
    }

    @Override
    // draws circle at point instead of gcode
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform prevTransform = g2d.getTransform();
        g2d.translate(getX(), -getY());
        Color prevColor = g.getColor();
        g2d.rotate(-getRot());
        if (getSelected() == true) {
            g.setColor(Color.RED);
        } /*
           * else {
           * g.setColor(Color.ORANGE);
           * }
           */
        Stroke currentStrok = g2d.getStroke();
        // ((Graphics2D)g).draw(new Ellipse2D.Double(getX()-0.5,-sheetY-1,1,2));
        for (NGCDocument activeNgcDoc : getNgcDocuments()) {
            HashMap<Integer, ArrayList<RelativePath2D>> layers = activeNgcDoc.getToolpathLayers();
            for (Integer toolNum : layers.keySet()) {
                // 1. Get radius for this specific tool layer
                double radius = activeNgcDoc.getToolOffset(toolNum).toolRadius();

                // 2. Calculate diameter (stroke width). Ensure it's visible (>0)
                float strokeWidth = (float) (radius * 2.0);
                if (strokeWidth <= 0.001)
                    strokeWidth = 0.01f;

                // 3. Set the stroke
                g2d.setStroke(
                        new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 100000));

                // 4. Draw only the paths for this tool
                layers.get(toolNum).forEach(e -> g2d.draw(e));
            }
        }
        g2d.setStroke(currentStrok);
        g2d.setColor(Color.ORANGE);
        g2d.draw(new Ellipse2D.Double(-HEAD_SIZE / 2, -HEAD_SIZE / 2, HEAD_SIZE, HEAD_SIZE));
        g2d.setColor(prevColor);
        g2d.setTransform(prevTransform);
    }
}
