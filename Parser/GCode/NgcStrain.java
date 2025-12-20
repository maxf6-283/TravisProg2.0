package Parser.GCode;

public class NgcStrain {
    protected static final NgcStrain router_971 = new NgcStrain(new CommentsParser971(), new MCodeParser971(),
            new GCodeParser971());

    protected final GenericParser commentsParser;
    protected final GenericParser mCodeParser;
    protected final GenericParser gCodeParser;

    public NgcStrain(
            GenericParser commentsParser, GenericParser mCodeParser, GenericParser gCodeParser) {
        this.commentsParser = commentsParser;
        this.mCodeParser = mCodeParser;
        this.gCodeParser = gCodeParser;
    }
}
