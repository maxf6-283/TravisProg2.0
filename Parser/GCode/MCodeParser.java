package Parser.GCode;

public class MCodeParser {
    public static void parse(String gcodeLine, int lineNum, NGCDocument doc) {
        int code;
        String tempCode = "";
        int indexOfM = gcodeLine.indexOf('M') + 1;
        while (!(indexOfM >= gcodeLine.length() || !(gcodeLine.charAt(indexOfM) != ' '))) {
            tempCode += gcodeLine.charAt(indexOfM);
            indexOfM++;
        }
        code = Integer.parseInt(tempCode);
        tempCode = null;

        switch (code) {
            case 3, 4, 5 -> {
                int indexOfS = gcodeLine.indexOf('S') + 1;
                String speed = "";
                while (!(indexOfS >= gcodeLine.length() || !(gcodeLine.charAt(indexOfS) != ' '))) {
                    speed += gcodeLine.charAt(indexOfS);
                    indexOfS++;
                }
                doc.setSpindleSpeed(Integer.parseInt(speed));
            } // spindle speed
            case 0, 1, 2, 30, 7, 9, 6 -> {
            } // program pause)0 or 1), program end(2 or 30), mist or turned off(7 or 9), tool
              // change
            case 8 -> throw new IllegalGCodeError("Flood Coolant is not supported @ line: " + lineNum);
            default -> throw new UnknownGCodeError("MCode : " + tempCode + " not parsable @ line: " + lineNum);
        }

    }
}