package Display;

import SheetHandler.Part;

public class EditAction {
    private double xFrom;
    private double yFrom;
    private double rotFrom;
    private double xTo;
    private double yTo;
    private double rotTo;
    private Part partMoved;

    public EditAction(Part part, double _xFrom, double _yFrom, double _rotFrom, double _xTo, double _yTo, double _rotTo) {
        xFrom = _xFrom;
        yFrom = _yFrom;
        rotFrom = _rotFrom;
        xTo = _xTo;
        yTo = _yTo;
        rotTo = _rotTo;
        partMoved = part;
    }

    public EditAction(Part part, double _xFrom, double _yFrom, double _rotFrom) {
        this(part, _xFrom, _yFrom, _rotFrom, part.getX(), part.getY(), part.getRot());
    }

    public void undoAction() {
        partMoved.setX(xFrom);
        partMoved.setY(yFrom);
        partMoved.setRot(rotFrom);
    }

    public void redoAction() {
        partMoved.setX(xTo);
        partMoved.setY(yTo);
        partMoved.setRot(rotTo);
    }
}
