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
                tempAxis += gcodeLine.charAt(index);
                index++;
            }
            x = Double.parseDouble(tempAxis);
        }
        if (gcodeLine.contains("Y")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("Y") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(index);
                index++;
            }
            y = Double.parseDouble(tempAxis);
            y = -y;
        }
        if (gcodeLine.contains("Z")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("Z") + 1;
            while (!(index >= gcodeLine.length() || (gcodeLine.charAt(index) == ' '))) {
                tempAxis += gcodeLine.charAt(index);
                index++;
            }
            z = Double.parseDouble(tempAxis);
        }
        if (gcodeLine.contains("I")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("I") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(index);
                index++;
            }
            i = Double.parseDouble(tempAxis);
        }
        if (gcodeLine.contains("J")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("J") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(index);
                index++;
            }
            j = Double.parseDouble(tempAxis);
        }
        if (gcodeLine.contains("K")) {
            String tempAxis = "";
            int index = gcodeLine.indexOf("K") + 1;
            while (!(index >= gcodeLine.length() || !(gcodeLine.charAt(index) != ' '))) {
                tempAxis += gcodeLine.charAt(index);
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
                throw new IllegalGCodeError("Absolute Arc Distance Mode is not currently supported");
                //TODO make this supported
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
                            doc.getCurrentPath2D().moveTo(x, y);
                            System.out.println("move to: "+x+", "+y);
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
                        System.out.println("line to: "+x+", "+y);
                    }
                } // linear move
                case 2 -> {
                    if(doc.getCurrentAxisPlane() == 17){//XY-plane
                        doc.getCurrentPath2D().arcToRelative(i, j, x, y, -1);
                    }
                }
                case 3 -> {
                    if(doc.getCurrentAxisPlane() == 17){
                        doc.getCurrentPath2D().arcToRelative(i, j, x, y, 1);
                    }
                }
                case 4 -> {
                    //dwell aka do nothing
                }
                case 10 -> {
                    //WCS Offset Select
                }
                case 17,18,19 -> {
                    doc.setCurrentAxisPlane(code);//sets axis planes
                }
                case 20 -> {
                    //do Nothing(inches mode)
                }
                case 21 ->{
                    //TODO automatically fix
                    throw new IllegalGCodeError("Metric Units not allowed in the world of imperial allens and wrenches");
                }
                case 43 -> {
                    //calls which tool offset is used(TODO fix complexities)
                }
                case 53 -> {
                    //Move In Machine Coordinates
                    if(z != 0){
                        throw new IllegalGCodeError(gcodeLine +" needs to be z0");
                    }
                }
                case 54,55,56,57,58,59 ->{
                    //WCS Offset(Do nothing for NOW TODO fix this)
                }
                case 64 -> {
                    //do Nothing(Path Blending??!!??)
                }
                case 90 -> doc.setIsRelative(false);// absolute distance mode
                case 91 -> doc.setIsRelative(true);// incremental distance mode
                case 94 -> {
                    //do Nothing(Feed rate change)
                }
                default -> {
                    throw new UnknownGCodeError("GCode : " + tempCode + " not parsable/not supported @ line: " + (lineNum-1));
                }
            }
        }
        doc.setGCodeHolder(Double.parseDouble(tempCode));
        gcodeLine = gcodeLine.substring(0, indexOfG - tempCode.length() - 1)
                + gcodeLine.substring(indexOfG, gcodeLine.length());
        gcodeLine.trim();
        if (gcodeLine.contains("G")) {
            GCodeParser.parse(gcodeLine, lineNum, doc);
        }
    }

    public static void parseImplicit(String gcodeLine, int lineNum, NGCDocument doc) {
        if(doc.getGCodeHolder()-Math.floor(doc.getGCodeHolder()) != 0.0){
            parse("G"+doc.getGCodeHolder()+" "+gcodeLine, lineNum, doc);
        }
        parse("G"+(int)(doc.getGCodeHolder())+" "+gcodeLine, lineNum, doc);
    }
}
