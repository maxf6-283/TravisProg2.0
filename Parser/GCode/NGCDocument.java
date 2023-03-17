package Parser.GCode;

import java.util.ArrayList;
import java.io.File;
import java.awt.geom.Path2D;

public class NGCDocument {
    private File file;
    private ArrayList<Path2D.Double> geometry;
    private int SpindleSpeed;
    private double ToolOffset = 0.1575;
    protected static final int INITIAL = 0;
    protected static final int CUTTING = 1;
    protected static final int RAPID = 2;
    protected static final int DONE = 3;
    private int implicitGCodeHolder;
    private int state = INITIAL;

    public NGCDocument() {
        this(null);
    }

    public NGCDocument(File file) {
        this.file = file;
        geometry = new ArrayList<>();
        geometry.add(new Path2D.Double());
    }

    public Path2D.Double getCurrentPath2D() {
        return geometry.get(geometry.size() - 1);
    }

    /**
     * 
     * @param state can be either {@link NGCDocument#INITIAL},
     *              {@link NGCDocument#CUTTING}, {@link NGCDocument#RAPID}, or
     *              {@link NGCDocument#DONE}
     */
    public void updateState(int state) {
        this.state = state;
        switch (state) {
            case 0, 1, 3 -> {
            }
            case 2 -> {
                geometry.add(new Path2D.Double());
            }
        }
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
