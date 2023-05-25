package com.op.axidraw;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import static java.awt.Color.BLACK;

public class AxidrawBaseCirclePack extends AxidrawBase {
    protected double minR = 0;
    protected double maxR = 0;
    protected double spacer = 0;
    protected double maxNumFillingF = 0;
    protected boolean innerCircleOnly = false;
    protected double maxCirclesF = 1;

    protected Random random = new Random(1);
    protected ArrayList<Circle> circles = new ArrayList<Circle>();

    int maxCircles = 0;
    double angInc = 10;
    private BufferedImage obi;
    private Graphics2D opG;
    private Color ground = Color.WHITE;  // Background color.
    private Color fill = BLACK;  // Background color.
    double strokeG = 1;

    protected void setupAllCirclesPack(Color col) {
        setupGraphicsForCirlcesPack();
        for (int i = 0; i < maxCircles; i++) {
            addPackedCircle(i, col);
        }

    }

    protected void setupGraphicsForCirlcesPack() {
        maxCircles = (int) (((double) imageW) * maxCirclesF);

        obi = new BufferedImage(imageW, imageH, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(ground);
        opG.fillRect(0, 0, imageW, imageH);

        opG.setColor(fill);

        circles = new ArrayList<>();

    }

    private int addPackedCircle(int numTry, Color col) {
        int tryCount = 0;
        double ww = (double) imageW;
        double hh = (double) imageH;
        boolean hasGroundOnlyColor;
        double r = minR + Math.random() * maxR;
        double x = Math.random() * ww;
        double y = Math.random() * hh;
        double rr = r + spacer;
        hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);

        double border = (double) (imageW) * 0.25;
        if (innerCircleOnly) {
            Ellipse2D inner = new Ellipse2D.Double(border, border, imageW - 2 * border, imageH - 2 * border);
            Rectangle2D in = new Rectangle2D.Double(x - rr / 2, y - rr / 2, rr, rr);
            if (!inner.contains(in)) {
                hasGroundOnlyColor = false;
            }
        }
        while (!hasGroundOnlyColor && rr > minR) {
            rr--;
            hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);
            tryCount++;
            //System.out.println("rr"+rr);
            if (!hasGroundOnlyColor && rr <= minR) {
                r = minR + Math.random() * maxR;
                x = Math.random() * ww;
                y = Math.random() * hh;
                rr = r + spacer;
                //System.out.println("tryAgain");
                hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);
                if (innerCircleOnly) {
                    Ellipse2D inner = new Ellipse2D.Double(border, border, imageW - 2 * border, imageH - 2 * border);
                    Rectangle2D in = new Rectangle2D.Double(x - rr / 2, y - rr / 2, rr, rr);
                    if (!inner.contains(in)) {
                        hasGroundOnlyColor = false;
                    }
                }
            }
        }

        if (hasGroundOnlyColor) {
            float grey = (float) getAverageColor(x, y, r, col);
            drawOne(x, y, rr, grey, col);
        }
        if (tryCount > 200000) {
            minR = Math.max(2, minR - 1);
        }
        System.out.println("hasOnlyGroundColor:" + hasGroundOnlyColor + " numTry:" + numTry + " tryCount:" + tryCount + " minRad:" + minR);


        return tryCount;
    }

    private void drawOne(double x, double y, double rr, float g, Color colcmyk) {
        int rrr = (int) (rr - spacer);

        Color col = new Color(g, g, g);
        opG.setColor(col);
        Shape shape = getShape(x, y, rrr);
        opG.fill(shape);

        opG.setColor(col.darker());
        opG.setStroke(new BasicStroke((float) (strokeG)));
        opG.draw(shape);


        addCircle(x, y, rr, g, colcmyk);
    }

    private void addCircle(double x, double y, double rr, float g, Color colcmyk) {
        Circle circle = new Circle(x, y, rr, g, colcmyk);
        circles.add(circle);
    }

    private Shape getShape(double x, double y, int rrr) {
        return new Ellipse2D.Double((int) (x - rrr), (int) (y - rrr), rrr * 2, rrr * 2);
    }

    private boolean hasOnlyGroundColor(double x, double y, double r) {
        int rgb = obi.getRGB((int) x, (int) y);
        Color colorTest = new Color(rgb);
        boolean hasGroundColor = (colorTest.equals(ground));
        if (!hasGroundColor) {
            return false;
        }

        for (double ang = 0; ang < 360; ang = ang + angInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= imageW || (int) yy >= imageH || (int) xx < 0 || (int) yy < 0) {
                    continue;
                }
                rgb = obi.getRGB((int) xx, (int) yy);
                colorTest = new Color(rgb);
                hasGroundColor = (colorTest.equals(ground));
                if (!hasGroundColor) {
                    return false;
                }
            }
        }

        return true;
    }

    protected class Circle {
        public double cx, cy, rad, grey;
        public Color col;

        public Circle(double x, double y, double rr, double g, Color colcmyk) {
            this.cx = x;
            this.cy = y;
            this.rad = rr;
            this.grey = g;
            this.col = colcmyk;
        }

        @Override
        public boolean equals(Object o) {
            Circle c = (Circle)o;
            double icx = imageW/2;
            double icy = imageH/2;
            double zThis = Math.sqrt((this.cx - icx)*(this.cx - icx) + (this.cy - icy) * (this.cy - icy));
            double zO = Math.sqrt((c.cx - icx)*(c.cx - icx) + (c.cy - icy) * (c.cy - icy));
            return zThis < zO;
        }
    }

    public class CircleComparator implements Comparator<Circle> {

        @Override
        public int compare(Circle o1, Circle o2) {
            return (int) ((o1.cx + o1.cy * imageW) - (o2.cx + o2.cy * imageW));
        }
    }


}
