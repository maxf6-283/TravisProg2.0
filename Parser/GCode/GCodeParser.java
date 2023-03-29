package Parser.GCode;

import java.util.HashMap;

public class GCodeParser {
    public static void parse(String gcodeLine, int lineNum, NGCDocument doc) {
        // split the line into multiple and parse all of them if it has >1 g
        if (gcodeLine.lastIndexOf('G') != gcodeLine.indexOf('G')) {
            String[] gcodeLines = gcodeLine.trim().split("G");
            for (String line : gcodeLines) {
                if (line.length() > 1) {
                    parse("G" + line, lineNum, doc);
                }
            }
        } else {
            HashMap<String, Double> attributes = new HashMap<>();
            for (String datum : gcodeLine.trim().split(" ")) {
                try {
                    attributes.put(datum.substring(0, 1), Double.parseDouble(datum.substring(1)));
                } catch (Exception r) {
                    System.out.println(lineNum + ": " + gcodeLine);
                    System.exit(lineNum);
                }
            }

            doc.addGCodeAttributes(attributes);
        }
    }
}
