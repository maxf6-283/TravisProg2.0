package Parser.GCode;

public class EndParserWinCNC implements GenericParser {
    public String parse(String gcodeLine, int lineNum, NGCDocument doc) {
        if (gcodeLine.contains("T")) { // tool changer
            // TODO: set tools to part of gcode
            int index = gcodeLine.indexOf('T');
            int tool = Integer.parseInt(gcodeLine.substring(index + 1, index + 2));
            gcodeLine = gcodeLine.substring(gcodeLine.indexOf('T') + 2);
        }
        if (gcodeLine.contains("S")) {
            int indexOfS = gcodeLine.indexOf('S') + 1;
            String speed = "";
            while (!(indexOfS >= gcodeLine.length() || !(gcodeLine.charAt(indexOfS) != ' '))) {
                speed += gcodeLine.charAt(indexOfS);
                indexOfS++;
            }
            doc.setSpindleSpeed(Integer.parseInt(speed));
            gcodeLine = gcodeLine.replace("S" + speed, "");
        }
        if (!gcodeLine.isEmpty()) {
            throw new IllegalArgumentException(
                    "The following Gcode line cannot be parsed, line: " + lineNum + "\n" + gcodeLine);
        }
        return gcodeLine;
    }
}
