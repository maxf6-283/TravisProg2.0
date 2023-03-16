package SheetHandler;

import java.util.ArrayList;
import java.awt.Graphics;
import java.io.File;

public class Cut {
    public ArrayList<Part> parts;
    public ArrayList<Hole> holes;
    private File cutFile;

    public Cut(File cutFile) {
        this.cutFile = cutFile;

        parts = new ArrayList<>();
        holes = new ArrayList<>();
    }

    public File cutFile() {
        return cutFile;
    }

    /**
     * Draw the parts and holes in the cut
     */
    public void draw(Graphics g) {
        for(Part part : parts) {
            part.draw(g);
        }
        for(Hole hole : holes) {
            hole.draw(g);
        }
    }
}
