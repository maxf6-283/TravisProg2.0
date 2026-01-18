package SheetHandler;

import Display.ErrorDialog;
import Display.Screen;
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** holds all information and methods related to a part */
public class Part {
    private double sheetX, sheetY, rotation; // x and y in inches, rotation in radians
    protected ArrayList<NGCDocument> activeNgcDocs = new ArrayList<>();
    private ArrayList<NGCDocument> allNGCDocs = new ArrayList<>();
    protected NGCDocument emitNGCDoc;
    private File partFile;
    private boolean selected = false;
    private ArrayList<Future<NGCDocument>> futures = new ArrayList<>(); // holds all future for concurrent gcode file
    // parsing
    private boolean exist = true;

    // private Shape outline;

    public Part(File partFile, double xLoc, double yLoc, double rot) {
        if (partFile == null) {
            throw new NullPointerException("Part File cannot be null!");
        } else {
            this.partFile = partFile;
            try {
                File[] files = new File[0];

                // either creates array of all the different files in the part folder or
                // the gcode file for the hole(unecessary but good practice)
                if (this instanceof Hole) {
                    files = new File[] { partFile };
                } else {
                    files = partFile.listFiles();
                }

                ArrayList<File> ngcFiles = new ArrayList<>();
                File parent = partFile;

                // throws error of the folder of a part cannot be found
                if (files == null) {
                    new WarningDialog(
                            new NullPointerException(), "Folder: " + parent.getName() + " Not Found", null);
                    exist = false;
                    return;
                }

                // adds all the gcode files(with .ngc ext.) to the arraylist of gcode
                // files
                for (File file : files) {
                    if (file.getName().lastIndexOf(".") != -1) {
                        String ext = file.getName()
                                .substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
                        if (ext.equals("ngc") || ext.equals("tap")) {
                            ngcFiles.add(file);
                        }
                    }
                }

                // throws error if no ngc files found
                if (ngcFiles.size() <= 0) {
                    new ErrorDialog(new FileNotFoundException(), "No NGC File found in: " + parent.getPath());
                }

                // checks if file has already been parsed previously(for multiple of
                // one part), else add to new files list
                ArrayList<File> newFiles = new ArrayList<>();
                for (File file : ngcFiles) {
                    end: {
                        for (NGCDocument doc : Parser.parsedDocuments) {
                            if (doc.getGcodeFile().equals(file)) {
                                allNGCDocs.add(doc);
                                break end;
                            }
                        }
                        newFiles.add(file);
                    }
                }

                // submits each parsing to the executor pool and parses the last one
                for (int i = 0; i < newFiles.size(); i++) {
                    if (Screen.DebugMode || i == newFiles.size() - 1) { // debugMode stops any parallelization
                        allNGCDocs.add(Parser.parse(newFiles.get(i))); // prevents weird get ahead
                        // errors(can still be prone i)
                    } else {
                        futures.add(Sheet.executor.submit(new Parser(newFiles.get(i))));
                    }
                }

                activeNgcDocs.add(allNGCDocs.get(0));
            } catch (FileNotFoundException e) {
                new ErrorDialog(e, "File : " + partFile.getAbsolutePath() + " Not Found");
            } catch (ConcurrentModificationException e) {
                new ErrorDialog(e);
            }
        }
        sheetX = xLoc;
        sheetY = yLoc;
        rotation = rot;

        // generateOutline();
    }

    /**
     * @return whether this part truly exists or not
     */
    public boolean exists() {
        return exist;
    }

    /** Pretty much reinstantiates this Part */
    public void reload() {
        try {
            allNGCDocs.clear();
            activeNgcDocs.clear();
            emitNGCDoc = null;
            File[] files = new File[0];
            if (this instanceof Hole) {
                files = new File[] { partFile };
            } else {
                files = partFile.listFiles();
            }
            ArrayList<File> ngcFiles = new ArrayList<>();
            File parent = partFile;
            if (files == null) {
                new ErrorDialog(new NullPointerException(), "Folder: " + parent.getName() + " Not Found");
            }
            for (File file : files) {
                if (file.getName().lastIndexOf(".") != -1
                        && file.getName()
                                .substring(file.getName().lastIndexOf(".") + 1, file.getName().length())
                                .equals("ngc")) {
                    ngcFiles.add(file);
                }
            }
            if (ngcFiles.size() <= 0) {
                new ErrorDialog(new FileNotFoundException(), "No NGC File found in: " + parent.getPath());
            }
            ArrayList<File> newFiles = new ArrayList<>();
            for (File file : ngcFiles) {
                end: {
                    for (NGCDocument doc : Parser.parsedDocuments) {
                        if (doc.getGcodeFile().equals(file)) {
                            allNGCDocs.add(doc);
                            break end;
                        }
                    }
                    newFiles.add(file);
                }
            }
            for (int i = 0; i < newFiles.size(); i++) {
                if (i == newFiles.size() - 1) {
                    allNGCDocs.add(Parser.parse(newFiles.get(i)));
                } else {
                    futures.add(Sheet.executor.submit(new Parser(newFiles.get(i))));
                }
            }
            activeNgcDocs.add(allNGCDocs.get(0));
        } catch (FileNotFoundException e) {
            new ErrorDialog(e, "File : " + partFile.getAbsolutePath() + " Not Found");
        } catch (ConcurrentModificationException e) {
            new ErrorDialog(e);
        }
    }

    /**
     * @return true if all futures have returned
     */
    protected boolean checkFutures() {
        ArrayList<Future<NGCDocument>> toRemove = new ArrayList<>();
        for (Future<NGCDocument> future : futures) {
            if (future.isDone()) {
                try {
                    allNGCDocs.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    new ErrorDialog(e);
                }
                toRemove.add(future);
            }
        }
        futures.removeAll(toRemove);
        return futures.size() == 0;
    }

    /**
     * @return list of all the NGCDocuments this part has
     */
    public ArrayList<NGCDocument> getAllNgcDocuments() {
        checkFutures();
        return allNGCDocs;
    }

    /** Makes the file to emit with null */
    public void nullify() {
        emitNGCDoc = null;
    }

    /**
     * @param suffix the suffix of which to match the emitted cut to
     * @return whether that suffix exists in a gcode file
     */
    public boolean setSelectedGCode(String suffix, File outputFile, boolean useDrillCycle) {
        checkFutures();

        // 1. Determine Target Strain based on output file extension
        String outExt = "";
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

        // 2. PASS 1: Find Exact Match (Suffix AND Strain)
        for (NGCDocument ngcDocument : allNGCDocs) {
            String fileName = ngcDocument.getGcodeFile().getName();
            int lastIndexOf_ = fileName.lastIndexOf('_') + 1;
            String fileSuffix = fileName.substring(lastIndexOf_ == -1 ? 0 : lastIndexOf_, fileName.lastIndexOf('.'));

            if (suffix.equals(fileSuffix) && ngcDocument.getNgcStrain() == targetStrain) {
                emitNGCDoc = ngcDocument;
                return true;
            }
        }

        // 3. PASS 2: Fallback (Suffix Only)
        // If we didn't find the specific strain (e.g. only .ngc exists but we want
        // .tap), take what we
        // have.
        for (NGCDocument ngcDocument : allNGCDocs) {
            String fileName = ngcDocument.getGcodeFile().getName();
            int lastIndexOf_ = fileName.lastIndexOf('_') + 1;
            String fileSuffix = fileName.substring(lastIndexOf_ == -1 ? 0 : lastIndexOf_, fileName.lastIndexOf('.'));

            if (suffix.equals(fileSuffix)) {
                emitNGCDoc = ngcDocument;
                return true;
            }
        }
        emitNGCDoc = null;

        return false;
    }

    /**
     * @return the current active emit NGCDocument
     */
    public NGCDocument getNgcDocument() {
        return emitNGCDoc;
    }

    /**
     * @return an array of all the suffixes of gcode files
     */
    public String[] getSuffixes() {
        if (this instanceof Hole) {
            return new String[] { "holes" };
        }
        checkFutures();
        String[] suffixes = new String[allNGCDocs.size()];
        for (int i = 0; i < suffixes.length; i++) {
            String fileName = allNGCDocs.get(i).getGcodeFile().getName();
            int lastIndexOf_ = fileName.lastIndexOf('_');
            suffixes[i] = fileName.substring(lastIndexOf_ == -1 ? 0 : lastIndexOf_ + 1, fileName.lastIndexOf('.'));
        }
        return suffixes;
    }

    /**
     * @param doc adds this the list of NGDocuments to be drawn
     */
    public void addActiveGcode(NGCDocument doc) {
        checkFutures();
        if (!allNGCDocs.stream().anyMatch(e -> e.equals(doc))) {
            throw new IllegalArgumentException("GCode doc dothe not appataine to this part");
        }
        activeNgcDocs.add(doc);
    }

    /**
     * @param doc removes this the list of NGDocuments to be drawn
     */
    public void removeActiveGcode(NGCDocument doc) {
        checkFutures();
        if (!activeNgcDocs.stream().anyMatch(e -> e.equals(doc))) {
            throw new IllegalArgumentException("GCode doc dothe not appataine to this part");
        }
        activeNgcDocs.removeAll(Arrays.asList(doc));
    }

    public boolean contains(Point2D point) {
        for (NGCDocument activeNgcDoc : activeNgcDocs) {
            for (RelativePath2D path : activeNgcDoc.getRelativePath2Ds()) {
                Point2D.Double pointToCheck = new Point2D.Double(point.getX(), -point.getY());
                pointToCheck.setLocation(pointToCheck.getX() + sheetX, pointToCheck.getY() + sheetY);

                pointToCheck.setLocation(
                        pointToCheck.getX() * Math.cos(-rotation) + pointToCheck.getY() * -Math.sin(-rotation),
                        pointToCheck.getX() * Math.sin(-rotation) + pointToCheck.getY() * Math.cos(-rotation));

                pointToCheck.setLocation(-pointToCheck.getX(), pointToCheck.getY());

                if (path.contains(pointToCheck)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return selected;
    }

    public double getX() {
        return sheetX;
    }

    public Rectangle2D getCenterPoint() {
        HashMap<Integer, ArrayList<RelativePath2D>> layers = emitNGCDoc.getToolpathLayers();
        Rectangle2D boundingBox = null;
        for (Integer toolNum : layers.keySet()) {
            for (RelativePath2D path : layers.get(toolNum)) {
                if (boundingBox == null) {
                    boundingBox = path.getBounds2D();
                } else {
                    Rectangle2D.union(boundingBox, path.getBounds2D(), boundingBox);
                }
            }
        }
        return boundingBox;
    }

    public void setX(double x) {
        sheetX = x;
    }

    public double getY() {
        return sheetY;
    }

    public void setY(double y) {
        sheetY = y;
    }

    public double getRot() {
        return rotation;
    }

    public void setRot(double rot) {
        rotation = rot;
    }

    public File partFile() {
        return partFile;
    }

    /**
     * translates the reference frame to the part, then draws all RelativePath2Ds
     * from each Active
     * NGCDocument
     *
     * @param g Graphics instance to be drawn to
     */
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform prevTransform = g2d.getTransform();
        g2d.translate(sheetX, -sheetY);
        Color prevColor = g.getColor();
        g2d.rotate(-rotation);
        if (selected == true) {
            g.setColor(Color.RED);
        } /*
           * else {
           * g.setColor(Color.ORANGE);
           * }
           */
        Stroke currentStrok = g2d.getStroke();
        // ((Graphics2D)g).draw(new Ellipse2D.Double(sheetX-0.5,-sheetY-1,1,2));

        for (NGCDocument activeNgcDoc : activeNgcDocs) {
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
        g2d.setColor(prevColor);
        g2d.setStroke(currentStrok);

        // g2d.draw(outline);

        g2d.setTransform(prevTransform);
    }

    public String toString() {
        return partFile.getName();
    }

    public ArrayList<NGCDocument> getNgcDocuments() {
        return activeNgcDocs;
    }

    public boolean equivalent(Object obj) {
        if (((Part) obj).getSelected()) {
            return true;
        }
        return false;
    }

    /*
     * @Deprecated
     * public void generateOutline() {
     * // outline = new Area(ngcDoc.getCurrentPath2D());
     * Stroke stroke = new BasicStroke((float) activeNgcDoc.getToolOffset(),
     * BasicStroke.CAP_ROUND,
     * BasicStroke.JOIN_ROUND,
     * 0);
     * // Area strokeShape = new Area(stroke.createStrokedShape(outline));
     *
     * RelativePath2D temp = activeNgcDoc.getCurrentPath2D();
     * for (RelativePath2D path : activeNgcDoc.getRelativePath2Ds()) {
     * if (calcArea(path.getBounds2D()) > calcArea(temp.getBounds2D())) {
     * temp = path;
     * }
     * }
     *
     * outline = stroke.createStrokedShape(temp);
     * }
     */

    // public void generateOutline() {
    // //outline = new Area(ngcDoc.getCurrentPath2D());
    // Stroke stroke = new BasicStroke((float) ngcDoc.getToolOffset(),
    // BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0);
    // //Area strokeShape = new Area(stroke.createStrokedShape(outline));

    // RelativePath2D temp = ngcDoc.getCurrentPath2D();
    // for (RelativePath2D path : ngcDoc.getRelativePath2Ds()) {
    // if(calcArea(path.getBounds2D()) > calcArea(temp.getBounds2D())){
    // temp = path;
    // }
    // }

    // outline = stroke.createStrokedShape(temp);
    // }

    // private double calcArea(Rectangle2D rect){
    // return rect.getWidth()*rect.getHeight();
    // }
}
