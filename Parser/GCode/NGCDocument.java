package Parser.GCode;

import java.util.ArrayList;
import java.io.File;
import java.nio.file.Path;
import java.awt.geom.Path2D;

public class NGCDocument {
    private File file;
    private ArrayList<Path2D.Double> geometry;
    private int SpindleSpeed;
    private double ToolOffset = 0.1575;
    private int implicitGCodeHolder;
    private boolean isRelativeArc;
    private boolean isRelative;

    public NGCDocument() {
        this(null);
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
        geometry.add(new Path2D.Double());
    }

    public Path2D.Double getCurrentPath2D() {
        return geometry.get(geometry.size() - 1);
    }

    public void addPath2D(Path2D.Double path){
        geometry.add(path);
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
