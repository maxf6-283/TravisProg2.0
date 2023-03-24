package Parser.Sheet;

import java.util.Arrays;
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

                String key = attribute.substring(0,attribute.indexOf(':'));
                key = key.replace("\"", "");
                key = key.replace("{", "");

                String value = attribute.substring(attribute.indexOf(':')+1);
                value = value.replace("\"", "");
                value = value.replace("}", "");
                
                decodedFile.put(key, value);
            }
            reader.close();
            return decodedFile;
        } catch (IOException e) {
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
            reader.read();
            // get number of parts
            int partCount = reader.read();
            System.out.printf("Parsing %s: %d parts detected.%n", cutFile.getName(), partCount);
            // x, y, rot are in sets of 64 bits
            byte[] nextNumber = new byte[8];
            for (int part = 0; part < partCount; part++) {
                System.out.println("Part read:" + reader.readNBytes(nextNumber, 0, 8) +": " + Arrays.toString(nextNumber));
                double partX = toDouble(nextNumber);
                System.out.printf("Part %d's x is %f%n", part, partX);
                System.out.println("Part read:" + reader.readNBytes(nextNumber, 0, 8) +": " + Arrays.toString(nextNumber));
                double partY = toDouble(nextNumber);
                System.out.printf("Part %d's y is %f%n", part, partY);
                System.out.println("Part read:" + reader.readNBytes(nextNumber, 0, 8) +": " + Arrays.toString(nextNumber));
                double partRot = toDouble(nextNumber);
                System.out.printf("Part %d's r is %f%n", part, partRot);
                //its in little endian and I hate it
                if(reader.read()==1){
                    //is a hole
                    cut.parts.add(new Hole(partX, partY,partRot));
                    System.out.println("Adding a hole");
                    part-=1;
                }else{
                    //is a part
                    int fileNameLength = reader.read();
                    if(fileNameLength == -1){
                        break;//end of file
                    }
                    System.out.println("File name length: " + fileNameLength);
                    String partFileName = "";
                    for (int character = 0; character < fileNameLength; character++) {
                        partFileName += (char)reader.read();
                    }
                    File partFile = new File(partFileName);
                    cut.parts.add(new Part(partFile, partX, partY, partRot));
                }
            }

            /*
            // hole time
            // holes follow the pattern "double x, double y, empty double, byte with a 1 in
            // it"
            // I don't know why and I hate it
            while (reader.available() != 0) {
                reader.readNBytes(nextNumber, 0, 8);
                double holeX = toDouble(nextNumber);
                reader.readNBytes(nextNumber, 0, 8);
                double holeY = toDouble(nextNumber);
                cut.parts.add(new Hole(holeX, holeY));
                reader.readNBytes(9);
            }*/
            reader.close();

        } catch (Exception e) {
            System.err.println("Cut file is wonky!\n\n");

            e.printStackTrace();

            System.exit(1);
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
                if(part instanceof Hole){
                    writer.write(1);//for holes
                    continue;
                }

                String filePath = part.partFile().getPath();
                writer.write(filePath.length()%256);
                writer.write(filePath.length()>>8);

                for(int i = 0; i < filePath.length(); i++) {
                    writer.write(filePath.charAt(i));
                }
            }

            writer.close();

        } catch(IOException e) {
            System.out.println("Cannot save cut file???\n\n");
        }
    }

}
