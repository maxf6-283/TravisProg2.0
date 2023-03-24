package SheetHandler;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import Parser.GCode.NGCDocument;
import Parser.GCode.Parser;

public class Part {
    private double sheetX, sheetY, rotation; // x and y in inches, rotation in radians
    private NGCDocument ngcDoc;
    private File partFile;

    public Part(File partFile, double xLoc, double yLoc, double rot) {
        if(partFile == null){
            //throw new NullPointerException("Part File cannot be null!");
        }
        this.partFile = partFile;
        sheetX = xLoc;
        sheetY = yLoc;
        rotation = rot;
        /*try {
            ngcDoc = Parser.parse(partFile);
        } catch (FileNotFoundException e) {
            System.out.println("File : " + partFile.getAbsolutePath() + " Not Found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Could not write to System.out!");
            e.printStackTrace();
        }*/
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
        //g.drawOval((int) (sheetX - 1), (int) (-sheetY - 1), 2, 2);
        Graphics2D g2d = (Graphics2D)g;
        g2d.rotate(rotation);
        ((Graphics2D)g).draw(new Ellipse2D.Double(sheetX+0.5,-sheetY-1,1,2));
        g2d.rotate(-rotation);
    }

    public String toString() {
        return partFile.getName();
    }

    public NGCDocument getNgcDocument(){
        return ngcDoc;
    }
}
