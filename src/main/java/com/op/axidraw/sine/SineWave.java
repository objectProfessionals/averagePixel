package com.op.axidraw.sine;

import com.op.axidraw.AxidrawBaseOLD;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.io.*;
import java.util.Random;

public class SineWave extends AxidrawBaseOLD {

    private static SineWave sineWave = new SineWave();
    private String dir = host + "sine/";
    private String ipFile = "VirgaCol25";
    private String opFile = ipFile + "_SINE";
    private int wmm = 210;
    private int hmm = 297;
    private double scaleOrig = 1;
    private double pngDpi = 300;
    private double angInc = 15;
    private double greyAngInc = 15;
    private double delta = 20; //5;
    private double deltaY = delta;
    private double deltaX = delta * 0.05;
    private double deltaAmp = delta * 0.5;
    private double deltaGreyR = delta * 1;
    private Random random = new Random(1);

    private boolean allColors = true;

    public static void main(String[] args) throws Exception {
        sineWave.run();
    }

    private void run() throws Exception {
        setup(wmm, hmm, dir + ipFile + ".jpg", dir + opFile + ".svg", 3.75);

//        if (allColors) {
//            drawSingle(1, Color.CYAN, 0);
//            drawSingle(2, Color.MAGENTA, deltaY*0.25);
//            drawSingle(3, Color.YELLOW, deltaY*0.5);
//            drawSingle(4, Color.BLACK, deltaY*0.75);
//        } else {
            drawSingle(1, Color.BLACK, 0);
//        }

        save();
    }

    private void drawSingle(int layer, Color col, double yOff) {
        Path2D.Double path = new Path2D.Double();
        boolean l2r = true;
        startSVGPath(layer+1);
        for (double y = yOff; y <= h; y = y + deltaY) {
            drawLine(path, y, col, l2r);
            l2r = !l2r;
        }

        endSVGPath(col);
        opG.setColor(col);
        opG.draw(path);
    }

    private void drawLine(Path2D.Double path, double y, Color penCol, boolean l2r) {
        double x = 0;
        double r = deltaGreyR;
        double ang = random.nextDouble() * 360;
        double x1 = l2r ? 0 : w;
        double darkness = getAverageDarkness(x, y, r, penCol);
        double angF = getAngFactor(darkness);
        double amplitude = getAmplitude(darkness);
        double angIncR = Math.toRadians(angInc);
        double angR1 = Math.toRadians(ang);
        double y1 = y + amplitude * Math.sin(angR1);

        while ((l2r && (x1 < w)) || (!l2r && (x1 > 0))) {
            double angR2 = angR1 + angIncR;
            double xD = angF * deltaX * (l2r ? 1 : -1);
            double yD = amplitude * Math.sin(angR2);

            drawLine(path, x1, y1, x1 + xD, y + yD, l2r);

            //System.out.println("x1,y1,x2,y2:" + x1 + "," + y1 + "," + (x1 + xD) + "," + (y1 + yD) + " darkness=" + darkness);
            darkness = getAverageDarkness(x1 + xD, y, r, penCol);
            angF = getAngFactor(darkness);
            amplitude = getAmplitude(darkness);

            x1 = x1 + xD;
            y1 = y + yD;

            angR1 = angR2;
        }
        drawJoin(path, x1, y, l2r);
    }

    private double getAmplitude(double darkness) {
        double d = Math.pow(darkness, 2);
        return (2 * deltaAmp * d);
    }

    private double getAngFactor(double darkness) {
        double d = Math.pow(darkness, 2);
        return 1 + 1 * (1 - d);
    }

    private void drawJoin(Path2D.Double path, double x, double y, boolean l2r) {
        if (l2r) {
            path.append(new Arc2D.Double(dp(2 * borderx + (x * scaleOrig)), dp(2 * bordery + (y * scaleOrig)), dp(deltaY * scaleOrig),
                    dp(deltaY * scaleOrig), 90, -180, Arc2D.OPEN), true);
            writer.println("A"+dp(deltaY * scaleOrig*0.5)+" "+dp(deltaY * scaleOrig*0.5) +" 0 0 1 "+dp(2 * borderx + x*scaleOrig)+ " "+ dp(2* bordery+y*scaleOrig));
        } else {
            path.append(new Arc2D.Double(dp(2 * borderx + (x * scaleOrig) - (deltaY * scaleOrig)), dp(2 * borderx + (y * scaleOrig)), dp((deltaY * scaleOrig)),
                    dp(deltaY * scaleOrig), 90, 180, Arc2D.OPEN), true);
            writer.println("A"+dp(deltaY * scaleOrig*0.5)+" "+dp(deltaY * scaleOrig*0.5) +" 0 0 1 "+dp(2 * borderx - x*scaleOrig)+ " "+ dp(2* bordery+y*scaleOrig));
        }
    }

    private void drawLine(Path2D.Double path, double x1, double y1, double x2, double y2, boolean l2r) {
        //opG.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));

        if (path.getCurrentPoint() == null) {
            path.moveTo(dp(2 * borderx + (x1 * scaleOrig)), dp(2 * bordery + (y1 * scaleOrig)));
            path.lineTo(dp(2 * borderx + (x2 * scaleOrig)), dp(2 * bordery + (y2 * scaleOrig)));
            writer.println("M"+dp(2 * borderx + (x1 * scaleOrig))+" "+dp(2 * bordery + (y1 * scaleOrig))+" ");
            writer.println("L"+dp(2 * borderx + (x2 * scaleOrig)) +" "+ dp(2 * bordery + (y2 * scaleOrig))+" ");
        } else {
            path.lineTo(dp(2 * borderx + (x2 * scaleOrig)), dp(2 * bordery + (y2 * scaleOrig)));
            writer.println("L"+dp(2 * borderx + (x2 * scaleOrig)) +" "+ dp(2 * bordery + (y2 * scaleOrig))+" ");
        }
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
        return 1 - grey;
    }

    private void save() throws Exception {
        File op1 = new File(dir + opFile + ".png");
        savePNGFile(obi, op1, pngDpi);

        endSVG();
    }

}
