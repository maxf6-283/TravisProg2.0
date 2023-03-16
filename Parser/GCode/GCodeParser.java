package Parser.GCode;

public class GCodeParser {
    public static void parse(String gcodeLine, int lineNum, NGCDocument doc) {
        int code;
        double codeDouble;
        String tempCode = "";
        int indexOfG = gcodeLine.indexOf('G') + 1;
        while (!(indexOfG >= gcodeLine.length() || !(gcodeLine.charAt(indexOfG) != ' '))) {
            tempCode += gcodeLine.charAt(indexOfG);
            indexOfG++;
        }
        if (tempCode.contains(".")) {
            codeDouble = Double.parseDouble(tempCode);
            if (codeDouble == 91.1) {

            } else{
                throw new UnknownGCodeError("GCode : " + tempCode + " not parsable @ line: " + lineNum);
            }
        } else {
            code = Integer.parseInt(tempCode);
            switch (code) {
                case 0 -> {
                } // rapid move (do Nothing)
                case 1 -> {

                } // linear move
                default -> {
                    throw new UnknownGCodeError("GCode : " + tempCode + " not parsable @ line: " + lineNum);
                }
            }
        }
        gcodeLine = gcodeLine.substring(0, indexOfG - tempCode.length() - 1)
                + gcodeLine.substring(indexOfG, gcodeLine.length());
        gcodeLine.trim();
        if (gcodeLine.contains("G")) {
            GCodeParser.parse(gcodeLine, lineNum, doc);
        }
    }

    public static void parseImplicit(String gcodeLine, int lineNum, NGCDocument doc) {

    }
}
