package SheetHandler;

import java.awt.Graphics;
import java.awt.Graphics2D;

public class Part {
    private double sheetX, sheetY, rotation; //x and y in inches, rotation in radians
    private Sheet parentSheet;

    public Part(String filePath) {
        //TODO: get part from file
    }

    protected void setParentSheet(Sheet sheet) {
        parentSheet = sheet;
    }    

    public Sheet parentSheet() {
        return 
        parentSheet;
    }

    public void draw(Graphics g) {
        //TODO: add sheet drawing
    }
}
