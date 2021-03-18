/*
 * The MIT License
 *
 * Copyright (c) 2016-2021 Kai Burjack
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.coderbot.iris.vendored.joml;

//#ifndef __GWT__

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Class for polygon/point intersection tests when testing many points against one or many static concave or convex, simple polygons.
 * <p>
 * This is an implementation of the algorithm described in <a href="http://alienryderflex.com/polygon/">http://alienryderflex.com</a> and augmented with using a
 * custom interval tree to avoid testing all polygon edges against a point, but only those that intersect the imaginary ray along the same y co-ordinate of the
 * search point. This algorithm additionally also supports multiple polygons.
 * <p>
 * This class is thread-safe and can be used in a multithreaded environment when testing many points against the same polygon concurrently.
 * <p>
 * Reference: <a href="http://alienryderflex.com/polygon/">http://alienryderflex.com</a>
 * 
 * @author Kai Burjack
 */
public class PolygonsIntersection {

    static class ByStartComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Interval i1 = (Interval) o1;
            Interval i2 = (Interval) o2;
            return Float.compare(i1.start, i2.start);
        }
    }

    static class ByEndComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Interval i1 = (Interval) o1;
            Interval i2 = (Interval) o2;
            return Float.compare(i2.end, i1.end);
        }
    }

    static class Interval {
        float start, end;
        int i, j, polyIndex;
    }

    static class IntervalTreeNode {
        float center;
        float childrenMinMax;
        IntervalTreeNode left;
        IntervalTreeNode right;
        List/* <Interval> */ byBeginning;
        List/* <Interval> */ byEnding;

        static boolean computeEvenOdd(float[] verticesXY, Interval ival, float x, float y, boolean evenOdd, BitSet inPolys) {
            boolean newEvenOdd = evenOdd;
            int i = ival.i;
            int j = ival.j;
            float yi = verticesXY[2 * i + 1];
            float yj = verticesXY[2 * j + 1];
            float xi = verticesXY[2 * i + 0];
            float xj = verticesXY[2 * j + 0];
            if ((yi < y && yj >= y || yj < y && yi >= y) && (xi <= x || xj <= x)) {
                float xDist = xi + (y - yi) / (yj - yi) * (xj - xi) - x;
                newEvenOdd ^= xDist < 0.0f;
                if (newEvenOdd != evenOdd && inPolys != null) {
                    inPolys.flip(ival.polyIndex);
                }
            }
            return newEvenOdd;
        }

        boolean traverse(float[] verticesXY, float x, float y, boolean evenOdd, BitSet inPolys) {
            boolean newEvenOdd = evenOdd;
            if (y == center && byBeginning != null) {
                int size = byBeginning.size();
                for (int b = 0; b < size; b++) {
                    Interval ival = (Interval) byBeginning.get(b);
                    newEvenOdd = computeEvenOdd(verticesXY, ival, x, y, newEvenOdd, inPolys);
                }
            } else if (y < center) {
                if (left != null && left.childrenMinMax >= y)
                    newEvenOdd = left.traverse(verticesXY, x, y, newEvenOdd, inPolys);
                if (byBeginning != null) {
                    int size = byBeginning.size();
                    for (int b = 0; b < size; b++) {
                        Interval ival = (Interval) byBeginning.get(b);
                        if (ival.start > y)
                            break;
                        newEvenOdd = computeEvenOdd(verticesXY, ival, x, y, newEvenOdd, inPolys);
                    }
                }
            } else if (y > center) {
                if (right != null && right.childrenMinMax <= y)
                    newEvenOdd = right.traverse(verticesXY, x, y, newEvenOdd, inPolys);
                if (byEnding != null) {
                    int size = byEnding.size();
                    for (int b = 0; b < size; b++) {
                        Interval ival = (Interval) byEnding.get(b);
                        if (ival.end < y)
                            break;
                        newEvenOdd = computeEvenOdd(verticesXY, ival, x, y, newEvenOdd, inPolys);
                    }
                }
            }
            return newEvenOdd;
        }
    }

    private static final ByStartComparator byStartComparator = new ByStartComparator();
    private static final ByEndComparator byEndComparator = new ByEndComparator();

    protected final float[] verticesXY;
    private float minX, minY, maxX, maxY;
    private float centerX, centerY, radiusSquared;
    private IntervalTreeNode tree;

    /**
     * Create a new {@link PolygonsIntersection} object with the given polygon vertices.
     * <p>
     * The <code>verticesXY</code> array contains the x and y coordinates of all vertices. This array will not be copied so its content must remain constant for
     * as long as the PolygonPointIntersection is used with it.
     * 
     * @param verticesXY
     *            contains the x and y coordinates of all vertices
     * @param polygons
     *            defines the start vertices of a new polygon. The first vertex of the first polygon is always the
     *            vertex with index 0. In order to define a hole simply define a polygon that is completely inside another polygon
     * @param count
     *            the number of vertices to use from the <code>verticesXY</code> array, staring with index 0
     */
    public PolygonsIntersection(float[] verticesXY, int[] polygons, int count) {
        this.verticesXY = verticesXY;
        // Do all the allocations and initializations during this constructor
        preprocess(count, polygons);
    }

    private IntervalTreeNode buildNode(List intervals, float center) {
        List left = null;
        List right = null;
        List byStart = null;
        List byEnd = null;
        float leftMin = 1E38f, leftMax = -1E38f, rightMin = 1E38f, rightMax = -1E38f;
        float thisMin = 1E38f, thisMax = -1E38f;
        for (int i = 0; i < intervals.size(); i++) {
            Interval ival = (Interval) intervals.get(i);
            if (ival.start < center && ival.end < center) {
                if (left == null)
                    left = new ArrayList();
                left.add(ival);
                leftMin = leftMin < ival.start ? leftMin : ival.start;
                leftMax = leftMax > ival.end ? leftMax : ival.end;
            } else if (ival.start > center && ival.end > center) {
                if (right == null)
                    right = new ArrayList();
                right.add(ival);
                rightMin = rightMin < ival.start ? rightMin : ival.start;
                rightMax = rightMax > ival.end ? rightMax : ival.end;
            } else {
                if (byStart == null || byEnd == null) {
                    byStart = new ArrayList();
                    byEnd = new ArrayList();
                }
                thisMin = ival.start < thisMin ? ival.start : thisMin;
                thisMax = ival.end > thisMax ? ival.end : thisMax;
                byStart.add(ival);
                byEnd.add(ival);
            }
        }
        if (byStart != null) {
            Collections.sort(byStart, byStartComparator);
            Collections.sort(byEnd, byEndComparator);
        }
        IntervalTreeNode tree = new IntervalTreeNode();
        tree.byBeginning = byStart;
        tree.byEnding = byEnd;
        tree.center = center;
        if (left != null) {
            tree.left = buildNode(left, (leftMin + leftMax) / 2.0f);
            tree.left.childrenMinMax = leftMax;
        }
        if (right != null) {
            tree.right = buildNode(right, (rightMin + rightMax) / 2.0f);
            tree.right.childrenMinMax = rightMin;
        }
        return tree;
    }

    private void preprocess(int count, int[] polygons) {
        int i, j = 0;
        minX = minY = 1E38f;
        maxX = maxY = -1E38f;
        List intervals = new ArrayList(count);
        int first = 0;
        int currPoly = 0;
        for (i = 1; i < count; i++) {
            if (polygons != null && polygons.length > currPoly && polygons[currPoly] == i) {
                /* New polygon starts. End the current. */
                float prevy = verticesXY[2 * (i - 1) + 1];
                float firsty = verticesXY[2 * first + 1];
                Interval ival = new Interval();
                ival.start = prevy < firsty ? prevy : firsty;
                ival.end = firsty > prevy ? firsty : prevy;
                ival.i = i - 1;
                ival.j = first;
                ival.polyIndex = currPoly;
                intervals.add(ival);
                first = i;
                currPoly++;
                i++;
                j = i - 1;
            }
            float yi = verticesXY[2 * i + 1];
            float xi = verticesXY[2 * i + 0];
            float yj = verticesXY[2 * j + 1];
            minX = xi < minX ? xi : minX;
            minY = yi < minY ? yi : minY;
            maxX = xi > maxX ? xi : maxX;
            maxY = yi > maxY ? yi : maxY;
            Interval ival = new Interval();
            ival.start = yi < yj ? yi : yj;
            ival.end = yj > yi ? yj : yi;
            ival.i = i;
            ival.j = j;
            ival.polyIndex = currPoly;
            intervals.add(ival);
            j = i;
        }
        // Close current polygon
        float yi = verticesXY[2 * (i - 1) + 1];
        float xi = verticesXY[2 * (i - 1) + 0];
        float yj = verticesXY[2 * first + 1];
        minX = xi < minX ? xi : minX;
        minY = yi < minY ? yi : minY;
        maxX = xi > maxX ? xi : maxX;
        maxY = yi > maxY ? yi : maxY;
        Interval ival = new Interval();
        ival.start = yi < yj ? yi : yj;
        ival.end = yj > yi ? yj : yi;
        ival.i = i - 1;
        ival.j = first;
        ival.polyIndex = currPoly;
        intervals.add(ival);
        // compute bounding sphere and rectangle
        centerX = (maxX + minX) * 0.5f;
        centerY = (maxY + minY) * 0.5f;
        float dx = maxX - centerX;
        float dy = maxY - centerY;
        radiusSquared = dx * dx + dy * dy;
        // build interval tree
        tree = buildNode(intervals, centerY);
    }
    
    /**
     * Test whether the given point <code>(x, y)</code> lies inside any polygon stored in this {@link PolygonsIntersection} object.
     * <p>
     * This method is thread-safe and can be used to test many points concurrently.
     * <p>
     * In order to obtain the index of the polygon the point is inside of, use {@link #testPoint(float, float, BitSet)}
     * 
     * @see #testPoint(float, float, BitSet)
     * 
     * @param x
     *            the x coordinate of the point to test
     * @param y
     *            the y coordinate of the point to test
     * @return <code>true</code> iff the point lies inside any polygon; <code>false</code> otherwise
     */
    public boolean testPoint(float x, float y) {
        return testPoint(x, y, null);
    }

    /**
     * Test whether the given point <code>(x, y)</code> lies inside any polygon stored in this {@link PolygonsIntersection} object.
     * <p>
     * This method is thread-safe and can be used to test many points concurrently.
     * 
     * @param x
     *            the x coordinate of the point to test
     * @param y
     *            the y coordinate of the point to test
     * @param inPolys
     *            if not <code>null</code> then the <i>i</i>-th bit is set if the given point is inside the <i>i</i>-th polygon
     * @return <code>true</code> iff the point lies inside the polygon and not inside a hole; <code>false</code> otherwise
     */
    public boolean testPoint(float x, float y, BitSet inPolys) {
        // check bounding sphere first
        float dx = (x - centerX);
        float dy = (y - centerY);
        if (inPolys != null)
            inPolys.clear();
        if (dx * dx + dy * dy > radiusSquared)
            return false;
        // check bounding box next
        if (maxX < x || maxY < y || minX > x || minY > y)
            return false;
        // ask interval tree for all polygon edges intersecting 'y' and perform
        // the even/odd/crosscutting/raycast algorithm on them and also return
        // the polygon index of the polygon the point is in by setting the appropriate
        // bit in the given BitSet.
        boolean res = tree.traverse(verticesXY, x, y, false, inPolys);
        return res;
    }

}


