package Display;

import javax.swing.JPanel;
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

public class Screen extends JPanel implements MouseInputListener, ActionListener{
    private JList<File> sheetList;
    private DefaultListModel<File> sheetFileList;
    private JButton addSheet;
    private Sheet selectedSheet;
    
    public Screen() {
        setLayout(null);

        sheetFileList = new DefaultListModel<>();
        File sheetParent = new File("./TestSheets");
        for(File sheetFile : sheetParent.listFiles()) {
            sheetFileList.addElement(sheetFile);
        }
        
        sheetList = new JList<File>(sheetFileList);
        sheetList.setBounds(100, 100, 200, 600);
        add(sheetList);

        addSheet = new JButton("Add new sheet");
        addSheet.setBounds(250, 100, 100, 50);
        add(addSheet);
        addSheet.addActionListener(this);
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
    }

   
}
