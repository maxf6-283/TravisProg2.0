package SheetHandler;

import java.awt.Graphics;

public class Hole extends Part{
    public Hole(double x, double y, double rot) {
        super(null, x, y, rot);
        //TODO change partfile, maybe???
    }

    @Override
    public void draw(Graphics g) {
        //TODO: draw a circle but sized correctly;
        g.fillOval((int)(getX()-0.5), (int)(-getY()-0.5), 1, 1);
    }
}
