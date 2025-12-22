package Parser.GCode;

import SheetHandler.Part;
import java.util.HashMap;

public interface GenericGCodeParser extends GenericParser {
    public void addGCodeAttributes(HashMap<String, Double> attributes, NGCDocument doc);

    public String getGCodeHeader(NGCDocument doc);

    public String getGCodeBody(NGCDocument doc);

    public String getGCodeFooter(NGCDocument doc);

    public String removeGCodeSpecialness(String gCode);

    public String gCodeTransformClean(Part part);
}
