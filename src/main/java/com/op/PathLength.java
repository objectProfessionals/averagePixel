package com.op;

/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilitiy class for length calculations of paths.
 * <p>
 *   PathLength is a utility class for calculating the length
 *   of a path, the location of a point at a particular length
 *   along the path, and the angle of the tangent to the path
 *   at a given length.
 * </p>
 * <p>
 *   It uses a FlatteningPathIterator to create a flattened version
 *   of the Path. This means the values returned are not always
 *   exact (in fact, they rarely are), but in most cases they
 *   are reasonably accurate.
 * </p>
 *
 * @author <a href="mailto:dean.jackson@cmis.csiro.au">Dean Jackson</a>
 * @version $Id: PathLength.java 489226 2006-12-21 00:05:36Z cam $
 */
public class PathLength {

    /**
     * The path to use for calculations.
     */
    protected Shape path;

    /**
     * The list of flattened path segments.
     */
    protected List segments;

    /**
     * Array where the index is the index of the original path segment
     * and the value is the index of the first of the flattened segments
     * in {@link #segments} that corresponds to that original path segment.
     */
    protected int[] segmentIndexes;

    /**
     * Cached copy of the path length.
     */
    protected float pathLength;

    /**
     * Whether this path been flattened yet.
     */
    protected boolean initialised;

    /**
     * Creates a new PathLength object for the specified {@link Shape}.
     * @param path The Path (or Shape) to use.
     */
    public PathLength(Shape path) {
        setPath(path);
    }

    /**
     * Returns the path to use for calculations.
     * @return Path used in calculations.
     */
    public Shape getPath() {
        return path;
    }

    /**
     * Sets the path to use for calculations.
     * @param v Path to be used in calculations.
     */
    public void setPath(Shape v) {
        this.path = v;
        initialised = false;
    }

    /**
     * Returns the length of the path used by this PathLength object.
     * @return The length of the path.
     */
    public float lengthOfPath() {
        if (!initialised) {
            initialise();
        }
        return pathLength;
    }

    /**
     * Flattens the path and determines the path length.
     */
    protected void initialise() {
        pathLength = 0f;

        PathIterator pi = path.getPathIterator(new AffineTransform());
        SingleSegmentPathIterator sspi = new SingleSegmentPathIterator();
        segments = new ArrayList(20);
        List indexes = new ArrayList(20);
        int index = 0;
        int origIndex = -1;
        float lastMoveX = 0f;
        float lastMoveY = 0f;
        float currentX = 0f;
        float currentY = 0f;
        float[] seg = new float[6];
        int segType;

        segments.add(new PathSegment(PathIterator.SEG_MOVETO, 0f, 0f, 0f,
                origIndex));

        while (!pi.isDone()) {
            origIndex++;
            indexes.add(new Integer(index));
            segType = pi.currentSegment(seg);
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    segments.add(new PathSegment(segType, seg[0], seg[1],
                            pathLength, origIndex));
                    currentX = seg[0];
                    currentY = seg[1];
                    lastMoveX = currentX;
                    lastMoveY = currentY;
                    index++;
                    pi.next();
                    break;
                case PathIterator.SEG_LINETO:
                    pathLength += Point2D.distance(currentX, currentY, seg[0],
                            seg[1]);
                    segments.add(new PathSegment(segType, seg[0], seg[1],
                            pathLength, origIndex));
                    currentX = seg[0];
                    currentY = seg[1];
                    index++;
                    pi.next();
                    break;
                case PathIterator.SEG_CLOSE:
                    pathLength += Point2D.distance(currentX, currentY,
                            lastMoveX, lastMoveY);
                    segments.add(new PathSegment(PathIterator.SEG_LINETO,
                            lastMoveX, lastMoveY,
                            pathLength, origIndex));
                    currentX = lastMoveX;
                    currentY = lastMoveY;
                    index++;
                    pi.next();
                    break;
                default:
                    sspi.setPathIterator(pi, currentX, currentY);
                    FlatteningPathIterator fpi =
                            new FlatteningPathIterator(sspi, 0.01f);
                    while (!fpi.isDone()) {
                        segType = fpi.currentSegment(seg);
                        if (segType == PathIterator.SEG_LINETO) {
                            pathLength += Point2D.distance(currentX, currentY,
                                    seg[0], seg[1]);
                            segments.add(new PathSegment(segType, seg[0],
                                    seg[1], pathLength,
                                    origIndex));
                            currentX = seg[0];
                            currentY = seg[1];
                            index++;
                        }
                        fpi.next();
                    }
            }
        }
        segmentIndexes = new int[indexes.size()];
        for (int i = 0; i < segmentIndexes.length; i++) {
            segmentIndexes[i] = ((Integer) indexes.get(i)).intValue();
        }
        initialised = true;
    }

    /**
     * Returns the number of segments in the path.
     */
    public int getNumberOfSegments() {
        if (!initialised) {
            initialise();
        }
        return segmentIndexes.length;
    }

    /**
     * Returns the length at the start of the segment given by the specified
     * index.
     */
    public float getLengthAtSegment(int index) {
        if (!initialised) {
            initialise();
        }
        if (index <= 0) {
            return 0;
        }
        if (index >= segmentIndexes.length) {
            return pathLength;
        }
        PathSegment seg = (PathSegment) segments.get(segmentIndexes[index]);
        return seg.getLength();
    }

    /**
     * Returns the index of the segment at the given distance along the path.
     */
    public int segmentAtLength(float length) {
        int upperIndex = findUpperIndex(length);
        if (upperIndex == -1) {
            // Length is off the end of the path.
            return -1;
        }

        if (upperIndex == 0) {
            // Length was probably zero, so return the upper segment.
            PathSegment upper = (PathSegment) segments.get(upperIndex);
            return upper.getIndex();
        }

        PathSegment lower = (PathSegment) segments.get(upperIndex - 1);
        return lower.getIndex();
    }

    /**
     * Returns the point that is the given proportion along the path segment
     * given by the specified index.
     */
    public Point2D pointAtLength(int index, float proportion) {
        if (!initialised) {
            initialise();
        }
        if (index < 0 || index >= segmentIndexes.length) {
            return null;
        }
        PathSegment seg = (PathSegment) segments.get(segmentIndexes[index]);
        float start = seg.getLength();
        float end;
        if (index == segmentIndexes.length - 1) {
            end = pathLength;
        } else {
            seg = (PathSegment) segments.get(segmentIndexes[index + 1]);
            end = seg.getLength();
        }
        return pointAtLength(start + (end - start) * proportion);
    }

    /**
     * Returns the point that is at the given length along the path.
     * @param length The length along the path
     * @return The point at the given length
     */
    public Point2D pointAtLength(float length) {
        int upperIndex = findUpperIndex(length);
        if (upperIndex == -1) {
            // Length is off the end of the path.
            return null;
        }

        PathSegment upper = (PathSegment) segments.get(upperIndex);

        if (upperIndex == 0) {
            // Length was probably zero, so return the upper point.
            return new Point2D.Float(upper.getX(), upper.getY());
        }

        PathSegment lower = (PathSegment) segments.get(upperIndex - 1);

        // Now work out where along the line would be the length.
        float offset = length - lower.getLength();

        // Compute the slope.
        double theta = Math.atan2(upper.getY() - lower.getY(),
                upper.getX() - lower.getX());

        float xPoint = (float) (lower.getX() + offset * Math.cos(theta));
        float yPoint = (float) (lower.getY() + offset * Math.sin(theta));

        return new Point2D.Float(xPoint, yPoint);
    }

    /**
     * Returns the slope of the path at the specified length.
     * @param index The segment number
     * @param proportion The proportion along the given segment
     * @return the angle in radians, in the range [-{@link Math#PI},
     *         {@link Math#PI}].
     */
    public float angleAtLength(int index, float proportion) {
        if (!initialised) {
            initialise();
        }
        if (index < 0 || index >= segmentIndexes.length) {
            return 0f;
        }
        PathSegment seg = (PathSegment) segments.get(segmentIndexes[index]);
        float start = seg.getLength();
        float end;
        if (index == segmentIndexes.length - 1) {
            end = pathLength;
        } else {
            seg = (PathSegment) segments.get(segmentIndexes[index + 1]);
            end = seg.getLength();
        }
        return angleAtLength(start + (end - start) * proportion);
    }

    /**
     * Returns the slope of the path at the specified length.
     * @param length The length along the path
     * @return the angle in radians, in the range [-{@link Math#PI},
     *         {@link Math#PI}].
     */
    public float angleAtLength(float length) {
        int upperIndex = findUpperIndex(length);
        if (upperIndex == -1) {
            // Length is off the end of the path.
            return 0f;
        }

        PathSegment upper = (PathSegment) segments.get(upperIndex);

        if (upperIndex == 0) {
            // Length was probably zero, so return the angle between the first
            // and second segments.
            upperIndex = 1;
        }

        PathSegment lower = (PathSegment) segments.get(upperIndex - 1);

        // Compute the slope.
        return (float) Math.atan2(upper.getY() - lower.getY(),
                upper.getX() - lower.getX());
    }

    /**
     * Returns the index of the path segment that bounds the specified
     * length along the path.
     * @param length The length along the path
     * @return The path segment index, or -1 if there is not such segment
     */
    public int findUpperIndex(float length) {
        if (!initialised) {
            initialise();
        }

        if (length < 0 || length > pathLength) {
            // Length is outside the path, so return -1.
            return -1;
        }

        // Find the two segments that are each side of the length.
        int lb = 0;
        int ub = segments.size() - 1;
        while (lb != ub) {
            int curr = (lb + ub) >> 1;
            PathSegment ps = (PathSegment) segments.get(curr);
            if (ps.getLength() >= length) {
                ub = curr;
            } else {
                lb = curr + 1;
            }
        }
        for (;;) {
            PathSegment ps = (PathSegment) segments.get(ub);
            if (ps.getSegType() != PathIterator.SEG_MOVETO
                    || ub == segments.size() - 1) {
                break;
            }
            ub++;
        }

        int upperIndex = -1;
        int currentIndex = 0;
        int numSegments = segments.size();
        while (upperIndex <= 0 && currentIndex < numSegments) {
            PathSegment ps = (PathSegment) segments.get(currentIndex);
            if (ps.getLength() >= length
                    && ps.getSegType() != PathIterator.SEG_MOVETO) {
                upperIndex = currentIndex;
            }
            currentIndex++;
        }
        return upperIndex;
    }

    /**
     * A {@link PathIterator} that returns only the next path segment from
     * another {@link PathIterator}.
     */
    protected static class SingleSegmentPathIterator implements PathIterator {

        /**
         * The path iterator being wrapped.
         */
        protected PathIterator it;

        /**
         * Whether the single segment has been passed.
         */
        protected boolean done;

        /**
         * Whether the generated move command has been returned.
         */
        protected boolean moveDone;

        /**
         * The x coordinate of the next move command.
         */
        protected double x;

        /**
         * The y coordinate of the next move command.
         */
        protected double y;

        /**
         * Sets the path iterator to use and the initial SEG_MOVETO command
         * to return before it.
         */
        public void setPathIterator(PathIterator it, double x, double y) {
            this.it = it;
            this.x = x;
            this.y = y;
            done = false;
            moveDone = false;
        }

        public int currentSegment(double[] coords) {
            int type = it.currentSegment(coords);
            if (!moveDone) {
                coords[0] = x;
                coords[1] = y;
                return SEG_MOVETO;
            }
            return type;
        }

        public int currentSegment(float[] coords) {
            int type = it.currentSegment(coords);
            if (!moveDone) {
                coords[0] = (float) x;
                coords[1] = (float) y;
                return SEG_MOVETO;
            }
            return type;
        }

        public int getWindingRule() {
            return it.getWindingRule();
        }

        public boolean isDone() {
            return done || it.isDone();
        }

        public void next() {
            if (!done) {
                if (!moveDone) {
                    moveDone = true;
                } else {
                    it.next();
                    done = true;
                }
            }
        }
    }

    /**
     * A single path segment in the flattened version of the path.
     * This is a local helper class. PathSegment-objects are stored in
     * the {@link PathLength#segments} - list.
     * This is used as an immutable value-object.
     */
    protected static class PathSegment {

        /**
         * The path segment type.
         */
        protected final int segType;

        /**
         * The x coordinate of the path segment.
         */
        protected float x;

        /**
         * The y coordinate of the path segment.
         */
        protected float y;

        /**
         * The length of the path segment, accumulated from the start.
         */
        protected float length;

        /**
         * The index of the original path segment this flattened segment is a
         * part of.
         */
        protected int index;

        /**
         * Creates a new PathSegment with the specified parameters.
         * @param segType The segment type
         * @param x The x coordinate
         * @param y The y coordinate
         * @param len The segment length
         * @param idx The index of the original path segment this flattened
         *            segment is a part of
         */
        PathSegment(int segType, float x, float y, float len, int idx) {
            this.segType = segType;
            this.x = x;
            this.y = y;
            this.length = len;
            this.index = idx;
        }

        /**
         * Returns the segment type.
         */
        public int getSegType() {
            return segType;
        }

        /**
         * Returns the x coordinate of the path segment.
         */
        public float getX() {
            return x;
        }

        /**
         * Sets the x coordinate of the path segment.
         */
        public void setX(float v) {
            x = v;
        }

        /**
         * Returns the y coordinate of the path segment.
         */
        public float getY() {
            return y;
        }

        /**
         * Sets the y coordinate of the path segment.
         */
        public void setY(float v) {
            y = v;
        }

        /**
         * Returns the length of the path segment.
         */
        public float getLength() {
            return length;
        }

        /**
         * Sets the length of the path segment.
         */
        public void setLength(float v) {
            length = v;
        }

        /**
         * Returns the segment index.
         */
        public int getIndex() {
            return index;
        }

        /**
         * Sets the segment index.
         */
        public void setIndex(int v) {
            index = v;
        }
    }
}


/*

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/


/**
 * An interface that path segments must implement.
 *
 * @version $Id: Segment.java 478249 2006-11-22 17:29:37Z dvholten $
 */
interface Segment extends Cloneable {
    double minX();
    double maxX();
    double minY();
    double maxY();
    Rectangle2D getBounds2D();

    Point2D.Double evalDt(double t);
    Point2D.Double eval(double t);

    Segment getSegment(double t0, double t1);
    Segment splitBefore(double t);
    Segment splitAfter(double t);
    void    subdivide(Segment s0, Segment s1);
    void    subdivide(double t, Segment s0, Segment s1);
    double  getLength();
    double  getLength(double maxErr);

    SplitResults split(double y);

    class SplitResults {
        Segment [] above;
        Segment [] below;
        SplitResults(Segment []below, Segment []above) {
            this.below = below;
            this.above = above;
        }

        Segment [] getBelow() {
            return below;
        }
        Segment [] getAbove() {
            return above;
        }
    }
}

/*

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/


/**
 * An abstract class for path segments.
 *
 * @version $Id: AbstractSegment.java 478249 2006-11-22 17:29:37Z dvholten $
 */
abstract class AbstractSegment implements Segment {

    protected abstract int findRoots(double y, double [] roots);

    public Segment.SplitResults split(double y) {
        double [] roots = { 0, 0, 0 };
        int numSol = findRoots(y, roots);
        if (numSol == 0) return null; // No split

        Arrays.sort(roots, 0, numSol);
        double [] segs = new double[numSol+2];
        int numSegments=0;
        segs[numSegments++] = 0;
        for (int i=0; i<numSol; i++) {
            double r = roots[i];
            if (r <= 0.0) continue;
            if (r >= 1.0) break;
            if (segs[numSegments-1] != r)
                segs[numSegments++] = r;
        }
        segs[numSegments++] = 1.0;

        if (numSegments == 2) return null;
        // System.err.println("Y: " + y + "#Seg: " + numSegments +
        //                    " Seg: " + this);

        Segment [] parts = new Segment[numSegments];
        double pT = 0.0;
        int pIdx = 0;
        boolean firstAbove=false, prevAbove=false;
        for (int i=1; i<numSegments; i++) {
            // System.err.println("Segs: " + segs[i-1]+", "+segs[i]);
            parts[pIdx] = getSegment(segs[i-1], segs[i]);
            Point2D.Double pt = parts[pIdx].eval(0.5);
            // System.err.println("Pt: " + pt);
            if (pIdx == 0) {
                pIdx++;
                firstAbove = prevAbove = (pt.y < y);
                continue;
            }
            boolean above = (pt.y < y);
            if (prevAbove == above) {
                // Merge segments
                parts[pIdx-1] = getSegment(pT, segs[i]);
            } else {
                pIdx++;
                pT=segs[i-1];
                prevAbove = above;
            }
        }
        if (pIdx == 1) return null;
        Segment [] below, above;
        if (firstAbove) {
            above = new Segment[(pIdx+1)/2];
            below = new Segment[pIdx/2];
        } else {
            above = new Segment[pIdx/2];
            below = new Segment[(pIdx+1)/2];
        }
        int ai=0, bi=0;
        for (int i=0; i<pIdx; i++) {
            if (firstAbove) above[ai++] = parts[i];
            else            below[bi++] = parts[i];
            firstAbove = !firstAbove;
        }
        return new SplitResults(below, above);
    }

    public Segment splitBefore(double t) {
        return getSegment(0.0, t);
    }

    public Segment splitAfter(double t) {
        return getSegment(t, 1.0);
    }

    // Doubles have 48bit precision
    static final double eps = 1/(double)(1L<<48);
    static final double tol = 4.0*eps;

    public static int solveLine(double a, double b,
                                double [] roots) {
        if (a == 0) {
            if (b != 0)
                // No intersection.
                return 0;
            // All pts intersect just return 0.
            roots[0] = 0;
            return 1;
        }

        roots[0] = -b/a;
        return 1;
    }

    public static int solveQuad(double a, double b, double c,
                                double [] roots) {
        // System.err.println("Quad: " + a +"t^2 + " + b +"t + " + c);
        if (a == 0) {
            // no square term.
            return solveLine(b, c, roots);
        }

        double det = b*b-4*a*c;
        // System.err.println("Det: " + det);

        if (Math.abs(det) <= tol*b*b) {
            // one real root (det doesn't contain any useful info)
            roots[0] =  -b/(2*a);
            return 1;
        }

        if (det < 0)
            return 0; // No real roots

        // Two real roots
        det = Math.sqrt(det);
        double w = -(b + matchSign(det, b));
        roots[0] = (2*c)/w;
        roots[1] = w/(2*a);
        return 2;
    }

    public static double matchSign(double a, double b) {
        if (b < 0) return (a < 0)?a:-a;
        return (a > 0)?a:-a;
    }

    public static int solveCubic(double a3, double a2,
                                 double a1, double a0,
                                 double [] roots) {

        // System.err.println("Cubic: " + a3 + "t^3 + " +
        //                    a2 +"t^2 + " +
        //                    a1 +"t + " + a0);

        double [] dRoots = { 0, 0};
        int dCnt = solveQuad(3*a3, 2*a2, a1, dRoots);
        double [] yVals = {0, 0, 0, 0};
        double [] tVals = {0, 0, 0, 0};
        int yCnt=0;
        yVals[yCnt]   = a0;
        tVals[yCnt++] = 0;
        double r;
        switch (dCnt) {
            case 1:
                r = dRoots[0];
                if ((r > 0) && (r < 1)) {
                    yVals[yCnt]   = ((a3*r+a2)*r+a1)*r+a0;
                    tVals[yCnt++] = r;
                }
                break;
            case 2:
                if (dRoots[0] > dRoots[1]) {
                    double t  = dRoots[0];
                    dRoots[0] = dRoots[1];
                    dRoots[1] = t;
                }
                r = dRoots[0];
                if ((r > 0) && (r < 1)) {
                    yVals[yCnt]   = ((a3*r+a2)*r+a1)*r+a0;
                    tVals[yCnt++] = r;
                }
                r = dRoots[1];
                if ((r > 0) && (r < 1)) {
                    yVals[yCnt]   = ((a3*r+a2)*r+a1)*r+a0;
                    tVals[yCnt++] = r;
                }
                break;
            default: break;
        }
        yVals[yCnt]   = a3+a2+a1+a0;
        tVals[yCnt++] = 1.0;

        int ret=0;
        for (int i=0; i<yCnt-1; i++) {
            double y0 = yVals[i],   t0 = tVals[i];
            double y1 = yVals[i+1], t1 = tVals[i+1];
            if ((y0 < 0) && (y1 < 0)) continue;
            if ((y0 > 0) && (y1 > 0)) continue;

            if (y0 > y1) { // swap so y0 < 0 and y1 > 0
                double t;
                t = y0; y0=y1; y1=t;
                t = t0; t0=t1; t1=t;
            }

            if (-y0 < tol*y1) { roots[ret++] = t0;      continue; }
            if (y1 < -tol*y0) { roots[ret++] = t1; i++; continue; }

            double epsZero = tol*(y1-y0);
            int cnt;
            for (cnt=0; cnt<20; cnt++) {
                double dt = t1-t0;
                double dy = y1-y0;
                // double t = (t0+t1)/2;
                // double t= t0+Math.abs(y0/dy)*dt;
                // This tends to make sure that we come up
                // a little short each time this generaly allows
                // you to eliminate as much of the range as possible
                // without overshooting (in which case you may eliminate
                // almost nothing).
                double t= t0+(Math.abs(y0/dy)*99+.5)*dt/100;
                double v = ((a3*t+a2)*t+a1)*t+a0;
                if (Math.abs(v) < epsZero) {
                    roots[ret++] = t; break;
                }
                if (v < 0) { t0 = t; y0=v;}
                else       { t1 = t; y1=v;}
            }
            if (cnt == 20)
                roots[ret++] = (t0+t1)/2;
        }
        return ret;
    }

 /*
 public static void check(Segment seg, float y, PrintStream ps) {
     ps.println("<path fill=\"none\" stroke=\"black\" " +
                " stroke-width=\"3\" d=\"" + seg + "\"/>");

     ps.println("<line x1=\"-1000\" y1=\""+y+
                "\" x2=\"1000\" y2=\""+y+"\" fill=\"none\" stroke=\"orange\"/>\n");

     SplitResults sr = seg.split(y);
     if (sr == null) return;
     Segment [] above = sr.getAbove();
     Segment [] below = sr.getBelow();
     for (int i=0; i<above.length; i++) {
         ps.println("<path fill=\"none\" stroke=\"blue\" " +
                    " stroke-width=\"2.5\" " +
                    " d=\"" + above[i] + "\"/>");
     }
     for (int i=0; i<below.length; i++) {
         ps.println("<path fill=\"none\" stroke=\"red\" " +
                    " stroke-width=\"2\" " +
                    "d=\"" + below[i] + "\"/>");
     }
 }
 public static void main(String [] args) {
     PrintStream ps;
     double [] roots = { 0, 0, 0 };
     int n = solveCubic (-0.10000991821289062, 9.600013732910156,
                         -35.70000457763672, 58.0, roots);
     for (int i=0; i<n; i++)
         System.err.println("Root: " + roots[i]);
     Cubic c;
     c = new Cubic(new Point2D.Double(153.6999969482422,5.099999904632568),
                   new Point2D.Double(156.6999969482422,4.099999904632568),
                   new Point2D.Double(160.39999389648438,2.3999998569488525),
                   new Point2D.Double(164.6999969482422,0.0));
     c.split(0);

     c = new Cubic(new Point2D.Double(24.899999618530273,23.10000228881836),
                   new Point2D.Double(41.5,8.399999618530273),
                   new Point2D.Double(64.69999694824219,1.0),
                   new Point2D.Double(94.5999984741211,1.0));
     c.split(0);

     try {
         ps = new PrintStream(new FileOutputStream(args[0]));
     } catch(java.io.IOException ioe) {
         ioe.printStackTrace();
         return;
     }

     ps.println("<?xml version=\"1.0\" standalone=\"no\"?>\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\"\n" +
                "\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n" +
                "<svg width=\"450\" height=\"500\"\n" +
                "     viewBox=\"-100 -100 450 500\"\n" +
                "     xmlns=\"http://www.w3.org/2000/svg\"\n" +
                "     xmlns:xlink=\"http://www.w3.org/1999/xlink\">");

     check(new Cubic(new Point2D.Double(0, 0),
                     new Point2D.Double(100, 100),
                     new Point2D.Double(-50, 100),
                     new Point2D.Double(50, 0)), 40, ps);

     check(new Cubic(new Point2D.Double(100, 0),
                     new Point2D.Double(200, 100),
                     new Point2D.Double(50, -50),
                     new Point2D.Double(150, 30)), 20, ps);

     check(new Cubic(new Point2D.Double(200, 0),
                     new Point2D.Double(300, 100),
                     new Point2D.Double(150, 100),
                     new Point2D.Double(250, 0)), 75, ps);

     check(new Quadradic(new Point2D.Double(0, 100),
                         new Point2D.Double(50,150),
                         new Point2D.Double(10,100)), 115, ps);

     check(new Linear(new Point2D.Double(100, 100),
                      new Point2D.Double(150,150)), 115, ps);
     ps.println("</svg>");
 }
 */
}
