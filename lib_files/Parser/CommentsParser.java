package Parser;

public class CommentsParser {
    public static String parse(String gcodeLine, NGCDocument doc){
        while(gcodeLine.contains("(")){
            gcodeLine = gcodeLine.substring(0, gcodeLine.indexOf('('))+gcodeLine.substring(gcodeLine.indexOf(')')+1, gcodeLine.length());
        }
        return gcodeLine;
    }
}
