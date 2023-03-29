package SheetHandler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import Parser.GCode.NGCDocument;
import Parser.GCode.Parser;
import Parser.GCode.RelativePath2D;

public class Part {
    private double sheetX, sheetY, rotation; // x and y in inches, rotation in radians
    private NGCDocument ngcDoc;
    private File partFile;
    private boolean selected = false;

    public Part(File partFile, double xLoc, double yLoc, double rot) {
        if (partFile == null) {
            // throw new NullPointerException("Part File cannot be null!");
        } else {
            this.partFile = partFile;
            try {
                File[] files = new File[0];
                if(this instanceof Hole){
                    files = new File[]{partFile};
                } else {
                    files = partFile.listFiles();
                }
                File parent = partFile;
                partFile = null;
                if(files == null){
                    throw new NullPointerException("Folder: "+parent.getName()+" Not Found");
                }
                for (File file : files) {
                    if (file.getName().lastIndexOf(".") != -1 && file.getName()
                            .substring(file.getName().lastIndexOf(".") + 1, file.getName().length()).equals("ngc")) {
                        partFile = file;
                        break;
                    }
                }
                if (partFile == null) {
                    throw new FileNotFoundException("No NGC File found in: " + parent.getPath());
                }
                for(NGCDocument doc : Parser.parsedDocuments){
                    if(doc.getFile().equals(partFile)){
                        ngcDoc = doc;
                    }
                }
                if(ngcDoc == null){
                    ngcDoc = Parser.parse(partFile);
                }
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

    public boolean contains(Point2D point){
        for(RelativePath2D path: ngcDoc.getRelativePath2Ds()){
            Point2D.Double pointToCheck = new Point2D.Double(point.getX(), -point.getY());
            pointToCheck.setLocation(pointToCheck.getX() + sheetX, pointToCheck.getY() + sheetY);
            
            pointToCheck.setLocation(pointToCheck.getX() * Math.cos(-rotation) + pointToCheck.getY() * -Math.sin(-rotation), pointToCheck.getX() * Math.sin(-rotation) + pointToCheck.getY() * Math.cos(-rotation));
            
            pointToCheck.setLocation(-pointToCheck.getX(), pointToCheck.getY());

            if(path.contains(pointToCheck)){
                return true;
            }
        }
        return false;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public boolean getSelected(){
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
        if(selected == true){
            g.setColor(Color.RED);
        }
        g2d.rotate(-rotation);
        // ((Graphics2D)g).draw(new Ellipse2D.Double(sheetX-0.5,-sheetY-1,1,2));
        for (RelativePath2D path : ngcDoc.getRelativePath2Ds()) {
            g2d.draw(path);
        }
        g2d.setTransform(prevTransform);
        g.setColor(Color.GREEN);
    }

    public String toString() {
        return partFile.getName();
    }

    public NGCDocument getNgcDocument() {
        return ngcDoc;
    }

    @Override
    public boolean equals(Object obj){
        if (((Part)obj).getSelected() == true){
            return true;
        }
        return false;
    }
}
