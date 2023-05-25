package com.op.knit;


import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class StrokePaint extends Base {

    private static StrokePaint strokePaint = new StrokePaint();

    private double strLen = 100;
    private double strF = 20;
    private double strSampleStep = strLen / strF;
    private double boxStepsPerEdge = 100;
    private double strFillDia = 5;
    private double greyThreshold = 0.8;
    private double bf = 0.05;
    private float opacF = 0.5f;

    private double curveVar = 0; //0.025;

    private float stroke = 1f;

    private String dir = host + "knit/";
    private String ipFile = "Virga2BW";
    private String opFile = "STROut";
    private int w = 0;
    private int h = 0;
    private BufferedImage ibi;
    private Graphics2D ipG;
    private BufferedImage obi;
    private Graphics2D opG;
    private ArrayList<Point2D.Double> points = new ArrayList();

    public static void main(String[] args) throws IOException {
        strokePaint.draw();
    }

    public void draw() throws IOException {
        init();

//        double rad = ((double) w) * 0.45;
//        for (double r = rad; r > 0; r =r - radStep) {
//            drawFadedStrokes(r);
//        }

        initBoxPoints();
        drawFadedStrokes();
        save();
    }

    private void drawFadedStrokes() {
        for (Point2D.Double point1 : points) {
            double x1 = point1.x;
            double y1 = point1.y;
            for (Point2D.Double point2 : points) {
                if (point1.x == point2.x || point1.y == point2.y) {
                    continue;
                }

                double x2 = point2.x;
                double y2 = point2.y;
                double ang = Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
                for (double f = 0; f < 1; f = f + 0.1) {
                    double xa = x1 + f * (x2 - x1);
                    double ya = y1 + f * (y2 - y1);
                    double lastGrey = getGrey(ibi, (int) xa, (int) ya);
                    double used = 0;
                    boolean finishedLine = true;
                    for (double d = 0; d < strLen; d = d + strSampleStep) {
                        double xx = xa + d * Math.cos(Math.toRadians(ang));
                        double yy = ya + d * Math.sin(Math.toRadians(ang));
                        if (isOnImage(ibi, (int) xx, (int) yy, bf)) {
                            double grey = getGrey(ibi, (int) xx, (int) yy);
                            if (grey < greyThreshold && grey - lastGrey >= -0.1) {
                                lastGrey = grey;
                                used ++;
                            } else {
                                finishedLine = false;
                                break;
                            }
                        }
                    }
                    if (used > 0.5*strLen/strSampleStep && finishedLine) {
                        drawFadedStroke(xa, ya, ang);
                    }
                }
            }

        }
    }

    private void initBoxPoints() {
        double bx = ((double) w) * bf;
        double by = ((double) h) * bf;
        for (double x = bx; x < w - bf; x = x + ((double) w) / boxStepsPerEdge) {
            points.add(new Point2D.Double(x, by));
        }
        for (double y = by; y < h - bf; y = y + ((double) h) / boxStepsPerEdge) {
            points.add(new Point2D.Double(w - bx, y));
        }
        for (double x = w - bx; x > bf; x = x - ((double) w) / boxStepsPerEdge) {
            points.add(new Point2D.Double(x, h - by));
        }
        for (double y = h - by; y > 0; y = y - ((double) h) / boxStepsPerEdge) {
            points.add(new Point2D.Double(bx, y));
        }
    }

    private void drawFadedStroke(double xr, double yr, double a) {
        for (double d = 0; d < strLen; d = d + strFillDia*0.5) {
            double x = xr + d * Math.cos(Math.toRadians(a));
            double y = yr + d * Math.sin(Math.toRadians(a));
            if (isOnImage(ibi, (int)x, (int)y, bf)) {
                float g = (float) (d / strLen);
                opG.setColor(new Color(g, g, g, opacF));
                opG.fillRect((int) x - (int) (strFillDia / 2), (int) y - (int) (strFillDia / 2), (int) (strFillDia), (int) (strFillDia));

                ipG.setColor(new Color(1, 1, 1, opacF));
                ipG.fillRect((int) x - (int) (strFillDia / 2), (int) y - (int) (strFillDia / 2), (int) (strFillDia), (int) (strFillDia));
            }
        }

    }

//    private void drawFadedStrokes(double rad) {
//        double cx = w / 2;
//        double cy = h / 2;
//        for (double a = 0; a < 360; a = a + angInc) {
//            double x1 = cx + rad * Math.cos(Math.toRadians(a));
//            double y1 = cy + rad * Math.sin(Math.toRadians(a));
//            for (double a2 = 0; a2 < 360; a2 = a2 + angInc) {
//                if (a == a2) {
//                    continue;
//                }
//                double x2 = cx + rad * Math.cos(Math.toRadians(a2));
//                double y2 = cy + rad * Math.sin(Math.toRadians(a2));
//                double lastGrey = getGrey(ibi, (int) x1, (int) y1);
//                boolean finishedLine = true;
//                double ang = Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
//                for (double d = 0; d < strLen; d = d + strSampleStep) {
//                    double xx = x1 + d * Math.cos(Math.toRadians(ang));
//                    double yy = y1 + d * Math.sin(Math.toRadians(ang));
//                    if (!isOnImage(ibi, (int) xx, (int) yy)) {
//                        continue;
//                    }
//                    double grey = getGrey(ibi, (int) xx, (int) yy);
//                    if (grey < greyThreshold && grey - lastGrey >= -0.01) {
//                        lastGrey = grey;
//                    } else {
//                        finishedLine = false;
//                        break;
//                    }
//                }
//                if (finishedLine) {
//                    drawFadedStroke(x1, y1, ang);
//                }
//            }
//        }
//    }

    private void init() throws IOException {
        File ip = new File(host + ipFile + ".png");
        ibi = ImageIO.read(ip);
        ipG = (Graphics2D) ibi.getGraphics();
        w = ibi.getWidth();
        h = ibi.getHeight();
        curveVar = w * curveVar;

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);
        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(stroke));

        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
    }


    private void save() throws IOException {
        File op1 = new File(dir + ipFile + opFile + ".png");
        ImageIO.write(obi, "png", op1);
        System.out.println("Saved " + op1.getPath());

        File op2 = new File(dir + ipFile + opFile + "MASK.png");
        ImageIO.write(ibi, "png", op2);
        System.out.println("Saved " + op2.getPath());
    }

}
