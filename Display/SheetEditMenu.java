package Display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import SheetHandler.Sheet;

public class SheetEditMenu extends JPanel implements ActionListener {
    private Sheet selectedSheet;
    private JLabel sheetName;
    private JLabel cutName;
    private JButton emit;
    private JButton addHole;
    private JButton addItem;
    private JButton del;
    private JButton reScan;
    private JButton save;
    private JButton addCut;
    private JButton selectGCodeView;

    public SheetEditMenu(JButton returnTo, Sheet currentSheet) {
        setLayout(new GridBagLayout());
        setBounds(0, 0, 300, 800);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(10, 10, 10, 10);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.ipady = 40;
        c.gridwidth = 3;
        add(returnTo, c);

        sheetName = new JLabel("Editing Sheet: ");
        sheetName.setForeground(Color.WHITE);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        c.ipady = 20;
        add(sheetName, c);

        addHole = new JButton("Add Hole");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.ipady = 20;
        add(addHole, c);

        addItem = new JButton("Add Item");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        add(addItem, c);

        del = new JButton("Del Select");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 2;
        add(del, c);

        reScan = new JButton("Rescan parts_library");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; 
        c.gridwidth = 2; 
        c.gridy = 3; 
        add(reScan, c);

        emit = new JButton("Emit GCode");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; 
        c.gridwidth = 1; 
        c.gridy = 4; 
        add(reScan, c);

        save = new JButton("Save");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; 
        c.gridy = 4; 
        add(reScan, c);

        addCut = new JButton("Add Cut");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2; 
        c.gridy = 4; 
        add(reScan, c);

        cutName = new JLabel("Current Cut: ");
        cutName.setForeground(Color.WHITE);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; 
        c.gridwidth = 3; 
        c.gridy = 5; 
        add(reScan, c);

        //bottom buffer
        c.gridx = 0;
        c.gridy = 6;
        c.weighty = 1;
        add(new JLabel(), c);

        selectedSheet = currentSheet;
        if (selectedSheet != null) {
            sheetName.setText("Editing Sheet: " + selectedSheet.getSheetFile().getName());
            cutName.setText("Current Cut: " + selectedSheet.getActiveCutFile().getName());
        }
    }

    public void setSelectedSheet(Sheet selectedSheet) {
        this.selectedSheet = selectedSheet;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 800);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (selectedSheet != null) {
            sheetName.setText("Editing Sheet: " + selectedSheet.getSheetFile().getName());
            cutName.setText("Current Cut: " + selectedSheet.getActiveCutFile().getName());
        }
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
