package Parser.GCode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class Parser {
    static BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

    public static void main(String[] args) {
        NGCDocument doc = new NGCDocument();
        long time = -System.nanoTime();
        try {
            doc = parse(new File("TestFiles/butterfly!!!!_1565.ngc"));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
        System.out.println((System.nanoTime() + time) / 1000.0 / 1000.0 / 1000.0);
    }

    /**
     * 
     * @param fileInput
     * @return
     * @throws IOException NGC file not found, try another or use a dxf
     */
    public static NGCDocument parse(File fileInput) throws IOException {
        int lineNum = 1;
        Scanner input;
        input = new Scanner(fileInput);
        NGCDocument doc = new NGCDocument(fileInput);
        while (input.hasNextLine()) {
            String line = input.nextLine();
            lineNum++;
            out.write(line + "\n");
            if (line.contains("(")) {
                line = CommentsParser.parse(line, doc);
            }
            line.trim();
            if (line.contains("M")) {
                MCodeParser.parse(line, lineNum, doc);
            } else if (line.contains("G")) {
                GCodeParser.parse(line);
            } else if (line.length() > 0) {
                GCodeParser.parseImplicit(line, doc);
            }
        }
        input.close();
        return doc;
    }
}