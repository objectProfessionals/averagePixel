package com.op.axidraw.sine;

import com.op.axidraw.AxidrawBaseOLD;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.File;

public class SineWaveSpiral extends AxidrawBaseOLD {

    private static SineWaveSpiral sineWave = new SineWaveSpiral();
    //  R.A. Robertson 2012.03 "Circle Packing 3" ~ www.rariora.org ~
    private String dir = host + "sine/";
    private String ipFile = "Virga2COL";
    private String opFile = ipFile + "_SPIRAL";
    private int wmm = 200;
    private int hmm = 200;
    private double dpi = 300;
    private double greyAngInc = 15;
    private double scaleF = 0.75;
    private Path2D.Double path = new Path2D.Double();

    public static void main(String[] args) throws Exception {
        sineWave.run();
    }

    private void run() throws Exception {
        generateSVG = true;
        setup(wmm, hmm, dir + ipFile + ".jpg", dir + opFile + ".svg", scaleF);

        drawAll(Color.BLACK, 412.0, 413.0);
        //drawAllColors();

        save();
    }

    private void drawAllColors() {
        drawAll(Color.CYAN, 412.0, 413.0);
        drawAll(Color.MAGENTA, 585.0, 413.0);
        drawAll(Color.YELLOW, 502.0, 587.0);
    }

    private void drawAll(Color col, Double ccx, Double ccy) {
        path = new Path2D.Double();
        opG.setColor(col);
        svgGenerator.setColor(col);
        double radIncPerCircum = 10;
        double startRad = 2;
        double rad = startRad;
        double delta = 0.25;
        double circum = Math.PI * 2 * rad;
        double numOfDeltas = circum / delta;
        double mainAng = Math.PI * 2 / numOfDeltas;
        double cx = ccx != null ? ccx : w / 2;
        double cy = ccy != null ? ccy : h / 2;
        double x1 = cx;
        double y1 = cy;
        double sineAngDelta = Math.toRadians(15);
        double sineAng = Math.toRadians(90);
        while (rad < w) {
            double darkness = getAverageDarkness(x1, y1, radIncPerCircum * 0.5, col);
            double angF = 1;
            if (darkness == -1) {
                double x2 = cx + (rad) * Math.cos(mainAng);
                double y2 = cy + (rad) * Math.sin(mainAng);
                moveLine(x2, y2);
                x1 = x2;
                y1 = y2;
            } else {
                double darkSine = 0.5 * radIncPerCircum * Math.sin(sineAng) * getAmplitude(darkness);
                double x2 = cx + (rad + darkSine) * Math.cos(mainAng);
                double y2 = cy + (rad + darkSine) * Math.sin(mainAng);

                drawLine(x1, y1, x2, y2);
                x1 = x2;
                y1 = y2;
                angF = getAngFactor(darkness);
            }

            circum = Math.PI * 2 * rad;
            numOfDeltas = circum / delta;
            mainAng = mainAng + angF * Math.toRadians(360 / numOfDeltas);
            rad = rad + radIncPerCircum / numOfDeltas;
            sineAng = sineAng + sineAngDelta;

            //System.out.println("x,y=" + x1 + "," + y1 + " col=" + col);
        }

        opG.draw(path);

        Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, wmm-1, hmm-1);
        svgGenerator.draw(rect);
        path.transform(AffineTransform.getScaleInstance(scaleF, scaleF));
        svgGenerator.draw(path);
    }

    private double getAmplitude(double darkness) {
        double d = Math.pow(darkness, 2);
        return (d);
    }

    private double getAngFactor(double darkness) {
        double d = Math.pow(darkness, 2);
        return 1 + 1 * (1 - d);
    }

    private void drawLine(double x1, double y1, double x2, double y2) {
        if (path.getCurrentPoint() == null) {
            path.moveTo(borderx + (x2 * scaleSVG), bordery + (y2 * scaleSVG));
        } else {
            path.lineTo(borderx + (x2 * scaleSVG), bordery + (y2 * scaleSVG));
        }
    }

    private void moveLine(double x2, double y2) {
        path.moveTo(borderx + (x2 * scaleSVG), bordery + (y2 * scaleSVG));
    }

    private double getAverageDarkness(double x, double y, double r, Color penCol) {
        double i = 0;
        double totGrey = 0;
        for (double ang = 0; ang < 360; ang = ang + greyAngInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= w || (int) yy >= h || (int) xx < 0 || (int) yy < 0) {
                    continue;
                }
                int rgb = ibi.getRGB((int) xx, (int) yy);
                Color c = new Color(rgb);
                double R = (double) (c.getRed()) / 255.0;
                double G = (double) (c.getGreen()) / 255.0;
                double B = (double) (c.getBlue()) / 255.0;
                if (penCol.equals(Color.BLACK)) {
                    totGrey = totGrey + ((R + G + B) / 3.0);
                } else {
                    float[] rgbF = {(float) R, (float) G, (float) B};
                    float[] cmyk = rgbToCmyk(rgbF);

                    if (penCol.equals(Color.CYAN)) {
                        totGrey = totGrey + (1 - cmyk[0]);
                    } else if (penCol.equals(Color.MAGENTA)) {
                        totGrey = totGrey + (1 - cmyk[1]);
                    } else if (penCol.equals(Color.YELLOW)) {
                        totGrey = totGrey + (1 - cmyk[2]);
                    }
                }
                i++;
            }
        }


        double grey = totGrey / i;
        if (i == 0) {
            return -1;
        }

        return 1 - grey;
    }

    public float[] rgbToCmyk(float... rgb) {
        if (rgb.length != 3) {
            throw new IllegalArgumentException();
        }

        float[] fromRGB = instance.fromRGB(rgb);
        return fromRGB;
    }

    private void save() throws Exception {
        File op1 = new File(dir + opFile + ".png");
        savePNGFile(obi, op1, dpi);

        endSVG();
    }
}
