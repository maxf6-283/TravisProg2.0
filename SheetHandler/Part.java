package SheetHandler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import Display.ErrorDialog;
import Display.Returnable;
import Display.WarningDialog;
import Parser.GCode.NGCDocument;
import Parser.GCode.Parser;
import Parser.GCode.RelativePath2D;

public class Part {
    private double sheetX, sheetY, rotation; // x and y in inches, rotation in radians
    private ArrayList<NGCDocument> activeNgcDocs = new ArrayList<>();
    private ArrayList<NGCDocument> allNGCDocs = new ArrayList<>();
    private NGCDocument emitNGCDoc;
    private File partFile;
    private boolean selected = false;
    private ArrayList<Future<NGCDocument>> futures = new ArrayList<>();
    private boolean exist = true;
    //private Shape outline;

    public Part(File partFile, double xLoc, double yLoc, double rot) {
        if (partFile == null) {
            throw new NullPointerException("Part File cannot be null!");
        } else {
            this.partFile = partFile;
            try {
                File[] files = new File[0];
                if (this instanceof Hole) {
                    files = new File[] { partFile };
                } else {
                    files = partFile.listFiles();
                }
                ArrayList<File> ngcFiles = new ArrayList<>();
                File parent = partFile;
                if (files == null) {
                    new WarningDialog(new NullPointerException(), "Folder: " + parent.getName() + " Not Found", null);
                    exist = false;
                    return;
                }
                for (File file : files) {
                    if (file.getName().lastIndexOf(".") != -1 && file.getName()
                            .substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).equals("ngc")) {
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
                    if (i == newFiles.size()-1) {
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
        sheetX = xLoc;
        sheetY = yLoc;
        rotation = rot;

        //generateOutline();
    }

    public boolean exists() {
        return exist;
    }

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
                if (file.getName().lastIndexOf(".") != -1 && file.getName()
                        .substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).equals("ngc")) {
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
                if (i == newFiles.size()-1) {
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
    private boolean checkFutures() {
        ArrayList<Future<NGCDocument>> toRemove = new ArrayList<>();
        for(Future<NGCDocument> future : futures) {
            if(future.isDone()) {
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

    public ArrayList<NGCDocument> getAllNgcDocuments() {
        checkFutures();
        return allNGCDocs;
    }

    public void nullify() {
        emitNGCDoc = null;
    }

    public boolean setSelectedGCode(String suffix) {
        if(this instanceof Hole) {
            emitNGCDoc = allNGCDocs.get(0);
            return suffix.equals("holes");
        }
        checkFutures();
        for(NGCDocument ngcDocument : allNGCDocs) {
            String fileName = ngcDocument.getGcodeFile().getName();
            int lastIndexOf_ = fileName.lastIndexOf('_')+1;
            if(suffix.equals(fileName.substring(lastIndexOf_ == -1? 0 : lastIndexOf_, fileName.lastIndexOf('.')))) {
                emitNGCDoc = ngcDocument;
                return true;
            }
        }

        return false;
    }

    public NGCDocument getNgcDocument() {
        return emitNGCDoc;
    }

    public String[] getSuffixes() {
        if(this instanceof Hole) {
            return new String[]{"holes"};
        }
        checkFutures();
        String[] suffixes = new String[allNGCDocs.size()];
        for(int i = 0; i < suffixes.length; i++) {
            String fileName = allNGCDocs.get(i).getGcodeFile().getName();
            int lastIndexOf_ = fileName.lastIndexOf('_');
            suffixes[i] = fileName.substring(lastIndexOf_ == -1? 0 : lastIndexOf_+1, fileName.lastIndexOf('.'));
        }
        return suffixes;
    }

    public void addActiveGcode(NGCDocument doc) {
        checkFutures();
        if (!allNGCDocs.stream().anyMatch(e -> e.equals(doc))) {
            throw new IllegalArgumentException("GCode doc dothe not appataine to this part");
        }
        activeNgcDocs.add(doc);
    }

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
            g2d.setStroke(
                    new BasicStroke((float) activeNgcDoc.getToolOffset(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            100000));
            activeNgcDoc.getRelativePath2Ds().stream().forEach(e -> g2d.draw(e));
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

    public boolean equivalent(Object obj){
        if (((Part)obj).getSelected()){
            return true;
        }
        return false;
    }

    /*@Deprecated
    public void generateOutline() {
        // outline = new Area(ngcDoc.getCurrentPath2D());
        Stroke stroke = new BasicStroke((float) activeNgcDoc.getToolOffset(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
                0);
        // Area strokeShape = new Area(stroke.createStrokedShape(outline));

        RelativePath2D temp = activeNgcDoc.getCurrentPath2D();
        for (RelativePath2D path : activeNgcDoc.getRelativePath2Ds()) {
            if (calcArea(path.getBounds2D()) > calcArea(temp.getBounds2D())) {
                temp = path;
            }
        }

        outline = stroke.createStrokedShape(temp);
    }*/

    // public void generateOutline() {
    //     //outline = new Area(ngcDoc.getCurrentPath2D());
    //     Stroke stroke = new BasicStroke((float) ngcDoc.getToolOffset(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0);
    //     //Area strokeShape = new Area(stroke.createStrokedShape(outline));

    //     RelativePath2D temp = ngcDoc.getCurrentPath2D();
    //     for (RelativePath2D path : ngcDoc.getRelativePath2Ds()) {
    //         if(calcArea(path.getBounds2D()) > calcArea(temp.getBounds2D())){
    //             temp = path;
    //         }
    //     }

    //     outline = stroke.createStrokedShape(temp);
    // }

    // private double calcArea(Rectangle2D rect){
    //     return rect.getWidth()*rect.getHeight();
    // }
}
