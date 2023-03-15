package SheetHandler;

import java.awt.Graphics;
import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;
import java.util.HashMap;


public class Sheet {
    private ArrayList<Part> parts;
    private double width, height; //in inches
    private File sheetFile, holeFile, activeCutFile;

    public Sheet(File sheetFile) {
        this.sheetFile = sheetFile;
        try {
            Scanner reader = new Scanner(sheetFile);
            HashMap<String, String> decodedFile = new HashMap<>();
            for(String attribute : reader.nextLine().split(",")) {
                decodedFile.put(attribute.substring(attribute.indexOf('"')+1, attribute.substring(attribute.indexOf('"')+1).indexOf('"')), attribute.substring(attribute.substring(0, attribute.lastIndexOf('"')-1).lastIndexOf('"')+1, attribute.lastIndexOf('"')-1));
            }
            width = Double.parseDouble(decodedFile.get("w"));
            height = Double.parseDouble(decodedFile.get("h"));
            holeFile = new File(decodedFile.get("hole_file"));
            activeCutFile = new File(decodedFile.get("active"));
        } catch (Exception e) {
            System.err.println("Sheet file not found!\n\n");
            e.printStackTrace();
        }

        //time to get the parts
        File parentFile = sheetFile.getParentFile();
        for(File cutFile : parentFile.listFiles()) {
            if(!cutFile.getName().endsWith(".cut")) {
                continue;
            }
            
        }
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
