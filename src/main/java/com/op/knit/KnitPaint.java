package com.op.knit;


import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class KnitPaint extends Base {

    private static KnitPaint knitPaint = new KnitPaint();

    private double numPoints = 60;
    private double wBreak = 0.15; // smaller = darker
    private double curveVar = 0.025;//0.025
    private double radF = 0.75;

    private float stroke = 1f;
    private float strokeMask = stroke * 3;
    private double maxErrors = 5;
    private int errorPinOffset = ((int)numPoints/10);

    private String dir = host + "knit/";
    //private String ipFile = "Virga3B";
    private String ipFile = "Heart2";
    private String opFile = "KnitOut";
    private int w = 0;
    private int h = 0;
    private int errorCount = 0;

    private ArrayList<Line> usedLines = new ArrayList<Line>();
    private BufferedImage ibi;
    private Graphics2D ipG;

    private BufferedImage obi;
    private Graphics2D opG;

    public static void main(String[] args) throws IOException {
        knitPaint.draw();
    }

    public void draw() throws IOException {
        init();

        //calcAllPins();
        //drawAll();
        drawContinuous();

        save();
    }


    private void drawContinuous() throws IOException {
        double ang = 360 / numPoints;
        Pin pin = null;
        int endPinPos = 0;
        int count = 0;
        HashMap stEn = new HashMap<Pin, Integer>();
        while (endPinPos != -1) {
            pin = calc(endPinPos);
            if (stEn.get(pin) == null) {
                stEn.put(pin, 0);
            } else {
                stEn.put(pin, ((int) stEn.get(pin)) + 1);
            }
            endPinPos = drawFromPin(pin);
            count++;
            //saveMask();
        }
        saveMask();
        System.out.println("count=" + count);
    }

    private Pin calc(int pos) {
        double ang = 360 / numPoints;
        double angSt = pos * ang;

        double minAng = ang * 10;
        Pin p = new Pin();
        p.endPointsbyWeighting = new TreeMap<Double, Line>();
        int c2 = pos;
        for (double angEn = angSt + ang; angEn < 360 + angSt; angEn = angEn + ang) {
            if (Math.abs(angEn - angSt) < minAng) {
                c2++;
                continue;
            }
            if (Math.abs(angEn - angSt) > 360 - minAng) {
                c2++;
                continue;
            }
            Line l = new Line();
            l.startPin = pos;
            l.endPin = c2;
            l.blackness = getBlackness(angSt, angEn);
            p.endPointsbyWeighting.put(l.blackness, l);
            p.pos = pos;

            c2 = (c2 + 1) % (int) numPoints;
        }
        return p;
    }


    private int drawFromPin(Pin pin) {

        ArrayList<Line> allLines = new ArrayList<>();
        for (Line l : pin.endPointsbyWeighting.values()) {
            allLines.add(l);
        }
        for (int c = allLines.size() - 1; c > 0; c--) {
            Line l = (Line) allLines.get(c);
            if (!l.used) {
                if (l.blackness > pin.remBreak && !usedLines.contains(l)) {
                    drawLine(l);
                    l.used = true;
                    usedLines.add(l);
                    return l.endPin;
                }
            }
        }
        if (errorCount < maxErrors) {
            int off = (int) (Math.random() < 0.5 ? Math.random() * errorPinOffset : Math.random() * -errorPinOffset);
            int p2 = (pin.pos + off + (int) numPoints) % ((int) numPoints);
            Line l = new Line();
            pin.remBreak = pin.remBreak + wBreak/maxErrors;
            l.startPin = pin.pos;
            l.endPin = p2;
            drawLine(l);
            errorCount++;
            System.out.println("errorCount=" + errorCount);
            return p2;
        }
        return -1;
    }

    private void drawLine(Line l) {
        double rad = radF *((double) w) / 2.0;
        int cx = w / 2;
        int cy = h / 2;
        double angPerPin = 360.0 / numPoints;

        double angSt = angPerPin * l.startPin;
        double angEn = angPerPin * l.endPin;

        double xSt = cx + rad * Math.cos(Math.toRadians(angSt));
        double ySt = cy + rad * Math.sin(Math.toRadians(angSt));

        double xEn = cx + rad * Math.cos(Math.toRadians(angEn));
        double yEn = cy + rad * Math.sin(Math.toRadians(angEn));

        int x1 = (int) xSt;
        int y1 = (int) ySt;
        int x2 = (int) xEn;
        int y2 = (int) yEn;

        //System.out.println("x1,y1:x2,y2 = " + x1 + "," + y1 + ":" + x2 + "," + y2);
        System.out.println("p1:p2 = "+l.startPin+":"+l.endPin);
        drawLine(opG, x1, y1, x2, y2);
        drawLine(ipG, x1, y1, x2, y2);

        //opG.drawLine(x1, y1, x2, y2);
        //ipG.drawLine(x1, y1, x2, y2);
    }

    private void drawLine(Graphics2D g, int x1, int y1, int x2, int y2) {
        double var = curveVar * (-0.5 + Math.random());
        QuadCurve2D curve = new QuadCurve2D.Double();
        curve.setCurve(x1, y1, x1+var, y1+var, x2, y2);
        g.draw(curve);
    }

    private double getBlackness(double angSt, double angEn) {
        double rad = radF * ((double) w) / 2.0;
        int cx = w / 2;
        int cy = h / 2;
        double x1 = cx + (int) (rad * Math.cos(Math.toRadians(angSt)));
        double y1 = cy + (int) (rad * Math.sin(Math.toRadians(angSt)));

        double x2 = cx + (int) (rad * Math.cos(Math.toRadians(angEn)));
        double y2 = cy + (int) (rad * Math.sin(Math.toRadians(angEn)));

        double totWhite = 0;

        double dx = (x2 - x1);
        double dy = (y2 - y1);
        double len = Math.sqrt(dx * dx + dy * dy);

        for (double j = 1; j < len; j++) {
            double s = j / len;
            int xx = (int) (x1 + dx * s);
            int yy = (int) (y1 + dy * s);

            Color col = new Color(ibi.getRGB(xx, yy));
            double white = ((double) (col.getRed() + col.getGreen() + col.getBlue())) / 3.0;
            white = white / 255.0;
            totWhite = totWhite + white;
        }
        return 1 - (totWhite / len);
    }

    private void init() throws IOException {
        File ip = new File(dir + ipFile + ".jpg");
        ibi = ImageIO.read(ip);
        w = ibi.getWidth();
        h = ibi.getHeight();
        curveVar = w  * curveVar;

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);
        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(stroke));
        //wBreak = calcBreak();

        ipG = (Graphics2D) ibi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        ipG.setStroke(new BasicStroke(strokeMask));
        ipG.setColor(Color.WHITE);
    }

    private double calcWBreak() {
        double tot = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = ibi.getRGB(x, y);
                Color c = new Color(rgb);
                double g = (c.getRed() + c.getGreen() + c.getBlue()) / 3.0;
                tot = tot + g;
            }
        }
        return tot / (double) (255.0 * w * h);
    }

    private void save() throws IOException {
        File op1 = new File(dir + ipFile + opFile + ".jpg");
        ImageIO.write(obi, "jpg", op1);
        System.out.println("Saved " + op1.getPath());
    }

    private void saveMask() throws IOException {
        File op1 = new File(dir + ipFile + "_MASK.jpg");
        ImageIO.write(ibi, "jpg", op1);
        System.out.println("Saved " + op1.getPath());
    }

    private class Line implements Comparable {
        int startPin = 0;
        int endPin = 0;
        double blackness = 0;
        boolean used = false;

        @Override
        public int compareTo(Object o) {
            Line l1 = (Line) this;
            Line l2 = (Line) o;

            return (int) (1000.0 * (l1.blackness - l2.blackness));
        }

        @Override
        public boolean equals(Object o) {
            Line l = (Line) o;
            boolean sameLine = (l.startPin == this.startPin && l.endPin == this.endPin);
            boolean reverseLine = (l.endPin == this.startPin && l.startPin == this.endPin);

            return sameLine || reverseLine;
        }
    }

    private class Pin {
        int pos = 0;
        double remBreak = wBreak;
        Map<Double, Line> endPointsbyWeighting = new TreeMap<Double, Line>();
    }

}
