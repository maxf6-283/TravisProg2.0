package Parser;

import java.util.ArrayList;
import java.awt.Shape;
import java.io.File;

public class NGCDocument {
    private File file;
    private ArrayList<Shape> geometry;
    private int SpindleSpeed;
    private double ToolOffset = 0.1575;
    private double[] currentWCSOffset = new double[4];

    public void addToCurrentWCSOffset(double[] addOffset){
        checkWCSOffset(addOffset);
        for(int i = 0; i < currentWCSOffset.length;i++){
            currentWCSOffset[i] += addOffset[i];
        }
    }

    public void setCurrentWCSOffset(double[] newOffset){
        checkWCSOffset(newOffset);
        currentWCSOffset = newOffset;
    }

    private void checkWCSOffset(double[] newOffset){
        if(newOffset.length != 6){
            throw new IllegalArgumentException("Offset needs to be 4 digits for each major axis(X,Y,I,J) instead of: "+newOffset);
        }
    }

    public double[] getCurrentWCSOffset(){
        return currentWCSOffset;
    }

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

    public void add(Shape shape){
        geometry.add(shape);
    }

    public void setSpindleSpeed(int SpindleSpeed){
        this.SpindleSpeed = SpindleSpeed;
    }

    public int getSpindleSpeed(){
        return SpindleSpeed;
    }
}
