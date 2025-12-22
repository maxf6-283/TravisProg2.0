package Parser.GCode;

import Display.Screen;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class NGCDocument {
    private File gcodeFile;
    private ArrayList<RelativePath2D> originalGeometry;
    private Point3D currentPoint;
    private int SpindleSpeed;
    private double toolOffset = 0.1575;
    private double implicitGCodeHolder;
    private boolean isRelativeArc = true;
    private boolean isRelative = false;
    private int currentAxisPlane = 0;
    private double lastI = 0;
    private double lastJ = 0;
    private HashMap<String, Double> previousAttributes = new HashMap<>();
    private boolean machineCoordinates = false;
    private StringBuilder gCodeStringBuilder;
    private boolean usingCutterComp = false; // TODO make sure to set true when G41 or G42 is called
    private boolean inchesMode = true;
    private HashMap<Double, ArrayList<RelativePath2D>> offsetGeometry = new HashMap<>();
    private NgcStrain ngcStrain;

    public NGCDocument() {
        this(null, null);
    }

    public void setUsingCutterComp() {
        usingCutterComp = true;
    }

    public int getCurrentAxisPlane() {
        if (currentAxisPlane < 17 || currentAxisPlane > 19) {
            throw new IllegalGCodeError(
                    "Axis Plane "
                            + currentAxisPlane
                            + " is not supported or needs to be called before an arc");
        }
        return currentAxisPlane;
    }

    public void setCurrentAxisPlane(int GCode) {
        if (GCode < 17 || GCode > 19) {
            throw new IllegalGCodeError(
                    "Axis Plane " + GCode + " is not supported or needs to be called before an arc");
        }
        currentAxisPlane = GCode;
    }

    public void setToolOffset(Double num) {
        toolOffset = num;
        if (Screen.DebugMode)
            System.out.println("new dia: " + toolOffset);
    }

    public void setCurrentPoint(Point3D point) {
        currentPoint = point;
    }

    public Point3D getCurrentPointr() {
        return currentPoint;
    }

    public void setGCodeHolder(double GCode) {
        implicitGCodeHolder = GCode;
    }

    public double getGCodeHolder() {
        return implicitGCodeHolder;
    }

    public boolean contains(Point2D point) {
        for (RelativePath2D path : originalGeometry) {
            if (path.contains(point)) {
                return true;
            }
        }
        return false;
    }

    public void setIsRelative(boolean isRelative) {
        this.isRelative = isRelative;
    }

    public boolean getRelativity() {
        return isRelative;
    }

    public void setIsRelativeArc(boolean isRelativeArc) {
        this.isRelativeArc = isRelativeArc;
    }

    public boolean getRelativityArc() {
        return isRelativeArc;
    }

    public NGCDocument(File file, NgcStrain ngcStrain) {
        this.gcodeFile = file;
        gCodeStringBuilder = new StringBuilder();
        originalGeometry = new ArrayList<>();
        originalGeometry.add(new RelativePath2D());
        this.ngcStrain = ngcStrain;
    }

    public ArrayList<RelativePath2D> getRelativePath2Ds() {
        if (usingCutterComp) {
            ArrayList<RelativePath2D> path = offsetGeometry.get(toolOffset);
            if (path == null) {
                path = getOffsetInstance();
                offsetGeometry.put(toolOffset, path);
            }
            return path;
        } else {
            // return getOffsetInstance();
            if (Screen.DebugMode)
                System.out.println(originalGeometry);
            return originalGeometry;
        }
    }

    private ArrayList<RelativePath2D> getOffsetInstance() {
        ArrayList<RelativePath2D> output = new ArrayList<>();

        for (RelativePath2D path : originalGeometry) {
            output.add(path.getOffsetInstance2(toolOffset));
        }

        return output;
    }

    protected RelativePath2D getCurrentPath2D() {
        return originalGeometry.get(originalGeometry.size() - 1);
    }

    protected void newPath2D() {
        originalGeometry.add(new RelativePath2D());
    }

    public void setGcodeFile(File file) {
        this.gcodeFile = file;
    }

    public File getGcodeFile() {
        return gcodeFile;
    }

    public void setSpindleSpeed(int SpindleSpeed) {
        this.SpindleSpeed = SpindleSpeed;
    }

    public int getSpindleSpeed() {
        return SpindleSpeed;
    }

    public void setLastArcCenter(double i, double j) {
        lastI = i;
        lastJ = j;
    }

    public double lastI() {
        return lastI;
    }

    public double lastJ() {
        return lastJ;
    }

    public void addGCodeAttributes(HashMap<String, Double> attributes) {
        ngcStrain.gCodeParser.addGCodeAttributes(attributes, this);
    }

    public void setUsingMachineCoordinates(boolean b) {
        machineCoordinates = b;
    }

    public boolean usingMachineCoordinates() {
        return machineCoordinates;
    }

    public double getToolOffset() {
        return toolOffset;
    }

    public void addToString(String lineToAdd) {
        gCodeStringBuilder.append("\n" + lineToAdd);
    }

    public String getGCodeString() {
        return gCodeStringBuilder.toString();
    }

    public NgcStrain getNgcStrain() {
        return ngcStrain;
    }

    public HashMap<String, Double> getPreviousAttributes() {
        return previousAttributes;
    }

    public boolean isInchesMode() {
        return inchesMode;
    }

    protected void setInchesMode(boolean newMode) {
        inchesMode = newMode;
    }

    protected void setPreviousAttributes(HashMap<String, Double> newAttributes) {
        previousAttributes = newAttributes;
    }

    public String getGCodeHeader() {
        return ngcStrain.gCodeParser.getGCodeHeader(this);
    }

    public String getGCodeBody() {
        return ngcStrain.gCodeParser.getGCodeBody(this);
    }

    public String getGCodeFooter() {
        return ngcStrain.gCodeParser.getGCodeFooter(this);
    }
}
