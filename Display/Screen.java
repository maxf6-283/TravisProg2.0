package Display;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import SheetHandler.Part;
import SheetHandler.Sheet;
import SheetHandler.SheetThickness;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Screen extends JPanel
        implements MouseWheelListener, MouseInputListener, ActionListener, ListSelectionListener, KeyListener {
    public static final boolean DebugMode = false;
    private JList<File> sheetList;
    private JScrollPane sheetScroll;
    private DefaultListModel<File> sheetFileList;
    private File sheetsParent;
    private JButton addSheet;
    private JButton selectSheet;
    private JButton returnToHome;
    private JLabel sheetName;
    private Sheet selectedSheet;
    private State state;
    private double xCorner = 0;
    private double yCorner = 0;
    private double zoom = 20;
    private double startX; // for panning
    private double startY;
    private boolean panning = false;
    private boolean draggingPart = false;
    private boolean rotatingPart = false;
    private boolean ctrlPressed = false;
    private Point2D rotationPoint;
    private Part partBeingDragged;
    private double partGrabbedX;
    private double partGrabbedY;
    private double partGrabbedInitialRot;
    private double partGrabbedInitialX;
    private double partGrabbedInitialY;
    private NewSheetPrompt newSheetPrompt;
    private JPanel editMenu;
    private BufferedImage img;
    private ArrayList<EditAction> undoList;
    private ArrayList<EditAction> redoList;
    private AbstractAction undo;
    private AbstractAction redo;
    private AbstractAction deleteSelected;
    private JButton addHole;
    private JButton addItem;
    private JButton del;
    private JButton reScan;
    private JButton emit;
    private JButton save;
    private JButton addCut;
    private JButton measure;
    private JLabel cutName;
    private JButton changeGCodeView;
    private Part selectedPart = null;

    public Screen() {
        setLayout(null);
        state = State.SHEET_SELECT;

        sheetFileList = new DefaultListModel<>();
        sheetsParent = new File("./TestSheets");
        for (int i = 0; i < sheetsParent.listFiles().length; i++) {
            sheetFileList.addElement(new File(sheetsParent.listFiles()[i].getAbsolutePath()) {
                @Override
                public String toString() {
                    return getName();
                }
            });
        }

        sheetList = new JList<File>(sheetFileList);
        sheetScroll = new JScrollPane(sheetList);
        sheetScroll.setBounds(100, 100, 225, 600);
        add(sheetScroll);
        sheetList.addListSelectionListener(this);

        returnToHome = new JButton("Return to Home");
        // returnToHome.setBounds(0, 0, 150, 45);
        // add(returnToHome);
        returnToHome.addActionListener(this);
        // returnToHome.setVisible(false);

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

        editMenu = new SheetEditMenu();
        add(editMenu);
        editMenu.setVisible(false);

        try {
            img = ImageIO.read(new File("Display\\971 large logo.png"));
        } catch (IOException e) {
            System.err.println("Logo not Found");
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                editMenu.setBounds(0, 0, 400, e.getComponent().getHeight());
            }
        });

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);

        setFocusable(true);
        requestFocus();

        undoList = new ArrayList<>();
        redoList = new ArrayList<>();

        undo = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoList.size() > 0) {
                    EditAction action = undoList.get(undoList.size() - 1);
                    action.undoAction(selectedSheet);
                    undoList.remove(action);
                    redoList.add(action);
                    if (!action.doesSomething()) {
                        this.actionPerformed(e);
                    }
                    repaint();
                }
            }
        };

        redo = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (redoList.size() > 0) {
                    EditAction action = redoList.get(redoList.size() - 1);
                    action.redoAction(selectedSheet);
                    redoList.remove(action);
                    undoList.add(action);
                    if (!action.doesSomething()) {
                        this.actionPerformed(e);
                    }
                    repaint();
                }
            }
        };

        deleteSelected = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedPart != null) {
                    // correct for deleting and moving a part at the same time
                    if (selectedPart == partBeingDragged) {
                        undoList.add(new EditAction(partBeingDragged, partGrabbedInitialX, partGrabbedInitialY,
                                partGrabbedInitialRot));
                        draggingPart = false;
                        partBeingDragged = null;
                    }
                    redoList.clear();
                    selectedSheet.removePart(selectedPart);
                    undoList.add(new EditAction(selectedPart, false));
                    repaint();
                }
            }
        };

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control Z"), "undo");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control Y"), "redo");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "delete");

        getActionMap().put("undo", undo);
        getActionMap().put("redo", redo);
        getActionMap().put("delete", deleteSelected);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1200, 800);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(33, 30, 31));
        g.fillRect(0, 0, getWidth(), getHeight());
        if (state == State.SHEET_SELECT) {
            g.drawImage(img, (getWidth() - 800) / 2, (getHeight() - 800) / 2, null);
        }
        switch (state) {
            case SHEET_SELECT -> {

            }
            case SHEET_EDIT -> {
                Graphics2D g2d = (Graphics2D) g;
                g2d.scale(zoom, zoom);
                g2d.translate(xCorner, yCorner);
                g2d.setStroke(new BasicStroke((float) (1 / zoom)));
                selectedSheet.draw(g);
                g2d.translate(-xCorner, -yCorner);
                g2d.scale(1 / zoom, 1 / zoom);

                if (rotationPoint != null) {
                    g2d.setColor(Color.ORANGE);
                    Point2D rPnt = sheetToScreen(rotationPoint);
                    g2d.drawLine((int) rPnt.getX() - 5, (int) rPnt.getY() - 5, (int) rPnt.getX() + 5,
                            (int) rPnt.getY() + 5);
                    g2d.drawLine((int) rPnt.getX() - 5, (int) rPnt.getY() + 5, (int) rPnt.getX() + 5,
                            (int) rPnt.getY() - 5);
                }
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
        if (e.getButton() == MouseEvent.BUTTON1 && state == State.SHEET_EDIT) {
            if (selectSheet != null) {
                Point2D grabLocation = screenToSheet(e.getPoint());
                partBeingDragged = selectedSheet.contains(grabLocation);

                if (ctrlPressed && rotationPoint == null) {
                    rotatingPart = true;
                    rotationPoint = grabLocation;
                } else if (partBeingDragged != null) {
                    if (selectedPart != null && !selectedPart.equals(partBeingDragged)) {
                        selectedPart.setSelected(false);
                    }
                    partBeingDragged.setSelected(true);
                    selectedPart = partBeingDragged;
                    if (ctrlPressed) {
                        draggingPart = true;
                        partGrabbedX = grabLocation.getX();
                        partGrabbedY = grabLocation.getY();
                        partGrabbedInitialRot = partBeingDragged.getRot();
                        partGrabbedInitialX = partBeingDragged.getX();
                        partGrabbedInitialY = partBeingDragged.getY();

                    } else {
                        draggingPart = true;
                        partGrabbedX = grabLocation.getX();
                        partGrabbedY = grabLocation.getY();
                        partGrabbedInitialRot = partBeingDragged.getRot();
                        partGrabbedInitialX = partBeingDragged.getX();
                        partGrabbedInitialY = partBeingDragged.getY();
                    }
                } else {
                    startX = xCorner - e.getX() / zoom;
                    startY = yCorner - e.getY() / zoom;
                    selectedPart.setSelected(false);
                    selectedPart = null;
                    panning = true;
                }
            }
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (draggingPart) {
                EditAction toBeUndone = new EditAction(partBeingDragged, partGrabbedInitialX, partGrabbedInitialY,
                        partGrabbedInitialRot);
                undoList.add(toBeUndone);
                redoList.clear();
                partBeingDragged = null;
            }
            panning = false;
            draggingPart = false;
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
        if (panning) {
            xCorner = startX + e.getX() / zoom;
            yCorner = startY + e.getY() / zoom;
        } else if (draggingPart) {
            Point2D movedPoint = screenToSheet(e.getPoint());
            if (rotatingPart && partGrabbedX - rotationPoint.getX() != 0
                    && movedPoint.getX() - rotationPoint.getX() != 0) {
                // oo fun rotation i had no pain at all coding this

                // get the starting angle from the rotation point
                double startingAngle = Math
                        .atan((partGrabbedY - rotationPoint.getY()) / (partGrabbedX - rotationPoint.getX()));
                if ((partGrabbedX - rotationPoint.getX()) > 0) {
                    startingAngle += Math.PI;
                }

                // get the ending angle from the rotation point
                double endingAngle = Math
                        .atan((movedPoint.getY() - rotationPoint.getY()) / (movedPoint.getX() - rotationPoint.getX()));
                if ((movedPoint.getX() - rotationPoint.getX()) > 0) {
                    startingAngle += Math.PI;
                }
                double rot = endingAngle - startingAngle;

                // now time for the movement!!!! yay!!!!!!!!!!!!!!
                // so baiscally what i need to do is translate the ctrl point along the same
                // rotation as if it were attachted to the part and then move the part by the
                // offset
                Point2D.Double ctrlPoint = (Point2D.Double) rotationPoint.clone();

                // translate it so the part center is at 0,0
                ctrlPoint.setLocation(ctrlPoint.getX() - partGrabbedInitialX + selectedSheet.getWidth(),
                        ctrlPoint.getY() + partGrabbedInitialY - selectedSheet.getHeight());

                // rotate it the same angle
                ctrlPoint.setLocation(ctrlPoint.getX() * Math.cos(rot) + ctrlPoint.getY() * -Math.sin(rot),
                        ctrlPoint.getX() * Math.sin(rot) + ctrlPoint.getY() * Math.cos(rot));

                // translate it back
                ctrlPoint.setLocation(ctrlPoint.getX() + partGrabbedInitialX - selectedSheet.getWidth(),
                        ctrlPoint.getY() - partGrabbedInitialY + selectedSheet.getHeight());

                // get the difference
                double xDiff = ctrlPoint.getX() - rotationPoint.getX();
                double yDiff = ctrlPoint.getY() - rotationPoint.getY();

                // translate part
                partBeingDragged.setX(partGrabbedInitialX - xDiff);
                partBeingDragged.setY(partGrabbedInitialY + yDiff);
                partBeingDragged.setRot(partGrabbedInitialRot - rot);
            } else {
                partBeingDragged.setX(partBeingDragged.getX() - partGrabbedX + movedPoint.getX());
                partBeingDragged.setY(partBeingDragged.getY() + partGrabbedY - movedPoint.getY());
                partGrabbedX = movedPoint.getX();
                partGrabbedY = movedPoint.getY();
            }

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
        } else if (e.getSource() == returnToHome) {
            switchStates(State.SHEET_SELECT);
        } else if (e.getSource() == addHole) {

        } else if (e.getSource() == addItem) {

        } else if (e.getSource() == del) {
            deleteSelected.actionPerformed(e);
        } else if (e.getSource() == reScan) {

        } else if (e.getSource() == emit) {

        } else if (e.getSource() == save) {

        } else if (e.getSource() == addCut) {

        } else if (e.getSource() == changeGCodeView) {

        } else if (e.getSource() == measure) {

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
            // get the actual xy coord selected
            double xPos = e.getX() / zoom;
            double yPos = e.getY() / zoom;

            // translate the corner to the orgin
            xCorner -= xPos;
            yCorner -= yPos;

            // something something sigmoid
            zoom *= 1 / Math.pow(Math.E, e.getPreciseWheelRotation() / 10);

            // translate the point back
            xCorner += e.getX() / zoom;
            yCorner += e.getY() / zoom;

            repaint();
        }
    }

    /**
     * Create a new sheet based on the given parameters
     * 
     * @param sheetWidth - the width of the sheet (almost always will be negative)
     * @param sheetY     - the height of the sheet
     * @param sheetName  - the name of the sheet
     */
    public void enterNewSheetInfo(double sheetWidth, double sheetHeight, String sheetName, SheetThickness thickness) {
        selectedSheet = new Sheet(sheetsParent, sheetName, sheetWidth, sheetHeight, thickness);
        switchStates(State.SHEET_SELECT);
    }

    public void returnToNormal() {
        switchStates(State.SHEET_SELECT);
    }

    /**
     * Switch between program states (note: intended for graphical use only; all
     * technical parts are done separately)
     * 
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
                returnToHome.setVisible(false);
                editMenu.setVisible(false);
            }
            case SHEET_EDIT -> {
                state = State.SHEET_EDIT;
                editMenu.setVisible(true);
                returnToHome.setVisible(true);
                selectSheet.setVisible(false);
                addSheet.setVisible(false);
                sheetList.setVisible(false);
                sheetScroll.setVisible(false);
                zoom = 20;
                xCorner = getWidth() / 2.0 + selectedSheet.getWidth() * 10.0;
                yCorner = getHeight() / 2.0 - selectedSheet.getHeight() * 10.0;
                xCorner /= zoom;
                yCorner /= zoom;
                newSheetPrompt.setVisible(false);
            }
            case SHEET_ADD -> {
                state = State.SHEET_ADD;
                editMenu.setVisible(false);
                returnToHome.setVisible(false);
                selectSheet.setVisible(false);
                addSheet.setVisible(false);
                sheetList.setVisible(false);
                sheetScroll.setVisible(false);
                newSheetPrompt.reset();
                newSheetPrompt.setVisible(true);
            }
        }
    }

    private Point2D screenToSheet(Point2D in) {
        Point2D out = new Point2D.Double(in.getX(), in.getY());
        out.setLocation(out.getX() - xCorner * zoom, out.getY() - yCorner * zoom);
        out.setLocation(out.getX() / zoom, out.getY() / zoom);
        return out;

    }

    private Point2D sheetToScreen(Point2D in) {
        Point2D out = new Point2D.Double(in.getX(), in.getY());
        out.setLocation(out.getX() * zoom, out.getY() * zoom);
        out.setLocation(out.getX() + xCorner * zoom, out.getY() + yCorner * zoom);
        return out;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 17) {
            ctrlPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == 17) {
            ctrlPressed = false;
            rotatingPart = false;
            rotationPoint = null;
            repaint();
        }
    }

    public class SheetEditMenu extends JPanel {
        public SheetEditMenu() {
            setLayout(new GridBagLayout());
            setBounds(0, 0, 300, 800);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.insets = new Insets(10, 5, 10, 5);
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 0.5;
            c.ipady = 40;
            c.gridwidth = 3;
            add(returnToHome, c);

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
            addHole.addActionListener(Screen.this);

            addItem = new JButton("Add Item");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 2;
            add(addItem, c);
            addItem.addActionListener(Screen.this);

            del = new JButton("Del Select");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 2;
            c.gridy = 2;
            add(del, c);
            del.addActionListener(Screen.this);

            reScan = new JButton("Rescan parts_library");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridwidth = 2;
            c.gridy = 3;
            add(reScan, c);
            reScan.addActionListener(Screen.this);

            emit = new JButton("Emit GCode");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridwidth = 1;
            c.gridy = 4;
            add(emit, c);
            emit.addActionListener(Screen.this);

            save = new JButton("Save");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 4;
            add(save, c);
            save.addActionListener(Screen.this);

            addCut = new JButton("Add Cut");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 2;
            c.gridy = 4;
            add(addCut, c);
            addCut.addActionListener(Screen.this);

            cutName = new JLabel("Current Cut: ");
            cutName.setForeground(Color.WHITE);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridwidth = 3;
            c.gridy = 5;
            add(cutName, c);

            changeGCodeView = new JButton("Change GCode viewed");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 7;
            add(changeGCodeView, c);
            changeGCodeView.addActionListener(Screen.this);

            measure = new JButton("Measure");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 2;
            c.gridy = 3;
            add(measure, c);
            measure.addActionListener(Screen.this);

            // bottom buffer
            c.gridx = 0;
            c.gridy = 6;
            c.weighty = 1;
            add(new JLabel(), c);

            if (selectedSheet != null) {
                sheetName.setText("Editing Sheet: " + selectedSheet.getSheetFile().getName());
                cutName.setText("Current Cut: " + selectedSheet.getActiveCutFile().getName());
            }
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
}
