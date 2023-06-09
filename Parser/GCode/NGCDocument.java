package Parser.GCode;

import Display.Screen;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.geom.Point2D;
import java.io.File;

public class NGCDocument {
    private File gcodeFile;
    private ArrayList<RelativePath2D> originalGeometry;
    private Point3D currentPoint;
    private int SpindleSpeed;
    private double toolOffset = 0.1575;
    private double implicitGCodeHolder;
    private boolean isRelativeArc;
    private boolean isRelative = false;
    private int currentAxisPlane = 0;
    private double lastI = 0;
    private double lastJ = 0;
    private HashMap<String, Double> previousAttributes = new HashMap<>();
    private boolean machineCoordinates;
    private StringBuilder gCodeStringBuilder;
    private boolean usingCutterComp = false;// TODO make sure to set true when G41 or G42 is called
    private boolean inchesMode = true;
    private HashMap<Double, ArrayList<RelativePath2D>> offsetGeometry = new HashMap<>();

    public NGCDocument() {
        this(null);
    }

    public void setUsingCutterComp() {
        usingCutterComp = true;
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

    public void setToolOffset(Double num) {
        toolOffset = num;
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

    public NGCDocument(File file) {
        this.gcodeFile = file;
        gCodeStringBuilder = new StringBuilder();
        originalGeometry = new ArrayList<>();
        originalGeometry.add(new RelativePath2D());
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
            return getOffsetInstance();
        }
    }

    private ArrayList<RelativePath2D> getOffsetInstance() {
        ArrayList<RelativePath2D> output = new ArrayList<>();

        for(RelativePath2D path : originalGeometry) {
            output.add(path.getOffsetInstance2(toolOffset));
        }

        return output;
    }

    private RelativePath2D getCurrentPath2D() {
        return originalGeometry.get(originalGeometry.size() - 1);
    }

    private void newPath2D() {
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
        // set g to the last thing if theres no g
        if (!attributes.containsKey("G")) {
            attributes.put("G", previousAttributes.get("G"));
        }

        // if not in inches, modify distance values
        if (!inchesMode) {
            if (attributes.containsKey("X")) {
                attributes.put("X", attributes.get("X") / 25.4);
            }
            if (attributes.containsKey("Y")) {
                attributes.put("Y", attributes.get("Y") / 25.4);
            }
            if (attributes.containsKey("I")) {
                attributes.put("I", attributes.get("I") / 25.4);
            }
            if (attributes.containsKey("J")) {
                attributes.put("J", attributes.get("J") / 25.4);
            }
            if (attributes.containsKey("Z")) {
                attributes.put("Z", attributes.get("Z") / 25.4);
            }
        }

        // if it's a movement, ignore it if machine coords
        if (usingMachineCoordinates() && attributes.get("G") >= 0 && attributes.get("G") <= 3) {
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
        // Note: I and J are only modal on some router contollers. This code is likely
        // not necessary
        if (!attributes.containsKey("I")) {
            attributes.put("I", getRelativityArc() ? 0 : previousAttributes.getOrDefault("I", 0.0));
        }
        if (!attributes.containsKey("J")) {
            attributes.put("J", getRelativityArc() ? 0 : previousAttributes.getOrDefault("J", 0.0));
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
                inchesMode = true;
            }
            case 21 -> {
                inchesMode = false;
            }
            case 41 -> {
                //cutter comp left
                if(attributes.get("G") == 41) {
                    getCurrentPath2D().offsetLeft();
                } else {
                    throw new UnknownGCodeError("Attributes " + attributes + "Not accepted GCode");
                }
            }
            case 42 -> {
                if(attributes.get("G") == 42) {
                    getCurrentPath2D().offsetRight();
                } else {
                    throw new UnknownGCodeError("Attributes " + attributes + "Not accepted GCode");
                }
            }
            case 43 -> {
                // calls which tool length offset is used(TODO fix complexities)
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
            case 80 -> {
                // turn off canned cycle, does nothing???
            }
            case 81, 82, 83 -> {
                // canned cycles
                // just make a dot i give up
                getCurrentPath2D().moveTo(attributes.get("X"), -attributes.get("Y"));
                getCurrentPath2D().lineTo(attributes.get("X"), -attributes.get("Y"));
            }
            case 90 -> {
                if (attributes.get("G") == 90) {
                    // absolute distance mode
                    setIsRelative(false);
                } else if (attributes.get("G") == 90.1) {
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
                } else if (attributes.get("G") == 91.1) {
                    setIsRelativeArc(true);
                } else {
                    throw new UnknownGCodeError("Attributes " + attributes + "Not accepted GCode");
                }
            }
            case 94 -> {
                // do Nothing(Feed rate change)
            }
            case 98, 99 -> {
                // idk
            }
            default -> {
                throw new UnknownGCodeError("Attributes " + attributes + "Not accepted GCode");
            }

        }
        if (Screen.DebugMode) {
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

    public double getToolOffset() {
        return toolOffset;
    }

    public void addToString(String lineToAdd) {
        gCodeStringBuilder.append("\n" + lineToAdd);
    }

    public String getGCodeString() {
        return gCodeStringBuilder.toString();
    }

    public String getGCodeHeader() {
        String gCodeString = getGCodeString();
        int endIndex = gCodeString.indexOf("G53");
        while (gCodeString.charAt(endIndex) != '\n') {
            endIndex++;
        }
        String header = gCodeString.substring(gCodeString.indexOf('%') + 1, endIndex + 1);
        return "(START HEADER)\n" + header + "(END HEADER)\n";
    }

    public String getGCodeBody() {
        String gCodeString = getGCodeString();
        int endIndex = gCodeString.indexOf("G53");
        while (gCodeString.charAt(endIndex) != '\n') {
            endIndex++;
        }
        String body = gCodeString.substring(endIndex + 1, gCodeString.lastIndexOf("G53"));
        return "(START BODY)\n" + body + "(END BODY)\n";
    }

    public String getGCodeFooter() {
        String gCodeString = getGCodeString();
        String footer = gCodeString.substring(gCodeString.lastIndexOf("G53"), gCodeString.lastIndexOf('%'));
        return "(START FOOTER)\n" + footer + "(END FOOTER)\n";
    }
}
