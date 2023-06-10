package SheetHandler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.File;

import Parser.GCode.NGCDocument;

/**
 * Holes are essentially identical to Parts besides drawing
 */
public class Hole extends Part {
    public static final double HEAD_SIZE = Double.parseDouble(Settings.settings.get("ScrewHeadSize"));

    public Hole(File holeFile, double x, double y, double rot) {
        super(holeFile, x, y, rot);
    }

    @Override
    // draws circle at point instead of gcode
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform prevTransform = g2d.getTransform();
        g2d.translate(getX(), -getY());
        Color prevColor = g.getColor();
        g2d.rotate(-getRot());
        if (getSelected() == true) {
            g.setColor(Color.RED);
        } /*
           * else {
           * g.setColor(Color.ORANGE);
           * }
           */
        Stroke currentStrok = g2d.getStroke();
        // ((Graphics2D)g).draw(new Ellipse2D.Double(getX()-0.5,-sheetY-1,1,2));

        for (NGCDocument activeNgcDoc : getNgcDocuments()) {
            g2d.setStroke(
                    new BasicStroke((float) activeNgcDoc.getToolOffset(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            100000));
            activeNgcDoc.getRelativePath2Ds().stream().forEach(e -> g2d.draw(e));
        }

        g2d.setStroke(currentStrok);
        g2d.setColor(Color.ORANGE);
        g2d.draw(new Ellipse2D.Double(-HEAD_SIZE / 2, -HEAD_SIZE / 2, HEAD_SIZE, HEAD_SIZE));
        g2d.setColor(prevColor);
        g2d.setTransform(prevTransform);
    }
}
