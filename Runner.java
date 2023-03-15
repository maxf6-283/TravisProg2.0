import Display.*;
import javax.swing.JFrame;

public class Runner {
    public static void main(String[] args) {
        Screen screen = new Screen();
        JFrame frame = new JFrame();
        
        frame.add(screen);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}