package Display;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Dimension;

public class Screen extends JPanel {
    
    public Screen() {
        setLayout(null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 1200);
    }

    @Override
    public void paintComponent(Graphics g) {

    }

   
}
