import Display.*;
import SheetHandler.Settings;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class Runner2 {
    public static void main(String[] args) {     
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Thee should  not  changeth  the  behold  and  feeleth");
        }
        Screen screen = new Screen();
        JFrame frame = new JFrame("Marissa and Claire and Nick Prog");
        try {
            BufferedImage img = ImageIO.read(new File(Settings.settings.get("IconImageFile")));
            frame.setIconImage(img);
        } catch (IOException e) {
            System.err.println("No icon image found");
        }

        frame.add(screen);

        frame.pack();
        frame.setVisible(true);
        //sets window on top but not always
        frame.setAlwaysOnTop(true);
        frame.setAlwaysOnTop(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}