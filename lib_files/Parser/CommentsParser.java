<<<<<<<< HEAD:Parser/GCode/CommentsParser.java
package Parser.GCode;
========
package Parser;
>>>>>>>> e900930b614978e8bc03b0d1416d3e49c8369a0e:lib_files/Parser/CommentsParser.java

public class CommentsParser {
    public static String parse(String gcodeLine, NGCDocument doc){
        while(gcodeLine.contains("(")){
            gcodeLine = gcodeLine.substring(0, gcodeLine.indexOf('('))+gcodeLine.substring(gcodeLine.indexOf(')')+1, gcodeLine.length());
        }
        return gcodeLine;
    }
}
