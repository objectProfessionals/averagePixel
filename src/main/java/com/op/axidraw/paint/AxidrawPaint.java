package com.op.axidraw.paint;

import com.op.axidraw.AxidrawBase;

import java.awt.*;

import static java.awt.Color.*;

public class AxidrawPaint extends AxidrawBase {
    private static final AxidrawPaint axidrawPaint = new AxidrawPaint();
    private String dir = host + "axidrawPaint/";
    private String ipFile = "Virga2";
    private String opFile = ipFile + "_PAINT";

    private int imgWmm = 200;
    private int imgHmm = 200;
    private int paperWmm = 297;
    private int paperHmm = 420;
    private boolean blackOrcmyk = false;
    private boolean realPaint = true;

    private double cornerF = 0.02;
    private double stepmm = 1;
    private double step = 0;
    private double numRadials = 3;
    private double paintBinaryRadmm = 1;
    private double strmm = 0.25;

    public static void main(String[] args) throws Exception {
        axidrawPaint.run();
    }

    private void run() throws Exception {
        setup(dir + ipFile + ".jpg", dir + opFile + ".svg", imgWmm, imgHmm, paperWmm, paperHmm);

        drawSVG();

        save();
    }

    private void drawSVG() {
        startSVGPath("0", 0, 0);
        corners(1, 1, totW - 1, totH - 1, totW * cornerF);
        //rect(0, 0, imageW, imageH);
        double str = strmm * scale;
        endSVGPath(RED, str);

        step = stepmm * scale;

        //paintAsLines();
        paintAsCircles();
    }

    private void paintAsCircles() {
        if (blackOrcmyk) {
            paintSVGAsCircle(1, 0, BLACK, imageW - step, step, 90, 180);
        } else {
            paintSVGAsCircle(1, 0, YELLOW, imageW - step, imageH - step, 180, 270);
            paintSVGAsCircle(2, 0, CYAN, step, step, 0, 90);
            paintSVGAsCircle(3, 0, MAGENTA, imageW - step, step, 90, 180);
            paintSVGAsCircle(4, 0, BLACK, step, imageH - step, 270, 360);
        }
    }

    private void paintSVGAsCircle(int layer, double radOff, Color col, double cx, double cy, double stAng, double enAng) {
        startSVGPath(layer+getColorName(col), 10, 10);
        paintAllCircles(radOff, col, cx, cy, stAng, enAng);
        double str = strmm * scale;
        endSVGPath(col, str);
    }

    private void paintAllCircles(double radOff, Color col, double cx, double cy, double stAng, double enAng) {
        double rad = step + radOff;
        double maxRad = Math.sqrt(imageW * imageW + imageH * imageH);
        double x1 = rad * Math.cos(Math.toRadians(0));
        double y1 = rad * Math.sin(Math.toRadians(0));
        boolean start = true;
        double valThreshold = 0.5;

        refreshPaint(WHITE, 10);

        while (rad < maxRad) {
            boolean moving = true;
            refreshPaint(col,1);
            for (double ang = stAng; ang < enAng; ang++) {
                double x2 = cx + rad * Math.cos(Math.toRadians(ang));
                double y2 = cy + rad * Math.sin(Math.toRadians(ang));
                if (x2 < 0 || x2 > imageW || y2 < 0 || y2 > imageH) {
                    continue;
                }

                if (start) {
                    moveTo(x1, y1);
                    start = false;
                } else {
                    double value = 1;
                    if (col.equals(BLACK)) {
                        value = 1 - getGrey(x2, y2);
                    } else {
                        value = getAverageColor(x2, y2, step, col);
                    }
                    if (value < valThreshold) {
                        moving = true;
                    } else {
                        if (moving) {
                            moveTo(x2 + (step / 2), y2);
                        }
                        lineTo(x2 + (step / 2), y2);
                        moving = false;
                    }
                }
            }
            rad = rad + step;
        }
    }

    private void refreshPaint(Color col, int i) {
        if (realPaint) {
            dipPaint(col, i);
        }
    }

    private void paintAsLines() {
        if (blackOrcmyk) {
            paintSVG(1, 0, BLACK);
        } else {
            paintSVG(1, 0, CYAN);
            paintSVG(2, stepmm / 3, MAGENTA);
            paintSVG(3, 2 * stepmm / 3, YELLOW);
        }
    }

    private void paintSVG(int layer, double yOff, Color col) {
        startSVGPath(layer+getColorName(col), 10, 10);
        paintAllLines(col, yOff);
        double str = strmm * scale;
        endSVGPath(col, str);
    }

    private void paintAllLines(Color col, double yOffmm) {
        double yOff = yOffmm * scale;
        double xStep = step * 0.5;
        double valThreshold = 0.5;

        moveTo((step / 2), step / 2);
        for (double y = yOff; y < imageH; y = y + step) {
            boolean moving = true;
            for (double x = 0; x < imageW - step; x = x + xStep) {
                double value = 1;
                if (col.equals(BLACK)) {
                    value = 1 - getGrey(x, y);
                } else {
                    value = getAverageColor(x, y, step, col);
                }
                if (value < valThreshold) {
                    moving = true;
                } else {
                    if (moving) {
                        dipPaint(col);
                        moveTo(x + (step / 2), y);
                    }
                    lineTo(x + (step / 2), y);
                    moving = false;
                }
            }
        }
    }

    private void paintAllGreyCells() {
        for (double y = 0; y < imageH; y = y + step) {
            for (double x = 0; x < imageW; x = x + step) {
                paintGreyCell(x + (step / 2), y + (step / 2));
            }
        }
    }

    private void paintGreyCell(double cx, double cy) {
        dipPaint(BLACK);
        double grey = 1 - getGrey(cx, cy);
        double radStep = step / (numRadials + 1);
        double num = (numRadials * grey);
        for (double i = num; i > 0; i--) {
            double rad = ((int) (i + 1)) * radStep * 0.5;
            circleAt(cx, cy, rad);
        }
    }

    private void paintCellBinary(double cx, double cy) {
        double grey = 1 - getGrey(cx, cy);
        double rad = paintBinaryRadmm * scale;
        if (grey > 0.5) {
            circleAt(cx, cy, rad);
        }
    }

    private void save() throws Exception {
        endSVG();
    }


}
