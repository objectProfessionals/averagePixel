package com.op.knit;


import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class KnitPaint extends Base {

    private static KnitPaint knitPaint = new KnitPaint();

    private double numPoints = 240;
    private double wBlacknessMin = 0.05; // smaller = darker
    private double wBlacknessMax = 0.3;
    private double wBreakFloor = wBlacknessMin; // darkest, use with maxErrors

    private boolean impByCol = false;
    private double maxErrors = 0;
    private double alphaStart = 0.25;//smaller = darker, use with maxErrors

    private double radF = 0.9;
    private double blacknessRadF = radF; // rad for concentration
    private double lightenF = 1;

    private double curveVar = 0; //0.025;
    private boolean printCoords = true;

    private float stroke = 1f;
    private float strokeMask = stroke * 3; /// str multiple - smaller = darker

    private boolean samplePatch = false;
    private float samplePatchF = 2f;

    private int errorPinOffset = ((int) numPoints / 10);
    private double wBreakDelta = wBlacknessMin - wBreakFloor;

    private String dir = host + "knit/";
    private String ipFile = "Virga2BW2";
    //private String ipFile = "Virga2000-LINE_INV";
    //private String ipFile = "hansolo";
    //private String ipFile = "Heart";
    private String opFile = "KnitOut-" + wBreakFloor + "-" + wBlacknessMin + "-" + wBlacknessMax;
    private int w = 0;
    private int h = 0;
    private int errorCount = 0;

    private ArrayList<Line> usedLines = new ArrayList<Line>();
    private BufferedImage ibi;
    private Graphics2D ipG;

    private BufferedImage obi;
    private Graphics2D opG;
    private double startPinPos = 0; //numPoints / 4;

    public static void main(String[] args) throws IOException {
        knitPaint.draw();
    }

    public void draw() throws IOException {
        init();

        drawContinuous();

        save();
    }


    private void drawContinuous() throws IOException {
        Pin pin = null;
        int endPinPos = (int) startPinPos;
        int count = 0;
        while (endPinPos != -1) {
            pin = calc(endPinPos);
            endPinPos = drawFromPin(pin);
            //endPinPos = drawFromPinBlackest(pin);
            count++;
            //saveMask();
        }
        saveMask();
        println("count=" + count, true);
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
            if (samplePatch) {
                l.blackness = getBlacknessByPatch(angSt, angEn);
            } else {
                l.blackness = getBlacknessLine(angSt, angEn);
            }
            p.endPointsbyWeighting.put(l.blackness, l);
            p.pos = pos;

            c2 = (c2 + 1) % (int) numPoints;
        }
        return p;
    }


    private int drawFromPinBlackest(Pin pin) {
        int max = 1000;
        if (usedLines.size() > max) {
            return -1;
        }
        ArrayList<Line> allLines = new ArrayList<>();
        for (Line l : pin.endPointsbyWeighting.values()) {
            allLines.add(l);
        }
        for (int c = allLines.size() - 1; c > 0; c--) {
            Line l = (Line) allLines.get(c);
            if (!usedLines.contains(l)) {
                drawLine(l);
                usedLines.add(l);
                return l.endPin;
            }
        }
        return -1;
    }

    private int drawFromPin(Pin pin) {

        ArrayList<Line> allLines = new ArrayList<>();
        for (Line l : pin.endPointsbyWeighting.values()) {
            allLines.add(l);
        }
        //Collections.reverse(allLines);
        for (int c = allLines.size() - 1; c > 0; c--) {
            Line l = (Line) allLines.get(c);
            if (l.blackness > wBlacknessMin && l.blackness < wBlacknessMax && !usedLines.contains(l)) {
                //if (l.blackness > pin.remBreak && !usedLines.contains(l)) {
                drawLine(l);
                usedLines.add(l);
                return l.endPin;
            }
        }
        if (errorCount < maxErrors) {
            errorCount++;
            int off = (int) (Math.random() < 0.5 ? Math.random() * errorPinOffset : Math.random() * -errorPinOffset);
            int p2 = (pin.pos + off + (int) numPoints) % ((int) numPoints);
            Line l = new Line();
            wBlacknessMax = wBlacknessMin;
            wBlacknessMin = wBreakFloor + wBreakDelta * (1 - (errorCount / maxErrors));
            l.startPin = pin.pos;
            l.endPin = p2;
            drawLine(l);
            //alphaStart = alphaStart - (alphaStart / (maxErrors * 1.1));
            //ipG.setColor(new Color(255, 255, 255, ((int) (255.0 * (double) alphaStart))));
            println("errorCount=" + errorCount + " wBlacknessMin=" + wBlacknessMin + " wBlacknessMax=" + wBlacknessMax + " alphaStart=" + alphaStart + " usedLines=" + usedLines.size(), printCoords);
            return p2;
        }
        println("No lines", true);
        return -1;
    }

    private void drawLine(Line l) {
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

        int used = usedLines.size();
        println("x1,y1:x2,y2 = " + x1 + "," + y1 + ":" + x2 + "," + y2 + " used:" + used + " err:" + errorCount + " alpha:" + alphaStart + " wBlacknessMin" + wBlacknessMin + " wBlacknessMax:" + wBlacknessMax, !printCoords);
        println("p1:p2 = " + l.startPin + ":" + l.endPin +", blackness="+l.blackness +" used:"+usedLines.size(), printCoords);
        double alpha = l.blackness;
        //ipG.setStroke(new BasicStroke(strokeMask*(float)(1+alpha)));
        ipG.setStroke(new BasicStroke(strokeMask));
        int a = ((int) (255.0 * (double) alpha));
        a = 255;
        ipG.setColor(new Color(255, 255, 255, a));
        drawLine(opG, x1, y1, x2, y2);
        //overlayLine(x1, y1, x2, y2);
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

    private void overlayLine(double x1, double y1, double x2, double y2) {
        double dx = (x2 - x1);
        double dy = (y2 - y1);
        double len = Math.sqrt(dx * dx + dy * dy);

        int str = (int) (strokeMask / 2f);
        for (double j = 1; j < len; j++) {
            double s = j / len;
            int xx = (int) (x1 + dx * s);
            int yy = (int) (y1 + dy * s);

            Color col = new Color(ibi.getRGB(xx, yy));

//            double inc = 1.1;
//            int low = (int)( (double)(col.getRed())*inc > 255 ? 255 : (double)(col.getRed())*inc);

            double light = (lightenF * 255);
            int red = col.getRed();

            if (red < 50) {
                light = light * 2.5;
            } else if (red < 100) {
                light = light * 2;
            } else if (red < 150) {
                light = light * 1.5;
            } else if (red < 200) {
                light = light * 1;
            } else {
                light = light * 0.5;
            }
            int l = col.getRed() + light > 255 ? 255 : (int)(col.getRed() + light);

            Color col2 = new Color(l, l, l);
            ipG.setColor(col2);
            ipG.fillRect(xx - str, yy - str, str * 2, str * 2);
        }
    }


    private double getBlacknessByPatch(double angSt, double angEn) {
        double rad = blacknessRadF * ((double) w) / 2.0;
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

        int pathD = (int) (stroke * samplePatchF);
        for (double j = 1; j < len; j++) {
            double s = j / len;
            int xx = (int) (x1 + dx * s);
            int yy = (int) (y1 + dy * s);

            double patchWhite = 0;
            double patchCount = 0;

            for (int y = -pathD; y <= pathD; y++) {
                for (int x = -pathD; x <= pathD; x++) {
                    if (xx +x >= w || yy + y >= h || xx+x <0 || yy+y <0 ) {
                    } else {
                        Color col = new Color(ibi.getRGB(xx + x, yy + y));
                        double white = ((double) (col.getRed() + col.getGreen() + col.getBlue())) / 3.0;
                        patchWhite = patchWhite + white;
                    }
                    patchCount++;
                }
            }
            patchWhite = patchWhite / patchCount;

            patchWhite = patchWhite / 255.0;
            totWhite = totWhite + patchWhite;
        }
        return 1 - (totWhite / len);
    }

    private double getBlacknessLine(double angSt, double angEn) {
        double rad = blacknessRadF * ((double) w) / 2.0;
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

            double importance = 1; //getImportance(xx, yy);

            Color col = new Color(ibi.getRGB(xx, yy));
            double redF = 1;
            if (impByCol) {
                redF= 0.25;
            }
            double white = ((double) (col.getRed()*redF + col.getGreen() + col.getBlue())) / (redF + 2);
            white = white / 255.0;
            white = white * importance;
            totWhite = totWhite + white;
        }
        return 1 - (totWhite / len);
    }

    private double getImportance(int xx, int yy) {
        double impF = 0.95;
        int cx = w/2;
        int cy = h/2;
        int cix = 500;
        int ciy = 500;
        int radi = w/4;
        Ellipse2D imp = new Ellipse2D.Double(cix, ciy, radi, radi);
        if (imp.contains(xx, yy)) {
            return impF;
        } else {
            return 1;
        }
    }

    private void init() throws IOException {
        File ip = new File(dir + ipFile + ".jpg");
        ibi = ImageIO.read(ip);
        w = ibi.getWidth();
        h = ibi.getHeight();
        curveVar = w * curveVar;

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
        //ipG.setStroke(new BasicStroke(strokeMask));

        //ipG.setXORMode(Color.WHITE);
        //ipG.setColor(new Color(255, 255, 255, 155));

        //ipG.setColor(new Color(255, 255, 255, ((int) (255.0 * (double) alphaStart))));
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
