package com.op.axidraw.paint;

import com.op.axidraw.AxidrawBase;

import java.awt.geom.Point2D;

import static java.awt.Color.BLACK;
import static java.awt.Color.RED;

public class AxidrawPaint extends AxidrawBase {
    private static final AxidrawPaint axidrawPaint = new AxidrawPaint();
    private String dir = host + "axidrawPaint/";
    private String ipFile = "VirgaBWSq";
    private String opFile = ipFile + "_PAINT";

    private int imgWmm = 200;
    private int imgHmm = 200;
    private int paperWmm = 297;
    private int paperHmm = 420;

    private double cornerF = 0.02;
    private double stepmm = 2;
    private double step = 0;
    private double numRadials = 3;
    private double paintBinaryRadmm = 1;

    public static void main(String[] args) throws Exception {
        axidrawPaint.run();
    }

    private void run() throws Exception {
        setup(dir + ipFile + ".jpg", dir + opFile + ".svg", imgWmm, imgHmm, paperWmm, paperHmm);

        drawSVG();

        save();
    }

    private void drawSVG() {
        startSVGPath(0, 0, 0);
        corners(1, 1, totW - 1, totH - 1, totW * cornerF);
        //rect(0, 0, imageW, imageH);
        endSVGPath(RED);

        step = stepmm * scale;

        startSVGPath(1, 10, 10);
        paintAllLines();
        endSVGPath(BLACK);
    }

    private void paintAllLines() {
        double xStep = step * 0.25;
        moveTo((step/2), step/2);
        for (double y = 0; y < imageH; y = y + step) {
            boolean moving = true;
            for (double x = 0; x < imageW - step; x = x + xStep) {
                double black = 1 - getGrey(x, y);
                if (black < 0.5) {
                    moving = true;
                } else {
                    if (moving) {
                        dipPaint(0);
                        moveTo(x + (step / 2), y);
                    }
                    lineTo(x + (step / 2), y);
                    moving = false;
                }
            }
        }
    }

    private void paintAllCells() {
        for (double y = 0; y < imageH; y = y + step) {
            for (double x = 0; x < imageW; x = x + step) {
                paintCell(x + (step / 2), y + (step / 2));
            }
        }
    }

    private void paintCell(double cx, double cy) {
        dipPaint(0);
        double grey = 1 - getGrey(cx, cy);
        double radStep = step / (numRadials + 1);
        double num = (numRadials * grey);
        for (double i = num; i > 0; i--) {
            double rad = ((int)(i+1)) * radStep * 0.5;
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
