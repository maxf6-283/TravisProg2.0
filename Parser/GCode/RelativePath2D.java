package Parser.GCode;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Defines a geometric path with relative coordinates and arcs possible
 * 
 * @see Path2D
 * @see Path2D.Double
 */
public class RelativePath2D extends Path2D.Double {
    public Point3D getCurrentPoint3D() {
        return new Point3D(getCurrentPoint().getX(), getCurrentPoint().getY(), z);
    }

    private double z;
    private double xP, yP;

    public void setZ(double z) {
        this.z = z;
    }

    public void setZRelative(double deltaZ) {
        z += deltaZ;
    }

    /**
     * Adds an arc segment, defined by 2 points, by drawing an arc that intersects
     * {@code (x2,y2)}, using the specified point {@code (x1,y2)}
     * 
     * @param x1        the X-coordinate of the center of the arc
     * @param y1        the Y-coordinate of the center of the arc
     * @param x2        the X-coordinate of the final end point
     * @param y2        the Y-coordinate of the final end point
     * @param direction -1 = clockwise, 0 = nothing, 1 = counterclockwise, throws
     */
    public void arcTo(double x1, double y1, double x2, double y2, int direction) {
        // TODO fix arc to Bezier conversion(only works for <90 degrees)
        if (direction < -1 || direction > 1) {
            throw new IllegalArgumentException("Direction: " + direction + " is not in the range -1, 1");
        }
        double ax = getCurrentPoint().getX() - x1;
        double ay = getCurrentPoint().getY() - y1;
        double bx = x2 - x1;
        double by = y2 - y1;
        double q1 = ax * ax + ay * ay;
        double q2 = q1 + ax * bx + ay * by;
        double k2 = (4.0 / 3) * (Math.sqrt(2 * q1 * q2) - q2) / (ax * by - ay * bx);
        curveTo(x1 + ax - k2 * ay, y1 + ay + k2 * ax, x1 + bx + k2 * by, y1 + by - k2 * bx, x2, y2);
    }

    /**
     * Adds an arc segment, defined by 2 points, by drawing an arc that intersects
     * {@code (x2,y2)}, using the specified point {@code (x1,y2)}, that are all relative to the previous point
     * 
     * @param x1        the X-coordinate of the center of the arc
     * @param y1        the Y-coordinate of the center of the arc
     * @param x2        the X-coordinate of the final end point
     * @param y2        the Y-coordinate of the final end point
     * @param direction -1 = clockwise, 0 = nothing, 1 = counterclockwise, throws
     */
    public void arcToRelative(double x1, double y1, double x2, double y2, int direction) {
        arcTo(x1 + xP, y1 + xP, x2 + xP, y2 + yP, direction);
        setRelative(x2, y2);
    }

    private void setRelative(double x, double y) {
        xP = x;
        yP = y;
    }

    /**
     * Adds a point to the path by moving to the specified, relative
     * coordinates specified in double precision.
     *
     * @param x the specified X coordinate relative to the previous point
     * @param y the specified Y coordinate relative to the previous point
     */
    public void moveToRelative(double x, double y) {
        moveTo(x + xP, y + yP);
        setRelative(x, y);
    }

    /**
     * Adds a point to the path by drawing a straight line from the
     * current coordinates to the new specified, relative coordinates
     * specified in double precision.
     *
     * @param x the specified X coordinate relative to the previous point
     * @param y the specified Y coordinate relative to the previous point
     */
    public void lineToRelative(double x, double y) {
        lineTo(x + xP, y + yP);
        setRelative(x, y);
    }

    /**
     * Adds a curved segment, defined by two new points, relative to the previous
     * point, to the path by
     * drawing a Quadratic curve that intersects both the current
     * coordinates and the specified coordinates {@code (x2,y2)},
     * using the specified point {@code (x1,y1)} as a quadratic
     * parametric control point. Both points are relative to the previous point
     * All coordinates are specified in double precision.
     *
     * @param x1 the X coordinate of the quadratic control point relative to the
     *           previous point
     * @param y1 the Y coordinate of the quadratic control point relative to the
     *           previous point
     * @param x2 the X coordinate of the final end point relative to the previous
     *           point
     * @param y2 the Y coordinate of the final end point relative to the previous
     *           point
     */
    public void quadToRelative(double x1, double y1, double x2, double y2) {
        quadTo(x1 + xP, y1 + yP, x2 + xP, y2 + yP);
        setRelative(x2, y2);
    }

    /**
     * Adds a curved segment, defined by three new points, to the path by
     * drawing a B&eacute;zier curve that intersects both the current
     * coordinates and the specified coordinates {@code (x3,y3)},
     * using the specified points {@code (x1,y1)} and {@code (x2,y2)} as
     * B&eacute;zier control points. All points are relative to the previous point
     * All coordinates are specified in double precision.
     *
     * @param x1 the X coordinate of the first B&eacute;zier control point relative
     *           to the previous point
     * @param y1 the Y coordinate of the first B&eacute;zier control point relative
     *           to the previous point
     * @param x2 the X coordinate of the second B&eacute;zier control point relative
     *           to the previous point
     * @param y2 the Y coordinate of the second B&eacute;zier control point relative
     *           to the previous point
     * @param x3 the X coordinate of the final end point relative to the previous
     *           point
     * @param y3 the Y coordinate of the final end point relative to the previous
     *           point
     */
    public void curveToRelative(double x1, double y1, double x2, double y2, double x3, double y3) {
        curveTo(x1 + xP, y1 + yP, x2 + xP, y2 + yP, x3 + xP, y3 + yP);
        setRelative(x3, y3);
    }
}

class Point3D extends Point2D.Double {
    private double z;

    public Point3D(double x, double y, double z) {
        super(x, y);
        this.z = z;
    }

    public double getZ() {
        return z;
    }

    public void setLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    /**
     * Returns a {@code String} that represents the value
     * of this {@code Point3D}.
     * 
     * @return a string representation of this {@code Point3D}.
     */
    public String toString() {
        return "Point3D[" + x + ", " + y + ", " + z + "]";
    }
}
