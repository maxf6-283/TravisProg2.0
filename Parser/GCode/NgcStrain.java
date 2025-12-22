package Parser.GCode;

public class NgcStrain {
    protected static final NgcStrain router_971 = new NgcStrain(
            new CommentsParser971(), new MCodeParser971(),
            new GCodeParser971(), new EndParser971());
    protected static final NgcStrain router_WinCNC = new NgcStrain(
            new CommentsParserWinCNC(), new MCodeParserWinCNC(),
            new GCodeParserWinCNC(), new EndParserWinCNC());

    protected final GenericParser commentsParser;
    protected final GenericParser mCodeParser;
    public final GenericGCodeParser gCodeParser;
    protected final GenericParser endParser;

    public NgcStrain(
            GenericParser commentsParser,
            GenericParser mCodeParser,
            GenericGCodeParser gCodeParser,
            GenericParser endParser) {
        this.commentsParser = commentsParser;
        this.mCodeParser = mCodeParser;
        this.gCodeParser = gCodeParser;
        this.endParser = endParser;
    }
}
