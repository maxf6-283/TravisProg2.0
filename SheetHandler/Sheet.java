package SheetHandler;

import java.util.ArrayList;

public class Sheet {
    private ArrayList<Part> parts;

    public Sheet(String filePath) {
        //TODO: add sheet storage
    }

    public void addPart(Part part) {
        parts.add(part);
        part.setParentSheet(this);
    }
}
