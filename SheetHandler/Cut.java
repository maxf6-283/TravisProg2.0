package SheetHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

import Parser.Sheet.SheetParser;

import java.awt.Graphics;
import java.io.File;

/**
 * Stores the cutFile and holeFile as well as an arraylist of all the parts
 * Also, essentially wraps the Arraylist to make the cut iterable for each part
 */
public class Cut implements Iterable<Part> {
    public ArrayList<Part> parts;
    private File cutFile;
    private File holeFile;

    public File getHoleFile() {
        return holeFile;
    }

    public Cut(File cutFile, File holeFile) {
        this.holeFile = holeFile;
        this.cutFile = cutFile;

        parts = new ArrayList<>();

        if (!cutFile.exists()) {
            /*
             * try {
             * if (!cutFile.createNewFile()) {
             * new ErrorDialog(new IOException("This Cut file already exists"));
             * }
             * } catch (IOException e) {
             * new ErrorDialog(e);
             * }
             */
            return;
        }

        SheetParser.parseCutFile(cutFile, this);
    }

    public Stream<Part> stream() {
        return parts.stream();
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

    public File getCutFile() {
        return cutFile;
    }

    /**
     * Draw the parts and holes in the cut
     */
    public void draw(Graphics g) {
        for (Part part : parts) {
            part.draw(g);
        }
    }
}
