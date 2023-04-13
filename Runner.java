import Display.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class Runner {
    public static void main(String[] args) {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Thee should  not  changeth  the  behold  and  feeleth");
        }
        Screen screen = new Screen();
        JFrame frame = new JFrame("JustinAndMaxProg");
        try {
            BufferedImage img = ImageIO.read(new File("Display/971 Icon.png"));
            frame.setIconImage(img);
        } catch (IOException e) {
            System.err.println("No icon image found");
        }

        frame.add(screen);

        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
