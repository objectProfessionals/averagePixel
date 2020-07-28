package com.op.axidraw;

import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class AxidrawBase extends Base {
    protected PrintWriter writer;

    protected int imageW = 0;
    protected int imageH = 0;
    protected double paperWmm = 0;
    protected double paperHmm = 0;
    protected double totW = 0;
    protected double totH = 0;
    private double paintDipRadmm = 5;
    private double xBordermm = 0;
    private double yBordermm = 0;

    protected int dpF = 2;
    protected BufferedImage ibi;
    protected double scale;
    protected ArrayList<Point2D.Double> paintPos = new ArrayList();

    protected ColorSpace instance = null;
    protected String pathToCMYKProfile = host + "sine/UncoatedFOGRA29.icc";
    protected String opFileName = "";

    protected void startSVGPath(int layer, double xBordermm, double yBordermm) {
        this.xBordermm = xBordermm;
        this.yBordermm = yBordermm;
        double xB = dp(xBordermm * scale);
        double yB = dp(yBordermm * scale);
        writer.println("<g style=\"display:inline\" inkscape:label=\"" + layer + "-Layer\" id=\"" + layer + "-layer\" " +
                "transform=\"translate(" + xB + ", " + yB + ")\" " +
                "inkscape:groupmode=\"layer\">");
        writer.println("<path id=\"path\" d=\"");

        setupPaintPos();
    }

    protected void endSVGPath(Color col) {
        String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
        writer.print("\" style=\"fill:none;stroke:" + hex + "\" />");
        writer.print("</g>");
    }

    protected void moveTo(double x, double y) {
        writer.println("M" + dp(x) + " " + dp(y) + " ");
    }

    protected void lineTo(double x, double y) {
        writer.println("L" + dp(x) + " " + dp(y) + " ");
    }

    protected void circleAt(double cx, double cy, double r) {
        writer.println("M " + cx + " " + cy + " m -" + r + ", 0 a " + r + "," + r + " 0 1,0 " + (r * 2) + ",0 a " + r + "," + r + " 0 1,0 -" + (r * 2) + ",0");
    }

    protected void rect(double x1, double y1, double w, double h) {
        moveTo(x1, y1);
        lineTo(x1 + w, y1);
        lineTo(x1 + w, y1 + h);
        lineTo(x1, y1 + h);
        lineTo(x1, y1);
    }

    protected void corners(double x1, double y1, double w, double h, double corner) {
        moveTo(x1, y1 + corner);
        lineTo(x1, y1);
        lineTo(x1 + corner, y1);

        moveTo(x1 + w, y1 + corner);
        lineTo(x1 + w, y1);
        lineTo(x1 + w - corner, y1);

        moveTo(x1 + w, y1 + h - corner);
        lineTo(x1 + w, y1 + h);
        lineTo(x1 + w - corner, y1 + h);

        moveTo(x1, y1 + h - corner);
        lineTo(x1, y1 + h);
        lineTo(x1 + corner, y1 + h);
    }

    protected void endSVG() {
        writer.println("</svg>");
        writer.close();
        System.out.println(opFileName + " saved");
    }

    protected double dp(double val) {
        BigDecimal bd = BigDecimal.valueOf(val);
        bd = bd.setScale(dpF, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    protected void setup(String ipFile, String opFile, int imageWmm, int imageHmm, int paperWmm, int paperHmm) throws IOException {

        File ip = new File(ipFile);
        ibi = ImageIO.read(ip);
        imageW = ibi.getWidth();
        imageH = ibi.getHeight();

        startSVG(opFile, imageWmm, imageHmm, paperWmm, paperHmm);
    }

    private void startSVG(String fileName, int imageWmm, int imageHmm, int paperWmm, int paperHmm) throws FileNotFoundException, UnsupportedEncodingException {
        opFileName = fileName;
        File op = new File(fileName);
        this.scale = ((double) imageW) / imageWmm;
        this.paperWmm = paperWmm;
        this.paperHmm = paperHmm;
        totW = paperWmm * scale;
        totH = paperHmm * scale;

        writer = new PrintWriter(op, "UTF-8");
        writer.println("<svg width=\"" + dp(totW) + "\" height=\"" + dp(totH) + "\" xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\">" +
                " viewBox=\"0 0 " + dp(totW) + " " + dp(totH) + "\"");
    }

    private void setupPaintPos() {
        paintPos = new ArrayList();
        double totW = paperWmm * scale;
        double xmm = 10;
        double ymm = 10;
        double xOff = xmm * scale;
        double yOff = ymm * scale;
        double xB = (xBordermm * scale);
        double yB = (yBordermm * scale);
        Point2D.Double black = new Point2D.Double(totW -xB- xOff, yOff-yB);
        paintPos.add(black);
    }

    protected double getGrey(double cx, double cy) {
        int rgb = ibi.getRGB((int) cx, (int) cy);
        Color c = new Color(rgb);
        double R = (double) (c.getRed()) / 255.0;
        double G = (double) (c.getGreen()) / 255.0;
        double B = (double) (c.getBlue()) / 255.0;
        double grey = ((R + G + B) / 3.0);
        return grey;
    }

    protected float[] rgbToCmyk(float... rgb) {
        if (rgb.length != 3) {
            throw new IllegalArgumentException();
        }
        if (instance == null) {
            try {
                instance = new ICC_ColorSpace(ICC_Profile.getInstance(pathToCMYKProfile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        float[] fromRGB = instance.fromRGB(rgb);
        return fromRGB;
    }

    protected void dipPaint(int paintInd) {
        double rad = paintDipRadmm * scale;
        Point2D.Double paint = paintPos.get(paintInd);
        circleAt(paint.x, paint.y, rad);
    }
}
