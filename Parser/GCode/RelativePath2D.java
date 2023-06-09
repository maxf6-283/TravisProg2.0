package Parser.GCode;

import Display.Screen;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Defines a geometric path with relative coordinates and arcs possible
 * 
 * @see Path2D
 * @see Path2D.Double
 */
public class RelativePath2D extends Path2D.Double {
    public Point3D getCurrentPoint3D() {
        if (getCurrentPoint() == null) {
            return null;
        }
        return new Point3D(getCurrentPoint().getX(), getCurrentPoint().getY(), z);
    }

    private double z;
    private double xP, yP;
    private boolean offsetLeft = false;
    private boolean offsetRight = false;

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
     * @param x1            the X-coordinate of the center of the arc
     * @param y1            the Y-coordinate of the center of the arc
     * @param x2            the X-coordinate of the final end point
     * @param y2            the Y-coordinate of the final end point
     * @param direction     -1 = clockwise, 0 = nothing, 1 = counterclockwise,
     *                      throws
     * @param isRelative    whether or not x2 and y2 are relative
     * @param isRelativeArc whether or not x1 and y1 are relative
     */
    public void arcTo(double x1, double y1, double x2, double y2, int direction, boolean isRelative,
            boolean isRelativeArc) {
        if (direction < -1 || direction > 1) {
            throw new IllegalArgumentException("Direction: " + direction + " is not in the range -1, 1");
        }

        direction *= -1;
        double startX = getCurrentPoint().getX();
        double startY = getCurrentPoint().getY();

        double centerX = x1;
        double centerY = y1;

        if (isRelativeArc) {
            centerX += startX;
            centerY += startY;
        }

        double endX = x2;
        double endY = y2;

        if (isRelative) {
            endX += startX;
            endY += startY;
        }

        if (Screen.DebugMode) {
            System.out.printf("Arguments: x1: %f, y1: %f, x2: %f, y2: %f%n", x1, y1, x2, y2);
            System.out.printf("Arcing from %f, %f to %f, %f around %f, %f%n", startX, startY, endX, endY, centerX,
                    centerY);
        }

        double startingAngle = Math.atan((startY - centerY) / (startX - centerX));
        if (startX - centerX < 0) {
            startingAngle += Math.PI;
        }

        double endingAngle = Math.atan((endY - centerY) / (endX - centerX));
        if (endX - centerX < 0) {
            endingAngle += Math.PI;
        }
        while (endingAngle * direction < startingAngle * direction) {
            endingAngle += Math.PI * 2 * direction;
        }

        double radius = Math.sqrt((startX - centerX) * (startX - centerX) + (startY - centerY) * (startY - centerY));
        double radius2 = Math.sqrt((endX - centerX) * (endX - centerX) + (endY - centerY) * (endY - centerY));

        if (Screen.DebugMode) {
            System.out.printf("Radius is %f, starting angle is %f, ending angle is %f%n", radius, startingAngle,
                    endingAngle);
        }

        if (Math.abs(radius - radius2) > 0.05
                || (Math.abs(radius - radius2) > 0.05 && Math.abs(radius - radius2) > (radius + radius2) / 2 * 0.001)) {
            throw new IllegalGCodeError("Arc is not defined to have a self-similar radius");
        } else {
            radius = radius / 2 + radius2 / 2;
            double angle = startingAngle;

            double incrVal = Math.abs(startingAngle - endingAngle) / 10;

            while (angle * direction < endingAngle * direction) {
                angle += incrVal * direction;
                if (angle * direction < endingAngle * direction)
                    lineTo(centerX + radius * Math.cos(angle), centerY + radius * Math.sin(angle));
            }
        }
        lineTo(endX, endY);
        setRelative(endX, endY);
    }

    public void setRelative(double x, double y) {
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
    @Deprecated
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
    @Deprecated
    public void curveToRelative(double x1, double y1, double x2, double y2, double x3, double y3) {
        curveTo(x1 + xP, y1 + yP, x2 + xP, y2 + yP, x3 + xP, y3 + yP);
        setRelative(x3, y3);
    }

    public void offsetRight() {
        if (!offsetLeft) {
            offsetRight = true;
        } else {
            throw new IllegalArgumentException("cannot offset in two directions at once");
        }
    }

    public void offsetLeft() {
        if (!offsetRight) {
            offsetLeft = true;
        } else {
            throw new IllegalArgumentException("cannot offset in two directions at once");
        }
    }

    public RelativePath2D getOffsetInstance(double offset) {
        // check if the path is only made of lines(only offset supported)
        PathIterator holder = getPathIterator(null);
        // stores the x,y pairs of the continous polygon
        ArrayList<java.lang.Double[]> coords = new ArrayList<>();
        ArrayList<Integer> type = new ArrayList<>();
        // adds polygon points as well as checking that it is one
        while (!holder.isDone()) {
            double[] temp = new double[2];
            // may need to change this to get rid of moveTos(assumption that they are part
            // of the polygon)
            if (holder.currentSegment(temp) != PathIterator.SEG_LINETO
                    && holder.currentSegment(temp) != PathIterator.SEG_MOVETO
                    && holder.currentSegment(temp) != PathIterator.SEG_CLOSE) {
                throw new IllegalArgumentException("Path Iterator must only contain lines");
            }
            // TODO remove repeated points(maybe do in moveto/lineto too)
            coords.add(getAsWrapper(temp));
            type.add(holder.currentSegment(temp));
            holder.next();
        }

        // not enough vertices
        if (coords.size() < 3) {
            return this;
        }

        // holds slope of the normal of each point(same order as coords)
        ArrayList<java.lang.Double> slopeNorm = new ArrayList<>();

        // double[] holds m and then b of parallel line(starts with edge of the first
        // and second coords)
        ArrayList<java.lang.Double[]> offsetLineEq = new ArrayList<>();

        // finds all slopes of the normal line of the subgradient of each point
        // also finds equation for each parallel line with specified offset
        for (int i = 0; i < coords.size(); i++) {
            if (i == 0) {
                slopeNorm.add(getMidNormSlope(getAsPrimitive(coords.get(coords.size() - 1)),
                        getAsPrimitive(coords.get(i)), getAsPrimitive(coords.get(i + 1))));
                offsetLineEq.add(getAsWrapper(
                        getOffsetLineInfo(getAsPrimitive(coords.get(i)), getAsPrimitive(coords.get(i + 1)), offset)));
            } else if (i == coords.size() - 1) {
                slopeNorm.add(getMidNormSlope(getAsPrimitive(coords.get(i - 1)), getAsPrimitive(coords.get(i)),
                        getAsPrimitive(coords.get(0))));
                offsetLineEq.add(getAsWrapper(
                        getOffsetLineInfo(getAsPrimitive(coords.get(i)), getAsPrimitive(coords.get(0)), offset)));
            } else {
                slopeNorm.add(getMidNormSlope(getAsPrimitive(coords.get(i - 1)), getAsPrimitive(coords.get(i)),
                        getAsPrimitive(coords.get(i + 1))));
                offsetLineEq.add(getAsWrapper(
                        getOffsetLineInfo(getAsPrimitive(coords.get(i)), getAsPrimitive(coords.get(i + 1)), offset)));
            }

        }

        // TODO remove disappearing sides
        for (int i = 0; i < coords.size(); i++) {

        }

        RelativePath2D output = new RelativePath2D();

        // calculates new points and puts it into a new RelativePath2D
        // TODO do calcs
        for (int i = 0; i < coords.size(); i++) {
            // normal slope stuff
            double b_1 = -slopeNorm.get(i) * coords.get(i)[0] + coords.get(i)[1];

            double x = (offsetLineEq.get(i)[1] - b_1) / (slopeNorm.get(i) - offsetLineEq.get(i)[0]);

            double y = offsetLineEq.get(i)[0] * x + offsetLineEq.get(i)[1];

            if (type.get(i) == PathIterator.SEG_MOVETO) {
                output.moveTo(x, y);
            } else if (type.get(i) == PathIterator.SEG_LINETO) {
                output.lineTo(x, y);
            } else {
                System.out.println(type.get(i));
            }
        }
        output.closePath();

        return output;
    }

    private double[] getOffsetLineInfo(double[] p1, double[] p2, double offset) {
        double[] output = new double[2];
        output[0] = getSlope(p1, p2);
        // angle of the line
        double theta = Math.atan(output[0]);
        // gets b of the line
        output[1] = p1[1] - output[0] * p1[0];

        // positive delta y(from p1 to p2) with positive slope = positive c(offset
        // left), else negative c(for offset left)
        // negative delta y(from p1 to p2) with negative slope = positive c(offset
        // left), else negative c(for offset left)
        // edge case for slope of zero
        double deltaY = p2[1] - p1[1];
        // adds offset to the b, compensated for line angle
        if (deltaY * output[0] > 0) {
            if (offsetLeft) {
                output[1] += offset / Math.cos(theta);
            } else {
                output[1] -= offset / Math.cos(theta);
            }
        } else if (deltaY * output[0] < 0) { // offsetRight, c is negative
            if (offsetLeft) {
                output[1] -= offset / Math.cos(theta);
            } else {
                output[1] += offset / Math.cos(theta);
            }
        } else if (output[0] == 0) {// slope of zero(edge case)
            // TODO finish
        } else if (output[0] == java.lang.Double.POSITIVE_INFINITY || output[0] == java.lang.Double.NEGATIVE_INFINITY) {// vertical
                                                                                                                        // line(edge
                                                                                                                        // case)
            // TODO finish
        } else if (deltaY == 0) {
            // do nothing?(same point i think)
        } else {
            throw new IllegalStateException(
                    "slope = " + output[0] + ", deltaY = " + deltaY + ", does not work for some reason");
        }

        return output;
    }

    private java.lang.Double[] getAsWrapper(double[] arr) {
        return Arrays.stream(arr).boxed().toArray(java.lang.Double[]::new);
    }

    private double[] getAsPrimitive(java.lang.Double[] arr) {
        return Arrays.stream(arr).mapToDouble(java.lang.Double::doubleValue).toArray();
    }

    public RelativePath2D getOffsetInstance2(double offset) {

        PathIterator holder = getPathIterator(null);
        // stores the start and endpoints of the lines
        ArrayList<ArrayList<double[]>> paths = new ArrayList<>();

        while (!holder.isDone()) {
            double[] pointLocation = new double[6];
            int type = holder.currentSegment(pointLocation);
            holder.next();
            if (type == PathIterator.SEG_MOVETO) {
                // remove the last line with a trailing thing
                if (paths.size() > 0)
                    paths.get(paths.size() - 1).remove(paths.get(paths.size() - 1).size() - 1);
                ArrayList<double[]> currentPath = new ArrayList<>();
                paths.add(currentPath);
                currentPath.add(new double[4]);
                currentPath.get(0)[0] = pointLocation[0];
                currentPath.get(0)[1] = pointLocation[1];
            } else if (type == PathIterator.SEG_LINETO) {
                ArrayList<double[]> currentPath = paths.get(paths.size() - 1);
                currentPath.get(currentPath.size() - 1)[2] = pointLocation[0];
                currentPath.get(currentPath.size() - 1)[3] = pointLocation[1];
                currentPath.add(new double[4]);
                currentPath.get(currentPath.size() - 1)[0] = pointLocation[0];
                currentPath.get(currentPath.size() - 1)[1] = pointLocation[1];
            }
        }
        paths.get(paths.size() - 1).remove(paths.get(paths.size() - 1).size() - 1);
        //remove empty paths
        paths.removeIf(e -> e.size() == 0);

        // offset lines to the left/right
        // offset is negative to the left, positive to the right
        if (offsetLeft) {
            offset = -offset;
        } else if (!offsetRight) {
            offset = 0;
        }
        
        // move points in tangent direction to line
        
        for (ArrayList<double[]> lines : paths) {
            for (double[] line : lines) {
                // calculate offset
                double pOffsetX = line[2] - line[0];
                double pOffsetY = line[3] - line[1];

                // normalize
                double mag = Math.sqrt(pOffsetX * pOffsetX + pOffsetY * pOffsetY);
                pOffsetX /= mag;
                pOffsetY /= mag;

                // rotate 90 degrees to the right
                double temp = pOffsetX;
                pOffsetX = pOffsetY;
                pOffsetY = -temp;

                // multiply by offset
                pOffsetX *= offset;
                pOffsetY *= offset;

                // move points
                line[0] += pOffsetX;
                line[1] += pOffsetY;
                line[2] += pOffsetX;
                line[3] += pOffsetY;
            }
        }

        // extend/retract lines to each other
        // i refers to the ith point in the line (0-indexed)
        for (ArrayList<double[]> lines : paths) {
            for (int i = 1; i < lines.size(); i++) {
                // fine I'll find slopes
                if (lines.get(i - 1)[0] == lines.get(i - 1)[2]) {
                    // line 1 is vertical
                    if (lines.get(i)[0] == lines.get(i)[2]) {
                        // the lines are parallel, leave the point where it is
                        continue;
                    }

                    double line2Slope = (lines.get(i)[1] - lines.get(i)[3]) / (lines.get(i)[0] - lines.get(i)[2]);
                    double line2Offset = lines.get(i)[1] - lines.get(i)[0] * line2Slope;

                    // get the intersection point
                    double pX = lines.get(i - 1)[0];
                    double pY = line2Slope * pX + line2Offset;

                    // actually set the line positions
                    lines.get(i - 1)[2] = pX;
                    lines.get(i - 1)[3] = pY;
                    lines.get(i)[0] = pX;
                    lines.get(i)[1] = pY;
                    continue;
                }
                if (lines.get(i)[0] == lines.get(i)[2]) {
                    // line 2 is vertical
                    double line1Slope = (lines.get(i - 1)[1] - lines.get(i - 1)[3])
                            / (lines.get(i - 1)[0] - lines.get(i - 1)[2]);
                    double line1Offset = lines.get(i - 1)[1] - lines.get(i - 1)[0] * line1Slope;

                    // get the intersection point
                    double pX = lines.get(i - 1)[0];
                    double pY = line1Slope * pX + line1Offset;

                    // actually set the line positions
                    lines.get(i - 1)[2] = pX;
                    lines.get(i - 1)[3] = pY;
                    lines.get(i)[0] = pX;
                    lines.get(i)[1] = pY;
                    continue;
                }
                double line1Slope = (lines.get(i - 1)[1] - lines.get(i - 1)[3])
                        / (lines.get(i - 1)[0] - lines.get(i - 1)[2]);
                double line1Offset = lines.get(i - 1)[1] - lines.get(i - 1)[0] * line1Slope;
                double line2Slope = (lines.get(i)[1] - lines.get(i)[3]) / (lines.get(i)[0] - lines.get(i)[2]);
                double line2Offset = lines.get(i)[1] - lines.get(i)[0] * line2Slope;

                if (line1Slope == line2Slope) {
                    // the lines are parallel, leave the point where it is
                    continue;
                }

                // get the intersection point
                double pX = (line2Offset - line1Offset) / (line1Slope - line2Slope);
                double pY = line1Slope * pX + line1Offset;

                // actually set the line positions
                lines.get(i - 1)[2] = pX;
                lines.get(i - 1)[3] = pY;
                lines.get(i)[0] = pX;
                lines.get(i)[1] = pY;
            }
        }
        

        // create the new polygon
        RelativePath2D offsetPath = new RelativePath2D();
        for (ArrayList<double[]> lines : paths) {
            offsetPath.moveTo(lines.get(0)[0], lines.get(0)[1]);
            for (double[] line : lines) {
                offsetPath.lineTo(line[2], line[3]);
            }
        }

        return offsetPath;
    }

    /**
     * @param leftP
     * @param midP
     * @param rightP
     * @return returns normal slope of the middle of the subgradient of the point
     *         midP
     */
    private double getMidNormSlope(double[] leftP, double[] midP, double[] rightP) {
        return -2 / (getSlope(leftP, midP) + getSlope(midP, rightP));
    }

    /**
     * @param p1 first point
     * @param p2 second point
     * @return slope of the line that fits these two points
     */
    private double getSlope(double[] p1, double[] p2) {
        return (p2[1] - p1[1]) / (p2[0] - p1[0]);
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
