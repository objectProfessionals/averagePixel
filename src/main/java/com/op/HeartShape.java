package com.op;

import java.awt.*;
import java.awt.geom.*;

public class HeartShape implements Shape {
    Path2D.Double p = new Path2D.Double();

    public Shape getShape(int size, int off, int x1, int y1) {
        int d = size * 2;
        p.moveTo(off, off + d / 4);
        p.quadTo(off, off, off + d / 4, off);
        p.quadTo(off + d / 2, off, off + d / 2, off + d / 4);
        p.quadTo(off + d / 2, off, off + d * 3 / 4, off);
        p.quadTo(off + d, off, off + d, off + d / 4);
        p.quadTo(off + d, off + d / 2, off + d * 3 / 4, off + d * 3 / 4);
        p.lineTo(off + d / 2, off + d);
        p.lineTo(off + d / 4, off + d * 3 / 4);
        p.quadTo(off, off + d / 2, off, off + d / 4);
        p.closePath();
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        return p.createTransformedShape(at);
    }

    @Override
    public boolean contains(Point2D arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(Rectangle2D arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(double arg0, double arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(double arg0, double arg1, double arg2,
                            double arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Rectangle getBounds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Rectangle2D getBounds2D() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PathIterator getPathIterator(AffineTransform arg0) {
        return p.getPathIterator(arg0);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform arg0, double arg1) {
        return p.getPathIterator(arg0, arg1);
    }

    @Override
    public boolean intersects(Rectangle2D arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean intersects(double arg0, double arg1, double arg2,
                              double arg3) {
        // TODO Auto-generated method stub
        return false;
    }
}
