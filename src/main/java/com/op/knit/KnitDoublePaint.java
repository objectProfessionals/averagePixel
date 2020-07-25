package com.op.knit;


import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class KnitDoublePaint extends Base {

    private static KnitDoublePaint knitPaint = new KnitDoublePaint();

    private double numPoints = 60;
    private double wBreakFloor = 0.02; // use with maxErrors
    private double wBreakMin = 0.05; // smaller = darker
    private double wBreakMax = 1;
    private double curveVar = 0.025;//0.025
    private double radF = 0.9;
    private double alphaStart = 0.95;//smaller = darker
    private double maxErrors = 0;// use with maxErrors
    private boolean printCoords = true;

    private float stroke = 1f;
    private float strokeMask = stroke * 2;
    private int errorPinOffset = ((int) numPoints / 10);
    private double wBreakDelta = wBreakMin - wBreakFloor;

    private String dir = host + "knit/double/";
    private String ipFile1 = "Heart";
    private String ipFile2 = "V";
    private String opFile = "KnitDOut-" + wBreakFloor + "-" + wBreakMin + "-" + alphaStart;
    private int w = 0;
    private int h = 0;
    private int errorCount = 0;

    private ArrayList<Line> usedLines1 = new ArrayList<Line>();
    private ArrayList<Line> usedLines2 = new ArrayList<Line>();
    private BufferedImage ibi1;
    private Graphics2D ipG1;
    private BufferedImage ibi2;
    private Graphics2D ipG2;

    private BufferedImage obi;
    private Graphics2D opG;
    private double startPinPos = numPoints - (numPoints / 4);

    public static void main(String[] args) throws IOException {
        knitPaint.draw();
    }

    public void draw() throws IOException {
        init();

        drawContinuous();

        save();
    }


    private void drawContinuous() throws IOException {
        double ang = 360 / numPoints;
        Pin pin = null;
        int endPinPos = (int) startPinPos;
        int count = 0;
        HashMap stEn = new HashMap<Pin, Integer>();
        while (endPinPos != -1) {
            boolean first = count % 2 == 0;
            BufferedImage ibi = first ? ibi1 : ibi2;
            Graphics2D ipG = first ? ipG1 : ipG2;
            ArrayList used = first ? usedLines1 : usedLines2;
            Color opCol = first ? Color.RED : Color.BLUE;
            pin = calc(ibi, endPinPos);
            if (stEn.get(pin) == null) {
                stEn.put(pin, 0);
            } else {
                stEn.put(pin, ((int) stEn.get(pin)) + 1);
            }
            endPinPos = drawFromPin(pin, used, ipG, opCol);
            count++;
            //saveMask();
            //break;
        }
        saveMask();
        println("count=" + count, true);
    }

    private Pin calc(BufferedImage ibi, int pos) {
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
            l.blackness = getBlackness(ibi, angSt, angEn);
            p.endPointsbyWeighting.put(l.blackness, l);
            p.pos = pos;

            c2 = (c2 + 1) % (int) numPoints;
        }
        return p;
    }


    private int drawFromPin(Pin pin, ArrayList usedLines, Graphics2D ipG, Color opCol) {

        ArrayList<Line> allLines = new ArrayList<>();
        for (Line l : pin.endPointsbyWeighting.values()) {
            allLines.add(l);
        }
        for (int c = allLines.size() - 1; c > 0; c--) {
            Line l = (Line) allLines.get(c);
            if (l.blackness > wBreakMin && l.blackness < wBreakMax && !usedLines.contains(l)) {
                //if (l.blackness > pin.remBreak && !usedLines.contains(l)) {
                drawLine(l, ipG, opCol);
                usedLines.add(l);
                return l.endPin;
            }
        }
        if (errorCount < maxErrors) {
            errorCount++;
            int off = (int) (Math.random() < 0.5 ? Math.random() * errorPinOffset : Math.random() * -errorPinOffset);
            int p2 = (pin.pos + off + (int) numPoints) % ((int) numPoints);
            Line l = new Line();
            wBreakMax = wBreakMin;
            wBreakMin = wBreakFloor + wBreakDelta * (1 - (errorCount / maxErrors));
            l.startPin = pin.pos;
            l.endPin = p2;
            drawLine(l, ipG, opCol);
            alphaStart = alphaStart - (alphaStart / (maxErrors * 1.1));
            ipG.setColor(new Color(255, 255, 255, ((int) (255.0 * (double) alphaStart))));
            println("errorCount=" + errorCount + " wBreakMin=" + wBreakMin + " wBreakMax=" + wBreakMax + " alphaStart=" + alphaStart + " usedLines=" + usedLines.size(), !printCoords);
            return p2;
        }
        return -1;
    }

    private void drawLine(Line l, Graphics2D ipG, Color opCol) {
        double rad = radF * ((double) w) / 2.0;
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

        println("x1,y1:x2,y2 = " + x1 + "," + y1 + ":" + x2 + "," + y2, !printCoords);
        int st = 1 + (int) ((numPoints + (l.startPin - startPinPos)) % numPoints);
        int en = 1 + (int) ((numPoints + (l.endPin - startPinPos)) % numPoints);
        println("p1:p2 = " + st + ":" + en, printCoords);
        opG.setColor(opCol);
        drawLine(opG, x1, y1, x2, y2);
        drawLine(ipG, x1, y1, x2, y2);

        //opG.drawLine(x1, y1, x2, y2);
        //ipG.drawLine(x1, y1, x2, y2);
    }

    private void println(String str, boolean doPrint) {
        if (doPrint) {
            System.out.println(str);
        }
    }

    private void drawLine(Graphics2D g, int x1, int y1, int x2, int y2) {
        double var = curveVar * (-0.5 + Math.random());
        QuadCurve2D curve = new QuadCurve2D.Double();
        curve.setCurve(x1, y1, x1 + var, y1 + var, x2, y2);
        g.draw(curve);
    }

    private double getBlackness(BufferedImage bi, double angSt, double angEn) {
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

            Color col = new Color(bi.getRGB(xx, yy));
            double white = ((double) (col.getRed() + col.getGreen() + col.getBlue())) / 3.0;
            white = white / 255.0;
            totWhite = totWhite + white;
        }
        return 1 - (totWhite / len);
    }

    private void init() throws IOException {
        File ip = new File(dir + ipFile1 + ".jpg");
        ibi1 = ImageIO.read(ip);
        File ip2 = new File(dir + ipFile2 + ".jpg");
        ibi2 = ImageIO.read(ip2);
        w = ibi1.getWidth();
        h = ibi1.getHeight();
        curveVar = w * curveVar;

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);
        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(stroke));
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        //wBreak = calcBreak();

        ipG1 = (Graphics2D) ibi1.getGraphics();
        ipG1.setStroke(new BasicStroke(strokeMask));
        //ipG1.setColor(new Color(255, 0, 0, ((int) (255.0 * (double) alphaStart))));
        ipG1.setColor(new Color(255, 255, 255, ((int) (255.0 * (double) alphaStart))));

        ipG2 = (Graphics2D) ibi2.getGraphics();
        ipG2.setStroke(new BasicStroke(strokeMask));
        //ipG2.setColor(new Color(0, 0, 255, ((int) (255.0 * (double) alphaStart))));
        ipG2.setColor(new Color(255, 255, 255, ((int) (255.0 * (double) alphaStart))));

    }

    private void save() throws IOException {
        File op1 = new File(dir + ipFile1 + "-" + ipFile2 + opFile + ".jpg");
        ImageIO.write(obi, "jpg", op1);
        System.out.println("Saved " + op1.getPath());
    }

    private void saveMask() throws IOException {
        File op1 = new File(dir + ipFile1 + "_MASK.jpg");
        ImageIO.write(ibi1, "jpg", op1);
        System.out.println("Saved " + op1.getPath());
        File op2 = new File(dir + ipFile2 + "_MASK.jpg");
        ImageIO.write(ibi2, "jpg", op2);
        System.out.println("Saved " + op2.getPath());
    }

    private class Line implements Comparable {
        int startPin = 0;
        int endPin = 0;
        double blackness = 0;

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
        Map<Double, Line> endPointsbyWeighting = new TreeMap<Double, Line>();
    }

}
