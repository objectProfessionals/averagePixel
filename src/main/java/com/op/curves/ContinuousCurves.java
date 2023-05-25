package com.op.curves;

import com.op.axidraw.AxidrawBaseOLD;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContinuousCurves extends AxidrawBaseOLD {
    private static final ContinuousCurves linePortrait = new ContinuousCurves();
    private String dir = host + "continuousCurves/";
    private String ipFile = "pi100K";
    private String opFile = ipFile + "_CC";
    private int wmm = 297;
    private int hmm = 210;
    private double pxpi = 3.776190476190476;
    private double w = wmm * pxpi;
    private double h = hmm * pxpi;
    private String pi = "";
    private int charDepth = 10;
    private double numericalDepth = Math.pow(10, charDepth);
    private int numOveride = 10; //pi.length()
    private int startPoint = 60;

    public static void main(String[] args) throws Exception {
        linePortrait.run();
    }

    private void run() throws Exception {
        startSVG(dir + opFile + ".svg", wmm, hmm, 0, 0);
        startSVGPath(0);
        readPI();


        moveToStart();
        //drawSVGArcs();
        //drawQuadraticBeziers();
        drawCubicBeziers();

        endSVGPath(Color.BLACK, 1);

        save();
    }

    private void moveToStart() {
        if (startPoint == 0) {
            writer.println("M " + dp(w / 2) + "," + dp(h / 2));
        } else {
            int dataDepth = 6 * charDepth;
            int i = (startPoint - 1) * dataDepth;
            double xStart = w * valueDepthAt(i, 4);
            double yStart = h * valueDepthAt(i, 5);
            writer.println("M " + dp(xStart) + "," + dp(yStart));
        }
    }

    private void drawCubicBeziers() {
        int dataDepth = 6 * charDepth;
        int num = dataDepth * (numOveride == 0 ? pi.length() / dataDepth : numOveride);
        for (int i = startPoint * dataDepth; i < startPoint * dataDepth + num; i = i + dataDepth) {
            double x1 = w * valueDepthAt(i, 0);
            double y1 = h * valueDepthAt(i, 1);
            double x2 = w * valueDepthAt(i, 2);
            double y2 = h * valueDepthAt(i, 3);
            double xEnd = w * valueDepthAt(i, 4);
            double yEnd = h * valueDepthAt(i, 5);

            writer.println("C " + dp(x1) + " " + dp(y1) + " " +
                    dp(x2) + " " + dp(y2) + " "
                    + dp(xEnd) + " " + dp(yEnd));
        }
    }

    private void readPI() {
        Path filePath = Path.of(dir + ipFile + ".txt");

        try {
            pi = Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawSVGArcs() throws FileNotFoundException, UnsupportedEncodingException {
        int dataDepth = 5 * charDepth;
        int num = numOveride == 0 ? pi.length() : numOveride;
        for (int i = 0; i < num - dataDepth; i = i + dataDepth) {
            double cx = w * valueDepthAt(i, 0);
            double cy = h * valueDepthAt(i, 1);
            double r = w * valueDepthAt(i, 2);
            double angStartRad = Math.PI * 2 * (valueDepthAt(i, 3));
            double angEndRad = Math.PI * 2 * (valueDepthAt(i, 4));

            double angStartDeg = Math.toDegrees(angStartRad);

            double xEnd = cx + r * Math.cos(angEndRad);
            double yEnd = cy + r * Math.sin(angEndRad);

            writer.println("A " + dp(r) + " " + dp(r) + " " + dp(angStartDeg) + " 1 1 " + dp(xEnd) + " " + dp(yEnd) + " ");
        }


    }

    private void drawQuadraticBeziers() throws FileNotFoundException, UnsupportedEncodingException {
        int dataDepth = 4 * charDepth;
        int num = numOveride == 0 ? pi.length() : numOveride;
        for (int i = 0; i < num - dataDepth; i = i + dataDepth) {
            double x1 = w * valueDepthAt(i, 0);
            double y1 = h * valueDepthAt(i, 1);
            double xEnd = w * valueDepthAt(i, 2);
            double yEnd = h * valueDepthAt(i, 3);

            writer.println("Q " + dp(x1) + " " + dp(y1) + " " + dp(xEnd) + " " + dp(yEnd));
        }
    }

    private double valueDepthAt(int i, int d) {
        String v = "";
        for (int ii = i + d * charDepth; ii < i + d * charDepth + charDepth; ii++) {
            String c = charAt(ii);
            v = v + c;
        }
        return Double.valueOf(v) / numericalDepth;
    }

    private String charAt(int i) {
        return "" + (pi.charAt(i));
    }

    private void save() throws Exception {
//        File op1 = new File(dir + opFile + ".png");
//        savePNGFile(obi, op1, pngDpi);

        endSVG();
    }


}
