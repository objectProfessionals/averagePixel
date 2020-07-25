package com.op.pack;

import com.op.Base;
import com.op.HeartShape;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Ishihara extends Base {

    private static Ishihara circlePack = new Ishihara();
    private String ipFile1 = "San2";
    private String ipFile2 = "Vir2";
    private String folder = "SanVir";
    private String dir = host + "ishihara/" + folder + "/";
    private String opFile = "Ishihara";
    private int w = 0;
    private int h = 0;
    private double dpi = 254;
    double brightnessThreshold = 175;
    private BufferedImage ibi1;
    private BufferedImage ibi2;
    private BufferedImage obi;
    private Graphics2D opG;

    ArrayList<Circle> circleList = new ArrayList<>();
    Color ground = Color.WHITE;  // Background color.
    Color fill = Color.BLACK;  // Background color.
    int maxCircles = 0;
    double minR = 5;
    double maxR = 40;
    private int lowestRad = (int)minR;
    private int maxTryCount = 250;
    double spacer = 0;
    double angInc = 15;
    double strokeF = 0.25;
    double maxCirclesF = 25;// VirgaCol:3=20 75 1 0 10 0.25; 2= 10 30 0
    private boolean innerCircleOnly = true;
    private double bf = 0.05;
    private double minRDec = 0.5;
    private double maxRDec = 0.5;

    public static void main(String[] args) throws Exception {
        circlePack.run();
    }

    private void run() throws Exception {
        setup();

        drawAll();

        save();
    }

    private void drawAll() {
        for (int i = 0; i < maxCircles; i++) {
            draw(i);
        }
    }


    void setup() throws IOException {
        File ip = new File(dir + ipFile1 + ".jpg");
        ibi1 = ImageIO.read(ip);

        File ip2 = new File(dir + ipFile2 + ".jpg");
        ibi2 = ImageIO.read(ip2);

        w = ibi1.getWidth();
        h = ibi1.getHeight();
        maxCircles = (int) (((double) w) * maxCirclesF);

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(ground);
        opG.fillRect(0, 0, w, h);

        opG.setColor(fill);

//        int border = (int)((double)(w)*bf);
//        opG.drawOval(border, border, w-2*border, h-2*border);

    }

    int draw(int numTry) {
        int tryCount = 0;
        double ww = (double) w;
        double hh = (double) h;
        boolean hasGroundOnlyColor;
        double r = minR + Math.random() * maxR;
        double x = Math.random() * ww;
        double y = Math.random() * hh;
        double rr = r + spacer;
        hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);

        hasGroundOnlyColor = testForInerCircleOnly(hasGroundOnlyColor, x, y, rr);
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
            }
            hasGroundOnlyColor = testForInerCircleOnly(hasGroundOnlyColor, x, y, rr);
        }

        if (hasGroundOnlyColor) {
            Color col = Color.WHITE;
            Color col1 = getAverageColor(x, y, r, ibi1);
            Color col2 = getAverageColor(x, y, r, ibi2);
            double g1 = (col1.getRed() + col1.getGreen() + col1.getBlue()) / 3;
            double g2 = (col2.getRed() + col2.getGreen() + col2.getBlue()) / 3;
            boolean p1LighterThanp2 = g1 > g2;
            boolean p2LighterThanp1 = g2 > g1;
            if (g1 > brightnessThreshold && g2 > brightnessThreshold) {
                int rb = (int) brightnessThreshold + (int) (Math.random() * (255 - brightnessThreshold));
                int green = (int) brightnessThreshold + (int) (Math.random() * (255 - brightnessThreshold));
                col = new Color(rb, green, rb);
                //System.out.println("grey="+grey);
            } else if (p1LighterThanp2) {
                double blue = brightnessThreshold + (255.0 - (double) brightnessThreshold) * ((g1 - g2) / 255);
                col = new Color((int) g2, (int) g1, (int) blue);
            } else if (p2LighterThanp1) {
                double blue = brightnessThreshold + (255.0 - (double) brightnessThreshold) * ((g2 - g1) / 255);
                col = new Color((int) g2, (int) g1, (int) blue);
//            } else {
//                int grey = br + (int)(Math.random()*(250-br));
//                col = new Color(255, 255, grey);
            }
            circleList.add(new Circle(x, y, r));
            drawOne(x, y, rr, col);
        }
        if (tryCount > maxTryCount) {
            minR = Math.max(lowestRad, minR - minRDec);
            maxR = Math.max(minR + 1, maxR - maxRDec);
        }
        System.out.println("hasOnlyGroundColor:" + hasGroundOnlyColor + " numTry:" + numTry + " circles:" + circleList.size() + " tryCount:" + tryCount + " rad:" + rr + " minRad:" + minR + " maxR:" + maxR);

        return tryCount;
    }

    private boolean testForInerCircleOnly(boolean hasGroundOnlyColor, double x, double y, double rr) {
        if (innerCircleOnly) {
            double border = (double) (w) * bf;
            Ellipse2D inner = new Ellipse2D.Double(border, border, w - 2 * border, h - 2 * border);
            Point2D in = new Point2D.Double(x, y);
            if (!inner.contains(in)) {
                //System.out.println("inside x,y="+x+","+y);
                return false;
            }
        }

        return hasGroundOnlyColor;
    }

    private void drawOne(double x, double y, double rr, Color col) {
        int rrr = (int) (rr - spacer);

        opG.setColor(col);
        Shape shape = getShape(x, y, rrr);
        opG.fill(shape);

//        opG.setColor(col.darker());
//        opG.setStroke(new BasicStroke((float) (minR * strokeF)));
//        opG.draw(shape);
    }

    private Shape getShape(double x, double y, int rrr) {
        return new Ellipse2D.Double((int) (x - rrr), (int) (y - rrr), rrr * 2, rrr * 2);
    }

    private Shape getShapeHeart(double x, double y, int rrr) {
        int size = 9 * rrr / 10;
        int off = 1 * rrr / 10;
        Shape heart = new HeartShape().getShape(size, off, (int) x, (int) y);
        return heart;
    }

    private Color getCircleColor(double x, double y, double r, BufferedImage ibi) {
        Color aveCol = Color.BLACK;
        double i = 0;
        double totR = 0;
        double totG = 0;
        double totB = 0;
        for (double ang = 0; ang < 360; ang = ang + angInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= w || (int) yy >= h || (int) xx < 0 || (int) yy < 0) {
                    continue;
                }
                int rgb = ibi.getRGB((int) xx, (int) yy);
                Color c = new Color(rgb);
                totR = totR + (double) (c.getRed()) / 255.0;
                totG = totG + (double) (c.getGreen()) / 255.0;
                totB = totB + (double) (c.getBlue()) / 255.0;
                i++;
            }
        }


        int totGr = (int) ((getRGB(totR, i) + getRGB(totG, i) + getRGB(totB, i)) / 3);
        aveCol = new Color(totGr, totGr, totGr);
        return aveCol;
    }

    private Color getAverageColor(double x, double y, double r, BufferedImage ibi) {
        Color aveCol = Color.BLACK;
        double i = 0;
        double totR = 0;
        double totG = 0;
        double totB = 0;
        for (double ang = 0; ang < 360; ang = ang + angInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= w || (int) yy >= h || (int) xx < 0 || (int) yy < 0) {
                    continue;
                }
                int rgb = ibi.getRGB((int) xx, (int) yy);
                Color c = new Color(rgb);
                totR = totR + (double) (c.getRed()) / 255.0;
                totG = totG + (double) (c.getGreen()) / 255.0;
                totB = totB + (double) (c.getBlue()) / 255.0;
                i++;
            }
        }


        aveCol = new Color(getRGB(totR, i), getRGB(totG, i), getRGB(totB, i));
        return aveCol;
    }

    private int getRGB(double totR, double i) {
        return (int) ((totR / i) * 255.0);
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
                if ((int) xx >= w || (int) yy >= h || (int) xx < 0 || (int) yy < 0) {
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

/* ======================= Circle Class ======================= */

    class Circle {
        double x, y, r;

        Circle(double x, double y, double r) {
            this.x = x;
            this.y = y;
            this.r = r;
            //ellipse(x, y, r, r);  // Turn off and run with lines only for variety.
        }
    }

    private void save() throws Exception {
        File op1 = new File(dir + ipFile1 + "_" + ipFile2 + "_" + opFile + ".png");
        savePNGFile(obi, op1, dpi);
    }


}
