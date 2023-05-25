package com.op.axidraw.paint;

import com.op.axidraw.AxidrawBase;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;

import static java.awt.Color.BLACK;
import static java.awt.Color.RED;

public class AxidrawPaintSine extends AxidrawBase {
    private static final AxidrawPaintSine axidrawPaint = new AxidrawPaintSine();
    private String dir = host + "axidrawPaint/";
    private String ipFile = "VirgaBWSq";
    private String opFile = ipFile + "_AXSINE";

    private int imgWmm = 200;
    private int imgHmm = 200;
    private int paperWmm = 297;
    private int paperHmm = 420;

    private double cornerF = 0.02;
    private double paintDipRadmm = 5;
    private double deltamm = 2; //5;

    private double angInc = 15;
    private double greyAngInc = 15;
    private double delta = 0; //5;
    private double deltaY = 0;
    private double deltaX = 0;
    private double deltaAmp = 0;
    private double deltaGreyR = 0;
    private Random random = new Random(1);
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
        rect(0, 0, imageW, imageH);
        endSVGPath(RED, strmm*scale);

        setup();

        startSVGPath("1", 0, 0);
        drawSines(deltaY);
        endSVGPath(BLACK, strmm*scale);
    }

    private void setup() {
        delta = deltamm * scale; //5;
        deltaY = delta;
        deltaX = delta * 0.05;
        deltaAmp = delta * 0.5;
        deltaGreyR = delta * 0.5;
    }

    private void drawSines(double yOff) {
        for (double y = yOff; y <= imageH; y = y + deltaY) {
            dipPaint(BLACK);
            drawLine(y);
        }
    }

    private void drawLine(double y) {
        double x = 0;
        double r = deltaGreyR;
        double ang = random.nextDouble() * 360;
        double x1 = 0;
        double darkness = getAverageDarkness(x, y, r);
        double angF = getAngFactor(darkness);
        double amplitude = getAmplitude(darkness);
        double angIncR = Math.toRadians(angInc);
        double angR1 = Math.toRadians(ang);
        double y1 = y + amplitude * Math.sin(angR1);

        moveTo(x1, y1);
        while (x1 < imageW) {
            double angR2 = angR1 + angIncR;
            double xD = angF * deltaX;
            double yD = amplitude * Math.sin(angR2);

            lineTo(x1 + xD, y + yD);

            //System.out.println("x1,y1,x2,y2:" + x1 + "," + y1 + "," + (x1 + xD) + "," + (y1 + yD) + " darkness=" + darkness);
            darkness = getAverageDarkness(x1 + xD, y, r);
            angF = getAngFactor(darkness);
            amplitude = getAmplitude(darkness);

            x1 = x1 + xD;
            y1 = y + yD;

            angR1 = angR2;
        }
    }

    private double getAmplitude(double darkness) {
        double d = Math.pow(darkness, 2);
        return (2 * deltaAmp * d);
    }

    private double getAngFactor(double darkness) {
        double d = Math.pow(darkness, 2);
        return 1 + 1 * (1 - d);
    }

    private double getAverageDarkness(double x, double y, double r) {
        double i = 0;
        double totGrey = 0;
        for (double ang = 0; ang < 360; ang = ang + greyAngInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= imageW || (int) yy >= imageH || (int) xx < 0 || (int) yy < 0) {
                    continue;
                }
                int rgb = ibi.getRGB((int) xx, (int) yy);
                Color c = new Color(rgb);
                double R = (double) (c.getRed()) / 255.0;
                double G = (double) (c.getGreen()) / 255.0;
                double B = (double) (c.getBlue()) / 255.0;
                totGrey = totGrey + ((R + G + B) / 3.0);
                i++;
            }
        }


        double grey = totGrey / i;
        return 1 - grey;
    }


    private void save() throws Exception {
        endSVG();
    }


}
