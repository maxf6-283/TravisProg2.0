package SheetHandler;

import java.awt.Graphics;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.File;

public class Sheet {
    private ArrayList<Part> parts;
    private double width, height; //in inches
    private String filePath;

    public Sheet(String filePath) {
        this.filePath = filePath;
        //TODO: add sheet storage
    }

    public void addPart(Part part) {
        parts.add(part);
        part.setParentSheet(this);
    }

    public void draw(Graphics g) {
        //TODO: add rectangle for sheet
        for(Part part : parts) {
            part.draw(g);
        }
    }

    public void saveToFile() {
        //TODO: add sheet saving to file
    }
}
