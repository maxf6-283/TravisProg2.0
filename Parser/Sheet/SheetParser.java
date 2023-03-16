package Parser.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import SheetHandler.Hole;
import SheetHandler.Part;
import SheetHandler.Cut;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

public class SheetParser {
    public static HashMap<String, String> parseSheetFile(File sheetFile) {
        try {
            Scanner reader = new Scanner(sheetFile);
            HashMap<String, String> decodedFile = new HashMap<>();
            for (String attribute : reader.nextLine().split(",")) {
                decodedFile.put(
                        attribute.substring(attribute.indexOf('"') + 1,
                                attribute.substring(attribute.indexOf('"') + 1).indexOf('"')),
                        attribute.substring(attribute.substring(0, attribute.lastIndexOf('"') - 1).lastIndexOf('"') + 1,
                                attribute.lastIndexOf('"') - 1));
            }
            reader.close();
            return decodedFile;
        } catch (Exception e) {
            System.err.println("Problem finding sheet!");
            e.printStackTrace();
            return new HashMap<String, String>();
        }
    }

    private static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    private static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static void parseCutFile(File cutFile, Cut cut) {
            try {
                FileInputStream reader = new FileInputStream(cutFile);
                //get something or other to do with holes
                int something = reader.read();
                //get number of parts
                int partCount = reader.read();
                // x, y, rot are in sets of 64 bits
                byte[] nextNumber = new byte[8];
                for(int part = 0; part < partCount; part++) {
                    reader.readNBytes(nextNumber, 0, 8);
                    double partX = toDouble(nextNumber);
                    reader.readNBytes(nextNumber, 0, 8);
                    double partY = toDouble(nextNumber);
                    reader.readNBytes(nextNumber, 0, 8);
                    double partRot = toDouble(nextNumber);
                    int fileNameLength = reader.read() << 8 | reader.read();
                    String partFileName = "";
                    for(int character = 0; character < fileNameLength; character ++) {
                        partFileName += reader.read();
                    }
                    File partFile = new File(partFileName);
                    cut.parts.add(new Part(partFile, partX, partY, partRot));
                }
                
                //hole time
                //holes follow the pattern "double x, double y, empty double, byte with a 1 in it"
                //I don't know why and I hate it
                while(reader.available() != 0) {
                    reader.readNBytes(nextNumber, 0, 8);
                    double holeX = toDouble(nextNumber);
                    reader.readNBytes(nextNumber, 0, 8);
                    double holeY = toDouble(nextNumber);
                    cut.holes.add(new Hole(holeX, holeY));
                }

            } catch (Exception e) {
                
            }
        }
    

}
