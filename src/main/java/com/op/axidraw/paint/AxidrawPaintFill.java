package com.op.axidraw.paint;

import com.op.axidraw.AxidrawBaseCirclePack;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;

import static java.awt.Color.*;

public class AxidrawPaintFill extends AxidrawBaseCirclePack {
    private static final AxidrawPaintFill axidrawPaintFill = new AxidrawPaintFill();
    private String dir = host + "axidrawPaint/";
    private String ipFile = "Virga2";
    private String opFile = ipFile + "_FILL";

    private int imgWmm = 200;
    private int imgHmm = 200;
    private int paperWmm = 297;
    private int paperHmm = 420;
    private double cornerF = 0.02;
    private double strmm = 0.25;

    public static void main(String[] args) throws Exception {
        axidrawPaintFill.run();
    }

    private void run() throws Exception {
        setupPacking();
        setup(dir + ipFile + ".jpg", dir + opFile + ".svg", imgWmm, imgHmm, paperWmm, paperHmm);

        drawSVG();

        save();
    }

    private void setupPacking() {
        minR = 10;
        maxR = 50;
        spacer = 0;
        maxNumFillingF = 1.15;
        innerCircleOnly = false;
        maxCirclesF = 0.5;
    }

    private void drawSVG() throws Exception {
        startSVGPath("0", 0, 0);
        corners(1, 1, totW - 1, totH - 1, totW * cornerF);
        //rect(0, 0, imageW, imageH);
        endSVGPath(RED, strmm * scale);


//        startSVGPath(1, 0, 0);
//        paintFilledCircle(imageW/2, imageH/2, 100, 30, 10);
//        endSVGPath(BLACK, strmm * scale);

//        paintCircles(1, BLACK);

        paintCircles(1, CYAN);
        paintCircles(2, MAGENTA);
        paintCircles(3, YELLOW);
        //paintCircles(4, BLACK);
    }

    private void paintCircles(int layer, Color col) {
        setupAllCirclesPack(col);
        startSVGPath(layer + getColorName(col), 10, 10);
        Collections.sort(circles, new CircleComparator());
        for (Circle circle : this.circles) {
            //double num = 1 + 0.5 * circle.rad;
            double num = 1 + 0.5 * circle.rad * circle.grey * maxNumFillingF;

            double ang = random.nextDouble() * 360;
            if (circle.col.equals(col)) {
                paintFilledCircle(circle.cx, circle.cy, circle.rad, ang, num);
            }
        }
        endSVGPath(col, strmm * scale);
        System.out.println("circles:" + circles.size());
    }

    private void save() throws Exception {
        endSVG();
    }

}
