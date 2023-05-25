package com.op;

import java.awt.*;
import java.awt.geom.*;

public class HeartShape implements Shape {
    Path2D.Double p = new Path2D.Double();

    public Shape getShape(double size, double off, double x1, double y1) {
        return getShape(size, off, x1, y1);
    }

    public Shape getShape(double size, double off, double x1, double y1, double rotAng) {
        double d = size * 2;
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
        AffineTransform at = new AffineTransform();
        AffineTransform tr = AffineTransform.getTranslateInstance(x1, y1);
        AffineTransform rot = AffineTransform.getRotateInstance(Math.toRadians(rotAng));
        //at.concatenate(rot);
        at.concatenate(tr);
        return p.createTransformedShape(at);
    }

    public static void test(double size, double off, double x1, double y1, Graphics2D opG) {
        HeartShape shaper = new HeartShape();
        Shape shape = shaper.getShape(size, off, x1-size, y1-size);
        opG.setColor(Color.RED);
        opG.fill(shape);

        opG.setColor(Color.BLUE);
        opG.drawRect((int)x1 -(int)size +(int)off, (int)y1  -(int)size+ (int)off, (int)size*2, (int)size*2);

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
