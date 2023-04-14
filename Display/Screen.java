package Display;

import static Display.Screen.SheetMenuState.*;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import Parser.GCode.NGCDocument;
import SheetHandler.Cut;
import SheetHandler.Hole;
import SheetHandler.Part;
import SheetHandler.Sheet;
import SheetHandler.SheetThickness;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Collections;

public class Screen extends JPanel
        implements MouseWheelListener, MouseInputListener, ActionListener, ListSelectionListener, ItemListener {
    public static Screen screen;
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
    private BufferedImage img;
    private ArrayList<EditAction> undoList;
    private ArrayList<EditAction> redoList;
    private AbstractAction undo;
    private AbstractAction redo;
    private AbstractAction deleteSelected;
    private AbstractAction saveSheet;
    private JButton addHole;
    private JButton addItem;
    private JButton del;
    private JButton reScan;
    private JButton emit;
    private JButton save;
    private JButton addCut;
    private JButton measure;
    private JButton changeCut;
    private JLabel cutName;
    private JButton changeGCodeView;
    private Part selectedPart = null;
    private Point2D.Double measurePoint1;
    private Point2D.Double measurePoint2;
    private SheetMenuState menuState = NULL;
    private ArrayList<JPanel> menuPanels = new ArrayList<>();
    private SheetEditMenu editMenu;
    private JPanel cutPanel;
    private JPanel gcodeCutPanel;
    private ItemSelectMenu itemSelectMenu;
    private EmitSelect emitPanel;
    private JButton[] suffixes;
    private JPanel gcodePartPanel;
    private JPanel newcutPanel;
    private ReturnToHomeJButton returnToHomeMenu;
    private ReturnOnceJButton returnOnce;
    public static Logger logger;
    private JTextField newCutField;
    private JButton newCutButton;
    private boolean aHeld;

    static {
        logger = Logger.getLogger("MyLog");
        FileHandler fh;
        try {
            fh = new FileHandler("logger.log", true);
            logger.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        logger.setUseParentHandlers(DebugMode);
    }

    public Screen() {
        screen = this;
        setLayout(null);
        state = State.SHEET_SELECT;

        sheetFileList = new DefaultListModel<>();
        sheetsParent = new File("./sheets");
        for (int i = 0; i < sheetsParent.listFiles().length; i++) {
            sheetFileList.addElement(new File(sheetsParent.listFiles()[i].getAbsolutePath()) {
                @Override
                public String toString() {
                    return getName();
                }
            });
        }

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                new ErrorDialog(e);
            }
        });

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
        menuPanels.add(editMenu);

        newcutPanel = new NewCutMenu();
        add(newcutPanel);
        newcutPanel.setVisible(false);
        newcutPanel.setBounds(editMenu.getBounds());
        menuPanels.add(newcutPanel);

        returnToHomeMenu = new ReturnToHomeJButton("Return to Home");
        returnToHomeMenu.addActionListener(this);

        returnOnce = new ReturnOnceJButton("Return");
        returnOnce.addActionListener(this);

        itemSelectMenu = new ItemSelectMenu();
        add(itemSelectMenu);
        itemSelectMenu.setVisible(false);
        itemSelectMenu.setBounds(editMenu.getBounds());
        menuPanels.add(itemSelectMenu);

        try {
            img = ImageIO.read(new File("Display\\971 large logo.png"));
        } catch (IOException e) {
            System.err.println("Logo not Found");
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                menuPanels.stream().forEach(j -> j.setBounds(0, 0, 400, e.getComponent().getHeight()));
            }
        });

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

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

        saveSheet = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedSheet != null) {
                    selectedSheet.saveToFile();
                }
            }
        };

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control Z"), "undo");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control Y"), "redo");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "save");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "delete");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("BACK_SPACE"), "delete");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("A"), "placement mode on");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released A"), "placement mode off");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control CONTROL"),
                "rotation mode on");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released CONTROL"),
                "rotation mode off");

        getActionMap().put("undo", undo);
        getActionMap().put("redo", redo);
        getActionMap().put("delete", deleteSelected);
        getActionMap().put("placement mode on", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aHeld = true;
            }
        });

        getActionMap().put("placement mode off", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aHeld = false;
            }
        });

        getActionMap().put("rotation mode on", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ctrlPressed = true;
            }
        });

        getActionMap().put("rotation mode off", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ctrlPressed = false;
                rotatingPart = false;
                rotationPoint = null;
            }
        });

        menuPanels.stream().forEach(e -> {
            e.setVisible(false);
        });
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
        // set all menu JPanels not visible
        measure.setForeground(menuState == MEASURE ? Color.LIGHT_GRAY : null);
        addHole.setForeground(menuState == ADD_HOLE ? Color.LIGHT_GRAY : null);
        switch (state) {
            case SHEET_SELECT -> {
                g.drawImage(img, (getWidth() - 800) / 2, (getHeight() - 800) / 2, null);
            }
            case SHEET_EDIT -> {
                Graphics2D g2d = (Graphics2D) g;
                AffineTransform prevTransform = g2d.getTransform();
                g2d.scale(zoom, zoom);
                g2d.translate(xCorner, yCorner);
                g2d.setStroke(new BasicStroke((float) (1 / zoom)));
                selectedSheet.draw(g);
                g2d.setTransform(prevTransform);

                if (rotationPoint != null) {
                    g2d.setColor(Color.ORANGE);
                    Point2D rPnt = sheetToScreen(rotationPoint);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawLine((int) rPnt.getX() - 5, (int) rPnt.getY() - 5, (int) rPnt.getX() + 5,
                            (int) rPnt.getY() + 5);
                    g2d.drawLine((int) rPnt.getX() - 5, (int) rPnt.getY() + 5, (int) rPnt.getX() + 5,
                            (int) rPnt.getY() - 5);
                }
                if (menuState == MEASURE) {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(2));
                    Point2D screenPoint1 = null;
                    Point2D screenPoint2 = null;
                    if (measurePoint1 != null) {
                        screenPoint1 = sheetToScreen(measurePoint1);
                        g2d.fillOval((int) screenPoint1.getX() - 5, (int) screenPoint1.getY() - 5, 10, 10);
                    }
                    if (measurePoint2 != null) {
                        screenPoint2 = sheetToScreen(measurePoint2);
                        g2d.fillOval((int) screenPoint2.getX() - 5, (int) screenPoint2.getY() - 5, 10, 10);
                    }
                    if (measurePoint1 != null && measurePoint2 != null) {
                        g2d.drawLine((int) screenPoint1.getX(), (int) screenPoint1.getY(), (int) screenPoint2.getX(),
                                (int) screenPoint2.getY());
                        g2d.drawString(String.format("%.3f\"", measurePoint1.distance(measurePoint2)),
                                (int) (screenPoint1.getX() / 2 + screenPoint2.getX() / 2 + 10),
                                (int) (screenPoint1.getY() / 2 + screenPoint2.getY() / 2 - 10));
                    }
                }
                try {
                    switch (menuState) {
                        case HOME, MEASURE -> {
                            editMenu.setVisible(true);
                            editMenu.hideAMessage();
                        }
                        case CUT_SELECT -> {
                            cutPanel.setVisible(true);
                        }
                        case GCODE_SELECT -> gcodeCutPanel.setVisible(true);
                        case GCODE_SELECT_PART -> gcodePartPanel.setVisible(true);
                        case EMIT_SELECT -> {
                            if (!emitPanel.isVisible())
                                emitPanel.setVisible(true);
                        }
                        case ADD_CUT -> {
                            newcutPanel.setVisible(true);
                        }
                        case ADD_HOLE -> {
                            editMenu.setVisible(true);
                            editMenu.showAMessage();
                        }
                        case ADD_ITEM -> {
                            itemSelectMenu.setVisible(true);
                        }
                        default -> throw new IllegalStateException("State Not Possible: " + menuState);
                    }
                } catch (NullPointerException e) {
                    new WarningDialog(e, "No Active Cut", () -> switchMenuStates(HOME));
                }
            }
            case SHEET_ADD -> {

            }
        }
    }

    public void switchMenuStates(SheetMenuState newState) {
        menuState = newState;
        menuPanels.stream().forEach(e -> {
            e.setVisible(false);
        });
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && state == State.SHEET_EDIT) {
            if (menuState == MEASURE) {
                if (measurePoint1 == null) {
                    measurePoint1 = (Point2D.Double) screenToSheet(new Point2D.Double(e.getX(), e.getY()));
                } else if (measurePoint2 == null) {
                    measurePoint2 = (Point2D.Double) screenToSheet(new Point2D.Double(e.getX(), e.getY()));
                } else {
                    measurePoint1 = null;
                    measurePoint2 = null;
                }
            } else if (menuState == ADD_HOLE && aHeld) {
                Point2D.Double holePoint = actualScreenToSheet(e.getPoint());

                Hole hole = selectedSheet.addHole(holePoint.getX(), holePoint.getY());

                undoList.add(new EditAction(hole, true));
            } else if (menuState == ADD_ITEM && aHeld) {
                Point2D.Double partPoint = actualScreenToSheet(e.getPoint());

                Part part = selectedSheet.addPart(itemSelectMenu.partFileToPlace, partPoint.getX(), partPoint.getY());

                undoList.add(new EditAction(part, true));
            } else {
                if (selectSheet != null) {
                    Point2D grabLocation = screenToSheet(e.getPoint());
                    partBeingDragged = selectedSheet.contains(grabLocation);

                    if (ctrlPressed && rotationPoint == null) {
                        rotatingPart = true;
                        rotationPoint = grabLocation;
                    } else if (partBeingDragged != null) {
                        if (selectedPart != null && !selectedPart.equivalent(partBeingDragged)) {
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
                        if (selectedPart != null) {
                            selectedPart.setSelected(false);
                            selectedPart = null;
                        }
                        panning = true;
                    }
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
            if (menuState == HOME) {
                switchMenuStates(ADD_HOLE);
            } else if (menuState == ADD_HOLE) {
                switchMenuStates(HOME);
            }
            addHole.setSelected(menuState == ADD_HOLE);
        } else if (e.getSource() == addItem) {
            switchMenuStates(ADD_ITEM);
        } else if (e.getSource() == del) {
            deleteSelected.actionPerformed(e);
        } else if (e.getSource() == reScan) {
            for (Cut cut : selectedSheet.getCuts()) {
                for (Part part : cut.parts) {
                    part.reload();
                }
            }
        } else if (e.getSource() == emit) {
            switchMenuStates(EMIT_SELECT);
        } else if (e.getSource() == save) {
            saveSheet.actionPerformed(e);
        } else if (e.getSource() == addCut) {
            switchMenuStates(ADD_CUT);
        } else if (e.getSource() == newCutButton) {
            File temp = new File(selectedSheet.getParentFile().getPath() + "\\" + newCutField.getText() + ".cut");
            selectedSheet.addCut(new Cut(temp, selectedSheet.getHolesFile()));
            selectedSheet.changeActiveCutFile(temp);
            switchMenuStates(HOME);
        } else if (e.getSource() == changeGCodeView) {
            if (menuState == GCODE_SELECT) {
                switchMenuStates(HOME);
            } else {
                switchMenuStates(GCODE_SELECT);
            }
        } else if (e.getSource() == measure) {
            if (menuState == MEASURE) {
                switchMenuStates(HOME);
            } else {
                switchMenuStates(MEASURE);
            }
            measure.setSelected(menuState == MEASURE);
        } else if (e.getSource() == changeCut) {
            if (menuState == CUT_SELECT) {
                switchMenuStates(HOME);
            } else {
                switchMenuStates(CUT_SELECT);
            }
        } else if (e.getSource() instanceof ReturnToHomeJButton) {
            switchMenuStates(HOME);
        } else if (e.getSource() instanceof FileJRadioButton
                && ((FileJRadioButton) e.getSource()).getType().equals(CUT_SELECT)) {
            selectedSheet.changeActiveCutFile(((FileJRadioButton) e.getSource()).getFile());
            emitPanel = new EmitSelect();
            add(emitPanel);
            emitPanel.setBounds(editMenu.getBounds());
            menuPanels.add(emitPanel);
        } else if (e.getSource() instanceof SheetHandlerJButtonCut) {
            gcodePartPanel = new PartSelectGcode(((SheetHandlerJButtonCut) e.getSource()).getgenericThing());
            add(gcodePartPanel);
            gcodePartPanel.setBounds(editMenu.getBounds());
            menuPanels.add(gcodePartPanel);
            switchMenuStates(GCODE_SELECT_PART);
        } else if (e.getSource() instanceof SheetHandlerJButtonPart) {
            remove(gcodePartPanel);
            menuPanels.remove(gcodePartPanel);
            gcodePartPanel = new GCodeSelectGcode(((SheetHandlerJButtonPart) e.getSource()).getgenericThing(),
                    ((PartSelectGcode) gcodePartPanel).getCut());
            gcodePartPanel.setBounds(editMenu.getBounds());
            add(gcodePartPanel);
            menuPanels.add(gcodePartPanel);
        } else if (e.getSource() instanceof ReturnOnceJButton && menuState == GCODE_SELECT_PART) {
            ((Returnable) gcodePartPanel).returnTo();
        } else if (itemSelectMenu.fileButtons.contains(e.getSource())) {
            itemSelectMenu.selectFile(((ItemSelectMenu.FileJButton) e.getSource()).file());
        } else {
            for (JButton button : suffixes) {
                if (e.getSource() == button) {
                    File outputFolder = new File(
                            "./output/" + java.time.LocalDate.now().toString().replaceAll("-", ""));
                    outputFolder.mkdir();
                    File outputFile = new File(outputFolder, emitPanel.gCodeName.getText() + ".ngc");
                    selectedSheet.emitGCode(outputFile, button.getText());
                }
            }
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
     * Switch between program states
     * 
     * @param newState - the state to swtich to
     */
    private void switchStates(State newState) {
        menuPanels.stream().forEach(e -> {
            e.setVisible(false);
        });

        switch (newState) {
            case SHEET_SELECT -> {
                state = State.SHEET_SELECT;
                switchMenuStates(NULL);
                selectSheet.setVisible(true);
                addSheet.setVisible(true);
                sheetList.setVisible(true);
                sheetScroll.setVisible(true);
                newSheetPrompt.setVisible(false);
                returnToHome.setVisible(false);
            }
            case SHEET_EDIT -> {
                state = State.SHEET_EDIT;
                switchMenuStates(HOME);
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

                gcodeCutPanel = new CutSelectGcode();
                add(gcodeCutPanel);
                gcodeCutPanel.setBounds(editMenu.getBounds());
                menuPanels.add(gcodeCutPanel);

                if (selectedSheet.getActiveCut() != null) {
                    emitPanel = new EmitSelect();
                    add(emitPanel);
                    emitPanel.setBounds(editMenu.getBounds());
                    menuPanels.add(emitPanel);
                }

                cutPanel = new CutSelect();
                add(cutPanel);
                cutPanel.setBounds(editMenu.getBounds());
                menuPanels.add(cutPanel);

                menuPanels.stream().forEach(e -> e.setVisible(false));
            }
            case SHEET_ADD -> {
                state = State.SHEET_ADD;
                switchMenuStates(NULL);
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

    private Point2D.Double actualScreenToSheet(Point2D in) {
        Point2D.Double out = new Point2D.Double(in.getX(), in.getY());
        out.setLocation(out.getX() - xCorner * zoom, out.getY() - yCorner * zoom);
        out.setLocation(out.getX() / zoom, out.getY() / zoom);

        out.setLocation(out.getX() + selectedSheet.getWidth(), selectedSheet.getHeight() - out.getY());
        return out;

    }

    private Point2D sheetToScreen(Point2D in) {
        Point2D out = new Point2D.Double(in.getX(), in.getY());
        out.setLocation(out.getX() * zoom, out.getY() * zoom);
        out.setLocation(out.getX() + xCorner * zoom, out.getY() + yCorner * zoom);
        return out;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source instanceof SheetHandlerJCheckBoxNGCDoc) {
            SheetHandlerJCheckBoxNGCDoc thing = (SheetHandlerJCheckBoxNGCDoc) source;
            if (e.getStateChange() == ItemEvent.SELECTED) {
                thing.getPart().addActiveGcode(thing.getgenericThing());
            }
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                thing.getPart().removeActiveGcode(thing.getgenericThing());
            }
        }
        repaint();
    }

    private class SheetEditMenu extends JPanel {
        private JLabel buffer;

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
            c.gridy = 6;
            add(cutName, c);

            changeGCodeView = new JButton("Change GCode viewed");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = 8;
            c.gridwidth = 1;
            add(changeGCodeView, c);
            changeGCodeView.addActionListener(Screen.this);

            measure = new JButton("Measure");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 2;
            c.gridy = 3;
            add(measure, c);
            measure.addActionListener(Screen.this);

            changeCut = new JButton("Select Cut");
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.weightx = 0;
            c.gridy = 5;
            add(changeCut, c);
            changeCut.addActionListener(Screen.this);

            // bottom buffer
            buffer = new JLabel();
            buffer.setForeground(Color.WHITE);
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 7;
            c.weighty = 1;
            add(buffer, c);

            if (selectedSheet != null) {
                sheetName.setText("Editing Sheet: " + selectedSheet.getSheetFile().getName());
                if (selectedSheet.getActiveCutFile() != null) {
                    cutName.setText("Current Cut: " + selectedSheet.getActiveCutFile().getName());
                }
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (selectedSheet != null) {
                sheetName.setText("Editing Sheet: " + selectedSheet.getSheetFile().getName());
                if (selectedSheet.getActiveCutFile() != null) {
                    cutName.setText("Current Cut: " + selectedSheet.getActiveCutFile().getName());
                }
            }
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        public void showAMessage() {
            buffer.setText("Hold a to add holes");
        }

        public void hideAMessage() {
            buffer.setText("");
        }
    }

    private class CutSelectGcode extends JPanel {
        private CutSelectGcode() {
            setLayout(new GridLayout(0, 1));
            add(returnToHomeMenu.clone());
            add(new JLabel("Select Cut to Change: ") {
                {
                    setForeground(Color.WHITE);
                }
            });

            for (Cut cut : selectedSheet.getCuts()) {
                add(new SheetHandlerJButtonCut(cut.getCutFile().getName(), cut) {
                    {
                        addActionListener(Screen.this);
                    }
                });
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private class PartSelectGcode extends JPanel implements Returnable {
        private Cut cut;

        private PartSelectGcode(Cut cut) {
            this.cut = cut;
            setLayout(new GridLayout(0, 1));
            add(returnToHomeMenu.clone());
            add(returnOnce.clone());
            add(new JLabel("Select Part to Change: ") {
                {
                    setForeground(Color.WHITE);
                }
            });

            for (Part part : cut) {
                if (part instanceof Hole) {
                    continue;
                }
                add(new SheetHandlerJButtonPart(part.partFile().getName(), part) {
                    {
                        addActionListener(Screen.this);
                    }
                });
            }
        }

        private Cut getCut() {
            return cut;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public void returnTo() {
            switchMenuStates(GCODE_SELECT);
        }
    }

    private class GCodeSelectGcode extends JPanel implements Returnable {
        private Cut cut;

        private GCodeSelectGcode(Part part, Cut cut) {
            this.cut = cut;
            setLayout(new GridLayout(0, 1));
            add(returnToHomeMenu.clone());
            add(returnOnce.clone());
            add(new JLabel("Select GCode File to View: ") {
                {
                    setForeground(Color.WHITE);
                }
            });

            for (NGCDocument docs : part.getAllNgcDocuments()) {
                add(new SheetHandlerJCheckBoxNGCDoc(docs.getGcodeFile().getName(), docs, part) {
                    {
                        addItemListener(Screen.this);
                        if (part.getNgcDocuments().stream().anyMatch(e -> e.equals(docs))) {
                            setSelected(true);
                        }
                    }
                });
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        public Cut getCut() {
            return cut;
        }

        @Override
        public void returnTo() {
            returnToTemp();
        }
    }

    private void returnToTemp() {
        remove(gcodePartPanel);
        menuPanels.remove(gcodePartPanel);
        gcodePartPanel = new PartSelectGcode(((GCodeSelectGcode) gcodePartPanel).getCut());
        gcodePartPanel.setBounds(editMenu.getBounds());
        add(gcodePartPanel);
        menuPanels.add(gcodePartPanel);
    }

    private class CutSelect extends JPanel {
        private ButtonGroup buttons;

        private CutSelect() {
            setLayout(new GridLayout(0, 1));
            add(returnToHomeMenu.clone());
            add(new JLabel("Select Active Cut:") {
                {
                    setForeground(Color.WHITE);
                }
            });

            buttons = new ButtonGroup();
            for (File cutFile : selectedSheet.getParentFile().listFiles()) {
                if (!cutFile.getName().endsWith(".cut")) {
                    continue;
                }
                buttons.add(
                        new FileJRadioButton(cutFile.getName().substring(0, cutFile.getName().lastIndexOf(".cut")),
                                CUT_SELECT) {
                            {
                                addActionListener(Screen.this);
                                if (selectedSheet.getActiveCutFile() != null
                                        && cutFile.getName().equals(selectedSheet.getActiveCutFile().getName())) {
                                    setSelected(true);
                                }
                                setFile(cutFile);
                            }
                        });
            }
            Collections.list(buttons.getElements()).stream().forEach(e -> this.add(e));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, 800);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private class ReturnToHomeJButton extends JButton implements Cloneable {
        public ReturnToHomeJButton(String text) {
            super(text);
        }

        @Override
        public ReturnToHomeJButton clone() {
            return new ReturnToHomeJButton(this.getText()) {
                {
                    setBounds(this.getBounds());
                    addActionListener(Screen.this);
                }
            };
        }
    }

    private class ReturnOnceJButton extends JButton implements Cloneable {
        public ReturnOnceJButton(String text) {
            super(text);
        }

        @Override
        public ReturnOnceJButton clone() {
            return new ReturnOnceJButton(this.getText()) {
                {
                    setBounds(this.getBounds());
                    addActionListener(Screen.this);
                }
            };
        }
    }

    private class EmitSelect extends JPanel {
        public JTextField gCodeName;
        public JLabel gCodeNameLabel;

        public EmitSelect() {
            setLayout(null);

            HashSet<String> suffixStrings = new HashSet<>();
            for (Part part : selectedSheet.getActiveCut()) {
                for (String suffix : part.getSuffixes()) {
                    suffixStrings.add(suffix);
                }
            }

            suffixes = new JButton[suffixStrings.size()];
            for (int i = 0; i < suffixes.length; i++) {
                suffixes[i] = new JButton((String) (suffixStrings.toArray()[i]));
                suffixes[i].setBounds(50, 150 + 50 * i, 200, 25);
                add(suffixes[i]);
                suffixes[i].addActionListener(Screen.this);
                if (i == suffixes.length - 1) {
                    JButton returnToMain = new ReturnToHomeJButton("Done emitting gCode");
                    returnToMain.setBounds(50, 150 + 50 * (i + 1), 200, 50);
                    add(returnToMain);
                    returnToMain.addActionListener(Screen.this);
                }
            }

            gCodeName = new JTextField(selectedSheet.getActiveCut().getCutFile().getName().substring(0,
                    selectedSheet.getActiveCut().getCutFile().getName().lastIndexOf('.')));
            gCodeName.setBounds(50, 50, 200, 25);
            add(gCodeName);

            gCodeNameLabel = new JLabel("gCode emission name:");
            gCodeNameLabel.setBounds(50, 25, 200, 25);
            gCodeNameLabel.setForeground(Color.WHITE);
            add(gCodeNameLabel);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, 800);
        }
    }

    class NewCutMenu extends JPanel {
        public NewCutMenu() {
            setLayout(null);

            add(new JLabel("Adding new sheet cut. Enter filename:") {
                {
                    setBounds(10, 0, editMenu.getWidth(), 50);
                    setForeground(Color.WHITE);
                }
            });

            newCutField = new JTextField();
            newCutField.setBounds(10, 60, 200, 50);
            add(newCutField);

            newCutButton = new JButton("Add Sheet Cut");
            newCutButton.setBounds(10, 120, 200, 50);
            newCutButton.addActionListener(Screen.this);
            add(newCutButton);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    class ItemSelectMenu extends JPanel {
        public ArrayList<JComponent> fileButtons;
        public File partFileToPlace;

        public ItemSelectMenu() {
            setLayout(new GridLayout(0, 1, 10, 10));

            add(returnToHomeMenu.clone());

            fileButtons = new ArrayList<>();

            selectFile(new File("./parts_library"));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, 800);
        }

        /**
         * Goes to the subbordinate list or sets the selected file if it's not a
         * directory
         */
        public void selectFile(File file) {
            fileButtons.stream().forEach(e -> {
                remove(e);
                if (e instanceof JButton) {
                    ((JButton) e).removeActionListener(Screen.this);
                }
            });
            fileButtons.clear();
            if (!file.getName().equals("parts_library")) {
                FileJButton button = new FileJButton(file.getParentFile(), "..");
                fileButtons.add(button);
                add(button);
                button.addActionListener(Screen.this);
            }
            for (File subFile : file.listFiles()) {
                if (subFile.isDirectory()) {
                    FileJButton button = new FileJButton(subFile);
                    fileButtons.add(button);
                    add(button);
                    button.addActionListener(Screen.this);
                } else {
                    partFileToPlace = file;
                    JLabel holdA = new JLabel("Hold a and click to add parts");
                    add(holdA);
                    holdA.setForeground(Color.WHITE);
                    fileButtons.add(holdA);
                    validate();
                    return;
                }
            }
            partFileToPlace = null;
            validate();
            return;
        }

        class FileJButton extends JButton {
            private File file;

            public FileJButton(File file) {
                super(file.getName());
                this.file = file;
            }

            public FileJButton(File file, String name) {
                super(name);
                this.file = file;
            }

            public File file() {
                return file;
            }
        }
    }

    public enum SheetMenuState {
        NULL, HOME, MEASURE, CUT_SELECT, GCODE_SELECT, GCODE_SELECT_PART, EMIT_SELECT, ADD_ITEM, ADD_HOLE, ADD_CUT
    }

    class SheetHandlerJButtonCut extends SheetHandlerJButton<Cut> {
        public SheetHandlerJButtonCut(String text, Cut genericThing) {
            super(text, genericThing);
        }
    }

    class SheetHandlerJButtonPart extends SheetHandlerJButton<Part> {
        public SheetHandlerJButtonPart(String text, Part genericThing) {
            super(text, genericThing);
        }
    }

    class SheetHandlerJCheckBoxNGCDoc extends SheetHandlerJCheckBox<NGCDocument> {
        private Part part;

        public SheetHandlerJCheckBoxNGCDoc(String text, NGCDocument doc, Part part) {
            super(text, doc);
            this.part = part;
        }

        public Part getPart() {
            return part;
        }
    }
}
