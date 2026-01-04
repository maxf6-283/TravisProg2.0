package Parser.GCode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentsParser971 implements GenericParser {
    private static final Pattern TOOL_DEF_PATTERN = Pattern.compile("T(\\d+).*?D=([\\d.]+)");

    public String parse(String gcodeLine, int lineNum, NGCDocument doc) {
        while (gcodeLine.contains("(")) {
            final int openIndex = gcodeLine.indexOf('(');
            final int closeIndex = gcodeLine.indexOf(')');

            if (closeIndex == -1 || closeIndex < openIndex)
                break;

            String content = gcodeLine.substring(openIndex + 1, closeIndex);

            // Check for Tool Definition
            Matcher m = TOOL_DEF_PATTERN.matcher(content);
            if (m.find()) {
                try {
                    int toolNum = Integer.parseInt(m.group(1));
                    double toolDia = Double.parseDouble(m.group(2));

                    // Define the tool in the library
                    doc.defineTool(toolNum, toolDia);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing tool definition at line " + lineNum);
                }
            }

            // Remove the comment from the line
            gcodeLine = gcodeLine.substring(0, openIndex) + gcodeLine.substring(closeIndex + 1);
        }
        return gcodeLine;
    }
}
