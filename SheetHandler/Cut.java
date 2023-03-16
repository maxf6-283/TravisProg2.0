package SheetHandler;

import java.util.ArrayList;
import java.awt.Graphics;
import java.io.File;

public class Cut {
    public ArrayList<Part> parts;
    public ArrayList<Hole> holes;
    public File cutFile;

    public Cut(File cutFile) {
        this.cutFile = cutFile;
        
    }

    public void draw(Graphics g) {
        for(Part part : parts) {
            part.draw(g);
        }
        for(Hole hole : holes) {
            hole.draw(g);
        }
    }
}
