package Parser.GCode;

import Display.Screen;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.geom.Point2D;
import java.io.File;

public class NGCDocument {
    private File file;
    private ArrayList<RelativePath2D> geometry;
    private Point3D currentPoint;
    private int SpindleSpeed;
    private double ToolOffset = 0.1575;// TODO finsih thishoibqwob4eiurboewqb
    private double implicitGCodeHolder;
    private boolean isRelativeArc;
    private boolean isRelative = false;
    private int currentAxisPlane = 0;
    private double lastI = 0;
    private double lastJ = 0;
    private HashMap<String, Double> previousAttributes = new HashMap<>();
    private boolean machineCoordinates;

    public NGCDocument() {
        this(null);
    }

    public int getCurrentAxisPlane() {
        if (currentAxisPlane < 17 || currentAxisPlane > 19) {
            throw new IllegalGCodeError(
                    "Axis Plane " + currentAxisPlane + " is not supported or needs to be called before an arc");
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
        for (RelativePath2D path : geometry) {
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

    public NGCDocument(File file) {
        this.file = file;
        geometry = new ArrayList<>();
        geometry.add(new RelativePath2D());
    }

    public ArrayList<RelativePath2D> getRelativePath2Ds() {
        return geometry;
    }

    public RelativePath2D getCurrentPath2D() {
        return geometry.get(geometry.size() - 1);
    }

    public void newPath2D() {
        geometry.add(new RelativePath2D());
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
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
        // set g to the last thing if theres no g
        if (!attributes.containsKey("G")) {
            attributes.put("G", previousAttributes.get("G"));
        }

        //if it's a movement, ignore it if machine coords
        if(usingMachineCoordinates() && attributes.get("G") >= 0 && attributes.get("G") <= 3) {
            setUsingMachineCoordinates(false);
            return;
        }
        if (!attributes.containsKey("X")) {
            attributes.put("X", getRelativity() ? 0 : previousAttributes.getOrDefault("X", 0.0));
        }
        if (!attributes.containsKey("Y")) {
            attributes.put("Y", getRelativity() ? 0 : previousAttributes.getOrDefault("Y", 0.0));
        }
        if (!attributes.containsKey("Z")) {
            attributes.put("Z", getRelativity() ? 0 : previousAttributes.getOrDefault("Z", 0.0));
        }
        // Note: I and J are only modal on some router contollers
        if (!attributes.containsKey("I")) {
            attributes.put("I", getRelativityArc() ? 0 : previousAttributes.getOrDefault("Y", 0.0));
        }
        if (!attributes.containsKey("J")) {
            attributes.put("J", getRelativityArc() ? 0 : previousAttributes.getOrDefault("Z", 0.0));
        }
        switch ((int) attributes.get("G").doubleValue()) {
            case 0 -> {
                if (getRelativity()) {
                    if (attributes.get("Z") + getCurrentPointr().getZ() > 0) {
                        newPath2D();
                    }
                    getCurrentPath2D().moveToRelative(attributes.get("X"), -attributes.get("Y"));
                    getCurrentPath2D().setZRelative(attributes.get("Z"));
                } else {
                    if (attributes.get("Z") > 0) {
                        newPath2D();
                    }
                    getCurrentPath2D().moveTo(attributes.get("X"), -attributes.get("Y"));
                    getCurrentPath2D().setZ(attributes.get("Z"));
                    getCurrentPath2D().setRelative(attributes.get("X"), -attributes.get("Y"));
                }
            }
            case 1 -> {
                if (getRelativity()) {
                    getCurrentPath2D().lineToRelative(attributes.get("X"), -attributes.get("Y"));
                    getCurrentPath2D().setZRelative(attributes.get("Z"));
                } else {
                    getCurrentPath2D().lineTo(attributes.get("X"), -attributes.get("Y"));
                    getCurrentPath2D().setZ(attributes.get("Z"));
                    getCurrentPath2D().setRelative(attributes.get("X"), -attributes.get("Y"));
                }
            }
            case 2, 3 -> {
                if (getCurrentAxisPlane() == 17) {
                    double x = attributes.get("X");
                    double y = -attributes.get("Y");
                    double i = attributes.get("I");
                    double j = -attributes.get("J");
                    getCurrentPath2D().arcTo(i, j, x, y, attributes.get("G") == 2 ? -1 : 1, getRelativity(),
                            getRelativityArc());
                } else {
                    if (getRelativity()) {
                        getCurrentPath2D().lineToRelative(attributes.get("X"), -attributes.get("Y"));
                        getCurrentPath2D().setZRelative(attributes.get("Z"));
                    } else {
                        getCurrentPath2D().lineTo(attributes.get("X"), -attributes.get("Y"));
                        getCurrentPath2D().setZ(attributes.get("Z"));
                        getCurrentPath2D().setRelative(attributes.get("X"), -attributes.get("Y"));
                    }
                }
                if (getRelativity()) {
                    getCurrentPath2D().setZRelative(attributes.get("Z"));
                } else {
                    getCurrentPath2D().setZ(attributes.get("Z"));
                }
            }
            case 4 -> {
                // dwell aka do nothing
            }
            case 10 -> {
                // WCS Offset Select
            }
            case 17, 18, 19 -> {
                setCurrentAxisPlane((int) attributes.get("G").doubleValue());// sets axis planes
            }
            case 20 -> {
                // do Nothing(inches mode)
            }
            case 21 -> {
                // TODO automatically fix
                throw new IllegalGCodeError(
                        "Metric Units not allowed in the world of imperial allens and wrenches");
            }
            case 43 -> {
                // calls which tool offset is used(TODO fix complexities)
            }
            case 53 -> {
                // Move In Machine Coordinates - ignore next move command
                setUsingMachineCoordinates(true);
            }
            case 54, 55, 56, 57, 58, 59 -> {
                // WCS Offset(Do nothing for NOW TODO fix this)
            }
            case 64 -> {
                // do Nothing(Path Blending??!!??)
            }
            case 90 -> {
                if (attributes.get("G") == 90) {
                    // absolute distance mode
                    setIsRelative(false);
                } else if(attributes.get("G") == 90.1) {
                    // absolute arc mode
                    setIsRelativeArc(false);
                } else {
                    throw new UnknownGCodeError("Attributes " + attributes + "Not accepted GCode");
                }
            }
            case 91 -> {
                if (attributes.get("G") == 91) {
                    // incremental distance mode
                    setIsRelative(true);
                } else if(attributes.get("G") == 91.1) {
                    setIsRelativeArc(true);
                } else {
                    throw new UnknownGCodeError("Attributes " + attributes + "Not accepted GCode");
                }
            }
            case 94 -> {
                // do Nothing(Feed rate change)
            }
            default -> {
                throw new UnknownGCodeError("Attributes " + attributes + "Not accepted GCode");
            }

        }
        if (Screen.DebugMode == true) {
            System.out.println(attributes);
        }

        previousAttributes = attributes;
    }

    public void setUsingMachineCoordinates(boolean b) {
        machineCoordinates = b;
    }

    public boolean usingMachineCoordinates() {
        return machineCoordinates;
    }
}
