package Display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class SheetEditMenu extends JPanel {
    public SheetEditMenu(){
        setLayout(null);
        setBounds(0, 0, 300, 800);
    }

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(300, 800);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
