package Display;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewSheetPrompt extends JFrame implements ActionListener{
    private JButton enterSheetInfo;
    private JTextField sheetWidthField;
    private JTextField sheetHeightField;
    private JTextField sheetNameField;
    private JLabel errMsg;
    private JLabel widthLabel;
    private JLabel heightLabel;
    private JLabel nameLabel;
    private Screen parent;

    public NewSheetPrompt(Screen parentScreen) {
        setLayout(null);
        parent = parentScreen;

        enterSheetInfo = new JButton("Create sheet");
        enterSheetInfo.setBounds(100, 100, 200, 25);
        add(enterSheetInfo);
        enterSheetInfo.addActionListener(this);

        widthLabel = new JLabel("Width:");
        widthLabel.setBounds(25, 10, 100, 25);
        add(widthLabel);
        
        sheetWidthField = new JTextField();
        sheetWidthField.setBounds(25, 50, 100, 25);
        add(sheetWidthField);
        
        heightLabel = new JLabel("Height:");
        heightLabel.setBounds(150, 10, 100, 25);
        add(heightLabel);

        sheetHeightField = new JTextField();
        sheetHeightField.setBounds(150, 50, 100, 25);
        add(sheetHeightField);

        nameLabel = new JLabel("Name:");
        nameLabel.setBounds(275, 10, 100, 25);
        add(nameLabel);

        sheetNameField = new JTextField();
        sheetNameField.setBounds(275, 50, 100, 25);
        add(sheetNameField);

        errMsg = new JLabel("Invalid width/height");
        errMsg.setBounds(150, 0, 200, 10);
        add(errMsg);
        errMsg.setVisible(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(425, 200);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == enterSheetInfo) {
            try {
                double sheetWidth = - Double.parseDouble(sheetWidthField.getText());
                double sheetHeight = - Double.parseDouble(sheetWidthField.getText());
                String sheetName = sheetNameField.getText();
                parent.enterNewSheetInfo(sheetWidth, sheetHeight, sheetName);
            } catch(NumberFormatException err) {
                errMsg.setVisible(true);
            }
        }
    }

    public void reset() {
        errMsg.setVisible(false);
        sheetWidthField.setText("");
        sheetHeightField.setText("");
        sheetNameField.setText("");
        setBounds(parent.getParent().getX() + parent.getParent().getWidth()/2 - 425/2, parent.getParent().getY() + parent.getParent().getHeight()/2 - 200/2, 425, 200);
        pack();
    }
}