package Parser.GCode;

public class CommentsParser {
    public static String parse(String gcodeLine, NGCDocument doc) {
        while (gcodeLine.contains("(")) {
            String temp = gcodeLine.substring(gcodeLine.indexOf('(') + 1, gcodeLine.indexOf(')'));
            gcodeLine = gcodeLine.substring(0, gcodeLine.indexOf('('))
                    + gcodeLine.substring(gcodeLine.indexOf(')') + 1, gcodeLine.length());
            String tempDouble = "";
            if (temp.contains("T1") && temp.contains("D=")) {
                int i = temp.indexOf("D=")+2;
                while(temp.charAt(i) != ' ') {
                    tempDouble += temp.charAt(i++);
                }
                doc.setToolOffset(Double.parseDouble(tempDouble));
            }
        }
        return gcodeLine;
    }
}
