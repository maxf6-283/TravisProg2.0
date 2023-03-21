package Display;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import SheetHandler.Sheet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelListener;

import java.io.File;

public class Screen extends JPanel
        implements MouseWheelListener, MouseInputListener, ActionListener, ListSelectionListener {
    private JList<File> sheetList;
    private DefaultListModel<File> sheetFileList;
    private File sheetParent;
    private JButton addSheet;
    private JButton selectSheet;
    private Sheet selectedSheet;
    private State state;
    private double xCorner = 0;
    private double yCorner = 0;
    private double zoom = 20;
    private double startX; //for panning
    private double startY;
    private boolean panning = false;

    public Screen() {
        setLayout(null);
        state = State.SHEET_SELECT;

        sheetFileList = new DefaultListModel<>();
        sheetParent = new File("./TestSheets");
        for(int i = 0; i < sheetParent.listFiles().length; i++){
            sheetFileList.addElement(new File(sheetParent.listFiles()[i].getAbsolutePath()){
                @Override
                public String toString(){
                    return getName();
                }
            });
        }

        sheetList = new JList<File>(sheetFileList);
        sheetList.setBounds(100, 100, 200, 600);
        add(sheetList);
        sheetList.addListSelectionListener(this);

        addSheet = new JButton("Add new sheet");
        addSheet.setBounds(350, 100, 200, 50);
        add(addSheet);
        addSheet.addActionListener(this);

        selectSheet = new JButton("Select sheet");
        selectSheet.setBounds(350, 200, 200, 50);
        add(selectSheet);
        selectSheet.addActionListener(this);
        selectSheet.setEnabled(false);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 800);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 1200, 800);
        switch (state) {
            case SHEET_SELECT -> {

            }
            case SHEET_EDIT -> {
                Graphics2D g2d = (Graphics2D) g;
                g2d.scale(zoom, zoom);
                g2d.translate(xCorner, yCorner);
                g2d.setStroke(new BasicStroke((float)(1/zoom)));
                selectedSheet.draw(g);
                g2d.translate(-xCorner, -yCorner);
                g2d.scale(1 / zoom, 1 / zoom);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON3 && state == State.SHEET_EDIT) {
            startX = xCorner - e.getX() / zoom;
            startY = yCorner - e.getY() / zoom;
            panning = true;
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON3) {
            panning = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(panning) {
            xCorner = startX + e.getX() / zoom;
            yCorner = startY + e.getY() / zoom;
        }
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addSheet) {
            // TODO: set up creating a new sheet here
        } else if (e.getSource() == selectSheet) {
            File sheetFile = sheetList.getSelectedValue();
            for (File file : sheetFile.listFiles()) {
                if (file.getName().endsWith(".sheet")) {
                    sheetFile = file;
                    break;
                }
            }
            selectedSheet = new Sheet(sheetFile);
            state = State.SHEET_EDIT;
            selectSheet.setVisible(false);
            addSheet.setVisible(false);
            sheetList.setVisible(false);
            zoom = 20;
            xCorner = getWidth()/2.0+selectedSheet.getWidth()*10.0;
            yCorner = getHeight()/2.0-selectedSheet.getHeight()*10.0;
            xCorner /= zoom;
            yCorner /= zoom;
        }

        repaint();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == sheetList) {
            if (sheetList.getSelectedIndex() == -1) {
                selectSheet.setEnabled(false);
            } else {
                selectSheet.setEnabled(true);
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (state == State.SHEET_EDIT) {
            //get the actual xy coord selected
            double xPos = e.getX() / zoom;
            double yPos = e.getY() / zoom;
            
            //translate the corner to the orgin
            xCorner -= xPos;
            yCorner -= yPos;
            
            //something something sigmoid
            zoom *= 1 / Math.pow(Math.E, e.getPreciseWheelRotation() / 25);

            //translate the point back
            xCorner += e.getX() / zoom;
            yCorner += e.getY() / zoom;

            repaint();
        }
    }

}
