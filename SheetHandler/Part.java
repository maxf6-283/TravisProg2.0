package SheetHandler;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.SwingUtilities;

import Parser.GCode.NGCDocument;
import Parser.GCode.Parser;
import Parser.GCode.RelativePath2D;

public class Part {
    private double sheetX, sheetY, rotation; // x and y in inches, rotation in radians
    private NGCDocument ngcDoc;
    private File partFile;

    public Part(File partFile, double xLoc, double yLoc, double rot) {
        if (partFile == null) {
            // throw new NullPointerException("Part File cannot be null!");
        } else {
            this.partFile = partFile;
            try {
                File[] files = partFile.listFiles();
                File parent = partFile;
                partFile = null;
                for (File file : files) {
                    if (file.getName().lastIndexOf(".") != -1 && file.getName()
                            .substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).equals("ngc")) {
                        partFile = file;
                        break;
                    }
                }
                if (partFile == null) {
                    throw new FileNotFoundException("Not NGC File found in: " + parent.getPath());
                }
                ngcDoc = Parser.parse(partFile);
            } catch (FileNotFoundException e) {
                System.out.println("File : " + partFile.getAbsolutePath() + " Not Found");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Could not write to System.out!");
                e.printStackTrace();
            }
        }
        sheetX = xLoc;
        sheetY = yLoc;
        rotation = rot;
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
        // TODO: add part drawing
        // g.drawOval((int) (sheetX - 1), (int) (-sheetY - 1), 2, 2);
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(sheetX, -sheetY);
        g2d.rotate(-rotation);
        // ((Graphics2D)g).draw(new Ellipse2D.Double(sheetX-0.5,-sheetY-1,1,2));
        for (RelativePath2D path : ngcDoc.getRelativePath2Ds()) {
            g2d.draw(path);
        }
        g2d.rotate(rotation);
        g2d.translate(-sheetX, sheetY);
    }

    public String toString() {
        return partFile.getName();
    }

    public NGCDocument getNgcDocument() {
        return ngcDoc;
    }
}
