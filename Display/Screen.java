package Display;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
    private JScrollPane sheetScroll;
    private DefaultListModel<File> sheetFileList;
    private File sheetsParent;
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
    private NewSheetPrompt newSheetPrompt;

    public Screen() {
        setLayout(null);
        state = State.SHEET_SELECT;

        sheetFileList = new DefaultListModel<>();
        sheetsParent = new File("./TestSheets");
        for(int i = 0; i < sheetsParent.listFiles().length; i++){
            sheetFileList.addElement(new File(sheetsParent.listFiles()[i].getAbsolutePath()){
                @Override
                public String toString(){
                    return getName();
                }
            });
        }

        sheetList = new JList<File>(sheetFileList);
        sheetScroll = new JScrollPane(sheetList);
        sheetScroll.setBounds(100, 100, 200, 600);
        add(sheetScroll);
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

        newSheetPrompt = new NewSheetPrompt(this);
        newSheetPrompt.setVisible(false);
        newSheetPrompt.setAlwaysOnTop(true);
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
            case SHEET_ADD -> {

            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1 && state == State.SHEET_EDIT) {
            startX = xCorner - e.getX() / zoom;
            startY = yCorner - e.getY() / zoom;
            panning = true;
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
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
            switchStates(State.SHEET_ADD);
        } else if (e.getSource() == selectSheet) {
            File sheetFile = sheetList.getSelectedValue();
            for (File file : sheetFile.listFiles()) {
                if (file.getName().endsWith(".sheet")) {
                    sheetFile = file;
                    break;
                }
            }
            selectedSheet = new Sheet(sheetFile);
            switchStates(State.SHEET_EDIT);
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
            zoom *= 1 / Math.pow(Math.E, e.getPreciseWheelRotation() / 10);

            //translate the point back
            xCorner += e.getX() / zoom;
            yCorner += e.getY() / zoom;

            repaint();
        }
    }

    /**
     * Create a new sheet based on the given parameters
     * @param sheetWidth - the width of the sheet (almost always will be negative)
     * @param sheetY - the height of the sheet
     * @param sheetName - the name of the sheet
     */
    public void enterNewSheetInfo(double sheetWidth, double sheetHeight, String sheetName) {
        selectedSheet = new Sheet(sheetsParent, sheetName, sheetWidth, sheetHeight);
    }

    /**
     * Switch between program states (note: intended for graphical use only; all technical parts are done separately)
     * @param newState - the state to swtich to
     */
    private void switchStates(State newState) {
        switch (newState) {
            case SHEET_SELECT -> {
                state = State.SHEET_SELECT;
                selectSheet.setVisible(true);
                addSheet.setVisible(true);
                sheetList.setVisible(true);
                sheetScroll.setVisible(true);
                newSheetPrompt.setVisible(false);
            }
            case SHEET_EDIT -> {
                state = State.SHEET_EDIT;
                selectSheet.setVisible(false);
                addSheet.setVisible(false);
                sheetList.setVisible(false);
                sheetScroll.setVisible(false);
                zoom = 20;
                xCorner = getWidth()/2.0+selectedSheet.getWidth()*10.0;
                yCorner = getHeight()/2.0-selectedSheet.getHeight()*10.0;
                xCorner /= zoom;
                yCorner /= zoom;
                newSheetPrompt.setVisible(false);
            }
            case SHEET_ADD -> {
                state = State.SHEET_ADD;
                selectSheet.setVisible(false);
                addSheet.setVisible(false);
                sheetList.setVisible(false);
                sheetScroll.setVisible(false);
                newSheetPrompt.reset();
                newSheetPrompt.setVisible(true);
            }
        }
    }
}
