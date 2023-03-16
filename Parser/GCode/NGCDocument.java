package Parser.GCode;

import java.util.ArrayList;
import java.io.File;
import java.awt.geom.GeneralPath;

public class NGCDocument {
    private File file;
    private ArrayList<GeneralPath> geometry;
    private int SpindleSpeed;
    private double ToolOffset = 0.1575;
    private static final int INITIAL = 0;
    private static final int CUTTING = 1;
    private static final int RAPID = 2;
    private static final int DONE = 3;
    private int implicitGCodeHolder;
    private int state = INITIAL;

    public NGCDocument(){
        file = null;
    }

    public NGCDocument(File file){
        this.file = file;
    }

    public void setFile(File file){
        this.file = file;
    }

    public File getFile(File file){
        return file;
    }

    public void add(GeneralPath shape){
        geometry.add(shape);
    }

    public void setSpindleSpeed(int SpindleSpeed){
        this.SpindleSpeed = SpindleSpeed;
    }

    public int getSpindleSpeed(){
        return SpindleSpeed;
    }
}
