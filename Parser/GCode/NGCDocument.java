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
    private double implicitGCodeHolder;
    private boolean isRelativeArc;
    private boolean isRelative;
    private int currentAxisPlane = 0;

    public NGCDocument() {
        this(null);
    }

    public int getCurrentAxisPlane(){
        if(currentAxisPlane <17 || currentAxisPlane >19){
            throw new IllegalGCodeError("Axis Plane "+currentAxisPlane+" is not supported or needs to be called before an arc");
        }
        return currentAxisPlane;
    }

    public void setCurrentAxisPlane(int GCode){
        if(currentAxisPlane <17 || currentAxisPlane >19){
            throw new IllegalGCodeError("Axis Plane "+currentAxisPlane+" is not supported or needs to be called before an arc");
        }
        currentAxisPlane = GCode;
    }

    public void setCurrentPoint(Point3D point){
        currentPoint = point;
    }

    public Point3D getCurrentPointr(){
        return currentPoint;
    }

    public void setGCodeHolder(double GCode){
        implicitGCodeHolder = GCode;
    }

    public double getGCodeHolder(){
        return implicitGCodeHolder;
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
