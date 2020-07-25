package com.op.axidraw.lineportrait;

import com.op.axidraw.AxidrawBaseOLD;

import java.awt.*;
import java.util.Random;

public class LinePortrait extends AxidrawBaseOLD {
    private static final LinePortrait linePortrait = new LinePortrait();
    private String dir = host + "linePortrait/";
    private String ipFile = "VirgaColA4b";
    private String opFile = ipFile + "_LINE";
    private int wmm = 210;
    private int hmm = 297;

    private Random random = new Random(1);

    public static void main(String[] args) throws Exception {
        linePortrait.run();
    }

    private void run() throws Exception {
        setup(wmm, hmm, dir + ipFile + ".png", dir + opFile + ".svg", 3.75);

        //draw();
        drawSVG();

        save();
    }

    private void drawSVG() {
        startSVGPath(0);
        int yStep = 10;
        double xStep = 25;
        double thickness = 0.5;
        double passes = 5;
        double rndSize = 1 * xStep * thickness / passes;

        int x0 = 0;
        int y0 = 0;
        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(1f));
        for (int x = 0; x < w; x = x + (int) xStep) {
            x0 = x;
            y0 = 0;
            for (double n = 0; n < passes; n++) {
                writer.println("M" + dp((borderx + x + xStep / 2) * scaleSVG) + " " + (bordery * scaleSVG) + " ");
                for (int y = 0; y < h; y = y + yStep) {
                    int rgb = ibi.getRGB((int) x, (int) y);
                    Color c = new Color(rgb);
                    double R = (double) (c.getRed()) / 255.0;
                    double G = (double) (c.getGreen()) / 255.0;
                    double B = (double) (c.getBlue()) / 255.0;
                    double grey = ((R + G + B) / 3.0);
                    double rndX = rndSize * random.nextDouble();
                    double pass = -0.5 + n / (passes - 1);
                    double xx = rndX + ((double) x + (xStep / 2.0)) + (thickness * xStep * pass * grey);

                    y0 = (int) y;
                    x0 = x;
                    writer.println("L" + dp((borderx + xx) * scaleSVG) + " " + dp(((bordery + y) * scaleSVG)) + " ");
                }
            }
        }
        endSVGPath(Color.BLACK);
    }

//    private void draw() {
//        startSVGPath(0);
//        double step = 50;
//        int x0 = 0;
//        int y0 = 0;
//        opG.setColor(Color.BLACK);
//        opG.setStroke(new BasicStroke(1f));
//        for (int x = 0; x < w; x = x + (int) step) {
//            writer.println("M" + dp(border + (x * scaleOrig)) + " " + dp(border) + " ");
//            x0 = x;
//            y0 = 0;
//            for (int y = 0; y < h; y++) {
//                int rgb = ibi.getRGB((int) x, (int) y);
//                Color c = new Color(rgb);
//                double R = (double) (c.getRed()) / 255.0;
//                double G = (double) (c.getGreen()) / 255.0;
//                double B = (double) (c.getBlue()) / 255.0;
//                double grey = 1 - ((R + G + B) / 3.0);
//                double xx = (double) x + 10 * grey;
//
//                opG.drawLine(border + x0, border + y0, border + (int) xx, border + y);
//                y0 = (int) y;
//                x0 = x;
//                writer.println("L" + dp(border + (xx * scaleOrig)) + " " + dp(border + (y * scaleOrig)) + " ");
//
//            }
//        }
//        endSVGPath(Color.BLACK);
//
//    }

    private void save() throws Exception {
//        File op1 = new File(dir + opFile + ".png");
//        savePNGFile(obi, op1, pngDpi);

        endSVG();
    }


}
