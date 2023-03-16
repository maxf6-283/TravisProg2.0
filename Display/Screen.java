package Display;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import SheetHandler.Sheet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;

import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

public class Screen extends JPanel implements MouseInputListener, ActionListener, ListSelectionListener{
    private JList<File> sheetList;
    private DefaultListModel<File> sheetFileList;
    private File sheetParent;
    private JButton addSheet;
    private JButton selectSheet;
    private Sheet selectedSheet;
    private State state;
    
    public Screen() {
        setLayout(null);
        state = State.SHEET_SELECT;

        sheetFileList = new DefaultListModel<>();
        sheetParent = new File("./TestSheets");
        for(File sheetFile : sheetParent.listFiles()) {
            sheetFileList.addElement(sheetFile);
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

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseClicked'");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mousePressed'");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseReleased'");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseEntered'");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseExited'");
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseDragged'");
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mouseMoved'");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == addSheet) {
            //TODO: set up creating a new sheet here
        }
        else if(e.getSource() == selectSheet) {
            File sheetFile = sheetList.getSelectedValue();
            for(File file : sheetFile.listFiles()) {
                if(file.getName().endsWith(".sheet")) {
                    sheetFile = file;
                    break;
                }
            }
            selectedSheet = new Sheet(sheetFile);
            state = State.SHEET_EDIT;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(e.getSource() == sheetList) {
            if(sheetList.getSelectedIndex() == -1) {
                selectSheet.setEnabled(false);
            } else {
                selectSheet.setEnabled(true);
            }
        }
    }

   
}
