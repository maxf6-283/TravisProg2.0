package Parser.GCode;

import java.util.ArrayList;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.File;

public class NGCDocument {
    private File file;
    private ArrayList<RelativePath2D> geometry;
    private Point3D currentPoint;
    private int SpindleSpeed;
    private double ToolOffset = 0.1575;
    private int implicitGCodeHolder;
    private boolean isRelativeArc;
    private boolean isRelative;

    public NGCDocument() {
        this(null);
    }

    public boolean contains(Point2D point){
        for(RelativePath2D path:geometry){
            if(path.contains(point)){
                return true;
            }
        }
        return false;
    }

    public void setIsRelative(boolean isRelative){
        this.isRelative = isRelative;
    }

    public boolean getRelativity(){
        return isRelative;
    }

    public void setIsRelativeArc(boolean isRelativeArc){
        this.isRelativeArc = isRelativeArc;
    }

    public boolean getRelativityArc(){
        return isRelativeArc;
    }

    public NGCDocument(File file) {
        this.file = file;
        geometry = new ArrayList<>();
        geometry.add(new RelativePath2D());
    }

    public RelativePath2D getCurrentPath2D() {
        return geometry.get(geometry.size() - 1);
    }

    public void newPath2D(){
        geometry.add(new RelativePath2D());
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile(File file) {
        return file;
    }

    public void setSpindleSpeed(int SpindleSpeed) {
        this.SpindleSpeed = SpindleSpeed;
    }

    public int getSpindleSpeed() {
        return SpindleSpeed;
    }
}
