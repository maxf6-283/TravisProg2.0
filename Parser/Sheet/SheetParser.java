package Parser.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import SheetHandler.Hole;
import SheetHandler.Part;
import SheetHandler.Cut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
        //I hate it its in little endian
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).putDouble(value);
        return bytes;
    }

    private static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    public static void parseCutFile(File cutFile, Cut cut) {
        try {
            FileInputStream reader = new FileInputStream(cutFile);
            // get something or other to do with holes
            int something = reader.read();
            // get number of parts
            int partCount = reader.read();
            // x, y, rot are in sets of 64 bits
            byte[] nextNumber = new byte[8];
            for (int part = 0; part < partCount; part++) {
                reader.readNBytes(nextNumber, 0, 8);
                double partX = toDouble(nextNumber);
                reader.readNBytes(nextNumber, 0, 8);
                double partY = toDouble(nextNumber);
                reader.readNBytes(nextNumber, 0, 8);
                double partRot = toDouble(nextNumber);
                //its in little endian and I hate it
                int fileNameLength = reader.read() | reader.read() << 8;
                String partFileName = "";
                for (int character = 0; character < fileNameLength; character++) {
                    partFileName += reader.read();
                }
                File partFile = new File(partFileName);
                cut.parts.add(new Part(partFile, partX, partY, partRot));
            }

            // hole time
            // holes follow the pattern "double x, double y, empty double, byte with a 1 in
            // it"
            // I don't know why and I hate it
            while (reader.available() != 0) {
                reader.readNBytes(nextNumber, 0, 8);
                double holeX = toDouble(nextNumber);
                reader.readNBytes(nextNumber, 0, 8);
                double holeY = toDouble(nextNumber);
                cut.holes.add(new Hole(holeX, holeY));
                reader.readNBytes(9);
            }
            reader.close();

        } catch (Exception e) {
            System.err.println("Cut file is wonky!\n\n");

            e.printStackTrace();
        }
    }

    public static void saveSheetInfo(File sheetFile, HashMap<String, String> sheetInfo) {
        String information = sheetInfo.toString().replace(" ", "");
        try {
            FileWriter writer = new FileWriter(sheetFile);
            writer.write(information);
            writer.close();
        } catch (IOException e) {
            System.err.println("Cannot save sheet file???\n\n");
            e.printStackTrace();
        }
    }

    public static void saveCutInfo(Cut cut) {
        FileOutputStream writer;
        try {
            writer = new FileOutputStream(cut.cutFile());
            //TODO: figure out what the first byte indicates
            writer.write(255);
            writer.write(cut.parts.size());

            //write the parts
            for(Part part : cut.parts) {
                writer.write(toByteArray(part.getX()));
                writer.write(toByteArray(part.getY()));
                writer.write(toByteArray(part.getRot()));

                String filePath = part.partFile().getPath();
                writer.write(filePath.length()%256);
                writer.write(filePath.length()>>8);

                for(int i = 0; i < filePath.length(); i++) {
                    writer.write(filePath.charAt(i));
                }
            }

            //write the holes
            for(Hole hole : cut.holes) {
                writer.write(toByteArray(hole.x));
                writer.write(toByteArray(hole.y));
                writer.write(toByteArray(0));
                writer.write(1);
            }

            writer.close();

        } catch(IOException e) {
            System.out.println("Cannot save cut file???\n\n");
        }
    }

}
