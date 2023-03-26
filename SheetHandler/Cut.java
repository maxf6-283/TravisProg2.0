package SheetHandler;

import java.util.ArrayList;
import java.util.Iterator;

import Parser.Sheet.SheetParser;

import java.awt.Graphics;
import java.io.File;

public class Cut implements Iterable<Part>{
    public ArrayList<Part> parts;
    private File cutFile;

    public Cut(File cutFile) {
        this.cutFile = cutFile;

        parts = new ArrayList<>();

        SheetParser.parseCutFile(cutFile, this);
    }

    @Override
    public Iterator<Part> iterator() {
        Iterator<Part> it = new Iterator<Part>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < parts.size();
            }

            @Override
            public Part next() {
                return parts.get(currentIndex++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;
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
            break;
        }
    }
}
