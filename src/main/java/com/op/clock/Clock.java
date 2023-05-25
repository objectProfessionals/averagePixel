package com.op.clock;


import com.op.GifSequenceWriter;
import com.op.axidraw.AxidrawBase;
import org.apache.batik.svggen.SVGPath;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Clock extends AxidrawBase {

    private static Clock clock = new Clock();

    private String dir = host + "clock/";
    private String ipFile = "CLOCK";
    private int w = 1000;
    private int h = w;

    private BufferedImage obi;
    private Graphics2D opG;
    private String fontFile = "ARIAL.TTF";
    private String dirFont = "../host/fonts/" + fontFile;
    private Font font;
    private float fontSize = 70;
    private int frameTime = 10;
    private BufferedImage obis[] = null;
    private int totalNumbers = 10;
    private double increments = 10;
    private boolean saveAsGIF = false;
    private boolean saveAsLinear = false;
    private boolean saveAsSVG = true;
    private boolean asOneSVGPath = false;
    private boolean saveAsTEX = false;
    Path2D.Double circlePathsPerPoint[] = null;

    private double[] ys = {6, 7, 8, 9, 10, 9.5, 8.5, 7.5, 6.5, 5.5, 4.5, 3.5, 2.5, 1.5, 0.5, 1, 2, 3, 4, 5};
    private double[] xs1 = {-8, -1, -8, 1, 8, 8, 6, 2, 1, 5};
    private double[] xs2 = {-7.5, -1, -7.5, 5, 0, 7.5, 7, 3, 4, -6};
    private double[] xs3 = {-6, -1, -7, 4, -6, 7, 6, 4, 6, -7};
    private double[] xs4 = {-4, -1, -5, -2, -3, 5, 4, 5, 4, -6};
    private double[] xs5 = {0, 0, 0, 0, 0, 0, 0, 6, 0, -4};
    private double[] xs6 = {4, 1, 5, 5, 0, -5, -4, -6, -6, 0};
    private double[] xs7 = {6, 1, 7, 6, 0, -7, -6, -5, -4, 4};
    private double[] xs8 = {7, 1, 8, 6, 0, -8, -7, 1, -2, 5};
    private double[] xs9 = {8, 1, 7, 5, 0, -7, -7, 0, 0, 6};
    private double[] xs10 = {8, 1, 5, 2, 0, -5, -7, -1, 2, 6.5};
    private double[] xs11 = {8, 1, 0, 5, 0, 0, -6.5, -2, 4, 7};
    private double[] xs12 = {7, 1, -7, 6, 0, 7, -6, -3, 6, 7};
    private double[] xs13 = {6, 1, -8, 6, 0, 8, -5, -4, 4, 7};
    private double[] xs14 = {4, 1, -8, 5, 0, 8, -4, -5, 2, 6};
    private double[] xs15 = {0, 0, -7, 0, -1, 7, 0, -4, 0, 4};
    private double[] xs16 = {-4, -1, 3, -2, -1, -3, 4, -3, -2, 0};
    private double[] xs17 = {-6, -1, 6, 4, -1, -6, 6, -2, -4, -4};
    private double[] xs18 = {-7.5, -1, 7, 5, -1, -7, 7, -1, -6, -6};
    private double[] xs19 = {-8, -1, 7.5, 4, -1, -7.5, 2, 0, -4, -7};
    private double[] xs20 = {-8, -1, 8, 1, -1, -8, -6, 1, -1, -6};

    private double[][] allPoints = {xs1, xs2, xs3, xs4, xs5, xs6, xs7, xs8, xs9, xs10, xs11, xs12, xs13, xs14, xs15, xs16, xs17, xs18, xs19, xs20};


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        clock.draw();

    }

    private void draw() throws Exception {
        init();

        drawAll();

        if (saveAsGIF) {
            saveAsGif();
        }
        if (saveAsSVG) {
            startSVG();
            int tot = circlePathsPerPoint.length;
            if (asOneSVGPath) {
                writer.println("<path id=\"path\" d=\"\n");
            }
            for (int n = 0; n < tot; n++) {
                Path2D.Double path = circlePathsPerPoint[n];
                System.out.println("numPoints=" + numberOfPoints(path));
                PathIterator pi = path.getPathIterator(null);
                int c = 0;
                String i = ("" + (ys[n]));
                String ii = i.replaceAll("\\.", "_");
                if (!asOneSVGPath) {
                    writer.println("<path id=\"path" + ii + "\" d=\"\n");
                }

                while (!pi.isDone()) {
                    double[] coords = {0, 0, 0};
                    pi.currentSegment(coords);
                    double x = coords[0];
                    double y = coords[1];
                    if (c == 0) {
                        moveTo(x, y);
                    } else {
                        lineTo(x, y);
                    }
                    pi.next();
                    c++;
                }
                if (!asOneSVGPath) {
                    writer.println("\" style=\"fill:none;stroke:#000000; stroke-width:1\" />");
                }
            }

            if (asOneSVGPath) {
                writer.println("\" style=\"fill:none;stroke:#000000; stroke-width:1\" />");
            }
            endSVG();
        }

        if (saveAsTEX) {
            saveTEX();
        }
    }

    private void saveTEX() throws Exception {
        File op1 = new File(dir + ipFile + ".jpg");
        saveJPGFile(obi, op1, 300);
    }


    private int numberOfPoints(Path2D.Double path) {
        int i = 0;
        PathIterator pi = path.getPathIterator(null);
        while (!pi.isDone()) {
            i++;
            pi.next();
        }

        return i;
    }

    private double[] getPoint(Path2D.Double path, int d) {
        int i = 0;
        PathIterator pi = path.getPathIterator(null);
        while (!pi.isDone()) {
            if (i == d) {
                double[] coords = {0, 0};
                pi.currentSegment(coords);
                return coords;
            }
            i++;
            pi.next();
        }

        return null;
    }

    private void drawAll() throws IOException, FontFormatException {
        double facStep = 1.1;
        for (int n = 0; n < totalNumbers; n++) {
            double fac = 1;
            for (int i = 0; i < increments; i++) {
                draw(n, i, fac);
                if (i < increments / 2) {
                    fac = fac / facStep;
                } else {
                    fac = fac * facStep;
                }
            }
        }
    }

    private void draw(int n, int i, double fac) throws IOException, FontFormatException {

        //-8<x<8 -10<y<10
        double stlScale = 222;
        double xMax = 8;
        double xRange = xMax * 2;
        double radOff = xRange * stlScale * 15;
        double angD = 360.0 / ((double) totalNumbers * increments);

        double scaleX = stlScale;
        double scaleY = stlScale * 2;
        int cx = w / 2;
        int cy = h;
        if (saveAsGIF) {
            opG.setColor(Color.WHITE);
            opG.fillRect(0, 0, w, h);
        }

        Path2D path = new Path2D.Double();
        int ny = 0;

        for (int nn = 0; nn < allPoints.length; nn++) {
            double x0 = allPoints[nn][n];
            double y = ys[ny];

            int n2 = n + 1;
            if (n2 >= xs1.length) {
                n2 = 0;
            }
            System.out.println("nn, n2: " + nn + "," + n2);
            double x1 = allPoints[nn][n2];

            double iFac = ((double) i + 1) / (increments + 1);
            iFac = fac * iFac;
            double x = x0 + ((x1 - x0) * iFac);

            if (nn == 0) {
                path.moveTo(cx + (x * scaleX), cy + (y * -scaleY));
            } else {
                path.lineTo(cx + (x * scaleX), cy + (y * -scaleY));
            }
            if (saveAsLinear) {
            }
            if (saveAsSVG || saveAsTEX) {
                Path2D.Double circlePath = circlePathsPerPoint[nn];
                double currNumPoints = numberOfPoints(circlePath);
                double currAng = currNumPoints * angD;
                double radD = x * stlScale * xRange;
                double newX = ((radOff + radD) * (Math.cos(Math.toRadians(currAng))));
                double newY = ((radOff + radD) * (Math.sin(Math.toRadians(currAng))));
                if (saveAsLinear) {
                    double linD = stlScale * xRange;
                    double rF = 1;
                    newX = x * linD;
                    newY = (currNumPoints) * linD;
                }
                if (currNumPoints == 0) {
                    circlePath.moveTo(newX, newY);
                } else {
                    circlePath.lineTo(newX, newY);
                }
                if (!saveAsLinear && currNumPoints == (totalNumbers * increments) -1) {
                    double coords[] = getPoint(circlePath, 0);
                    circlePath.lineTo(coords[0], coords[1]);
                }
            }
            if (saveAsTEX) {
                double yRange = circlePathsPerPoint.length;
                Path2D.Double circlePath = circlePathsPerPoint[nn];
                double currNumPoints = numberOfPoints(circlePath);
                double xf = currNumPoints / (circlePathsPerPoint.length * (double)i);
                int xx = (int) (xf * (double)w);

                double df = ((double)h) / yRange;
                int yy = (int) (y * df);

                float g = (float) ((xMax + x0) / xRange);
                Color grey = new Color(g, g, g);
                opG.setColor(grey);
                int dd = (int)(w/(double)(circlePathsPerPoint.length));
                opG.fillRect(xx-dd/2, yy-dd/2, dd, dd);
            }
            ny++;
        }

        if (saveAsGIF) {
            opG.setColor(Color.BLACK);
            opG.draw(path);
        }

        obis[(n * (int) increments) + i] = deepCopy(obi);
    }

    public void startSVG() throws FileNotFoundException, UnsupportedEncodingException {
        int imgWmm = w / 10;
        int imgHmm = h / 10;
        int paperWmm = imgWmm + 2;
        int paperHmm = imgHmm + 2;
        imageW = w;
        startSVG(dir + ipFile + ".svg", imgWmm, imgHmm, paperWmm, paperHmm);
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public void init() throws IOException {
        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(new Color(0.5f, 0.5f, 0.5f));
        opG.fillRect(0, 0, w, h);

        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(5));
        obis = new BufferedImage[totalNumbers * (int) increments];

        circlePathsPerPoint = new Path2D.Double[allPoints.length];
        for (int n = 0; n < allPoints.length; n++) {
            circlePathsPerPoint[n] = new Path2D.Double();
        }
    }

    public void saveAsGif() {
        BufferedImage firstImage = obis[0];

        String out = dir + ipFile + ".gif";
        File fOut = new File(out);
        if (fOut.exists()) {
            fOut.delete();
        }

        ImageOutputStream output = null;
        try {
            output = new FileImageOutputStream(fOut);
        } catch (IOException e) {
            System.out.println("error gif " + e);
            e.printStackTrace();
        }

        try {
            GifSequenceWriter writer = new GifSequenceWriter(output,
                    firstImage.getType(), +frameTime + "", true);

            writer.writeToSequence(firstImage);
            for (int i = 1; i < obis.length; i++) {
                BufferedImage nextImage = obis[i];
                writer.writeToSequence(nextImage);
            }

            writer.close();
            output.close();
            System.out.println("saved gif " + out);

        } catch (IOException e) {
            System.out.println("error gif " + e);
            e.printStackTrace();
        }

        //Base.savePNGFile(obi, animDir+obj+".png", 300);
    }


}
