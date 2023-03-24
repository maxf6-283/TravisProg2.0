package Parser.GCode;

public class GCodeParser {
    public static void parse(String gcodeLine, int lineNum, NGCDocument doc) {
        int code;
        double codeDouble;
        String tempCode = "";
        double x, y, z, i, j, k;
        x = y = z = i = j = k = 0;
        int indexOfG = gcodeLine.indexOf('G') + 1;
        while (!(indexOfG >= gcodeLine.length() || !(gcodeLine.charAt(indexOfG) != ' '))) {
            tempCode += gcodeLine.charAt(indexOfG);
            indexOfG++;
        }
        if (gcodeLine.contains("X")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("X") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(indexOfG);
                index++;
            }
            x = Double.parseDouble(tempAxis);
        }
        if (gcodeLine.contains("Y")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("Y") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(indexOfG);
                index++;
            }
            y = Double.parseDouble(tempAxis);
        }
        if (gcodeLine.contains("Z")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("Z") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(indexOfG);
                index++;
            }
            z = Double.parseDouble(tempAxis);
        }
        if (gcodeLine.contains("I")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("I") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(indexOfG);
                index++;
            }
            i = Double.parseDouble(tempAxis);
        }
        if (gcodeLine.contains("J")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("J") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(indexOfG);
                index++;
            }
            j = Double.parseDouble(tempAxis);
        }
        if (gcodeLine.contains("K")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("K") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(indexOfG);
                index++;
            }
            k = Double.parseDouble(tempAxis);
        }
        if (tempCode.contains(".")) {
            codeDouble = Double.parseDouble(tempCode);
            if (codeDouble == 91.1) {
                doc.setIsRelativeArc(true);// incremental distance mode
            } else if (codeDouble == 90.1) {
                doc.setIsRelativeArc(false);// absolute arc distance mode
            } else {
                throw new UnknownGCodeError("GCode : " + tempCode + " not parsable @ line: " + lineNum);
            }
        } else {
            code = Integer.parseInt(tempCode);
            switch (code) {
                case 0 -> {
                    // TODO Detect if Rapid extends out of part or not to detect new path
                    if (doc.getRelativity() == true) {
                        //TODO set Z-related relativity stuff
                    } else {
                        if(z>0){
                            doc.newPath2D();
                            doc.getCurrentPath2D().setZ(z);
                        }
                    }
                } // rapid move (do Nothing)
                case 1 -> {
                    if(doc.getRelativity() == true){
                        doc.getCurrentPath2D().lineToRelative(x, y);
                        doc.getCurrentPath2D().setZRelative(z);
                    }else{
                        doc.getCurrentPath2D().lineTo(x, y);
                        doc.getCurrentPath2D().setZ(z);
                    }
                } // linear move
                case 2 -> {
                    if(doc.getCurrentAxisPlane() == 17){//XY-plane

                    }
                }
                case 3 -> {
                    if(doc.getCurrentAxisPlane() == 17){
                        
                    }
                }
                case 17,18,19 -> {
                    doc.setCurrentAxisPlane(code);//sets axis planes
                }
                case 90 -> doc.setIsRelative(false);// absolute distance mode
                case 91 -> doc.setIsRelative(true);// incremental distance mode
                default -> {
                    throw new UnknownGCodeError("GCode : " + tempCode + " not parsable/not supported @ line: " + lineNum);
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

class UnknownGCodeError extends Error{
    public UnknownGCodeError(String errorMessage){
        super(errorMessage);
    }
}

class IllegalGCodeError extends Error{
    public IllegalGCodeError(String errorMessage){
        super(errorMessage);
    }
}
