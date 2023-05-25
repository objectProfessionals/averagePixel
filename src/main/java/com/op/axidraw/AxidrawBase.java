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
import java.util.HashMap;

import static java.awt.Color.*;
import static java.lang.Math.*;

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
    protected HashMap<Color, Point2D.Double> paintPos = new HashMap<>();

    protected ColorSpace instance = null;
    protected String pathToCMYKProfile = host + "UncoatedFOGRA29.icc";
    protected String opFileName = "";

    protected void startSVGPath(String layer, double xBordermm, double yBordermm) {
        this.xBordermm = xBordermm;
        this.yBordermm = yBordermm;
        double xB = dp(xBordermm * scale);
        double yB = dp(yBordermm * scale);
        writer.println("<g style=\"display:inline\" inkscape:label=\"" + layer + "\" id=\"" + layer + "-layer\" " +
                "transform=\"translate(" + xB + ", " + yB + ")\" " +
                "inkscape:groupmode=\"layer\">");
        writer.println("<path id=\"path\" d=\"");

        setupPaintPos();
    }

    protected void endSVGPath(Color col, double strokeWidth) {
        String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
        writer.print("\" style=\"fill:none;stroke:" + hex + "; stroke-width:" + dp(strokeWidth) + "\" />");
        writer.print("</g>");
    }

    protected void moveTo(double x, double y) {
        writer.println("M" + dp(x) + " " + dp(y) + " ");
    }

    protected void lineTo(double x, double y) {
        writer.println("L" + dp(x) + " " + dp(y) + " ");
    }

    protected void circleAt(double cx, double cy, double r) {
        writer.println("M " + dp(cx) + " " + dp(cy) + " m -" + dp(r) + ", 0 a " + dp(r) + "," + dp(r)
                + " 0 1,0 " + dp(r * 2) + ",0 a " + dp(r) + "," + dp(r) + " 0 1,0 -" + dp(r * 2) + ",0");
    }

    protected void circleAt(double cx, double cy, double r, double ang) {
        //(rx ry x-axis-rotation large-arc-flag sweep-flag x y)
        writer.println("M " + dp(cx) + " " + dp(cy) + " m -" + dp(r) + ", 0 a " + dp(r) + "," + dp(r)
                + " 0 1,0 " + dp(r * 2) + ",0 a " + dp(r) + "," + dp(r) + " 0 1,0 -" + dp(r * 2) + ",0");
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

    protected void startSVG(String fileName, int imageWmm, int imageHmm, int paperWmm, int paperHmm) throws FileNotFoundException, UnsupportedEncodingException {
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
        paintPos = new HashMap<>();
        double totW = paperWmm * scale;
        double xmm = 10;
        double ymm = 10;
        double ySepmm = 30;
        double xOff = xmm * scale;
        double yOff = ymm * scale;
        double xB = (xBordermm * scale);
        double yB = (yBordermm * scale);
        double yS = (ySepmm * scale);
        Point2D.Double black = new Point2D.Double(totW - xB - xOff, yOff - yB);
        paintPos.put(BLACK, black);
        Point2D.Double cyan = new Point2D.Double(totW - xB - xOff, yOff - yB + yS);
        paintPos.put(CYAN, cyan);
        Point2D.Double magenta = new Point2D.Double(totW - xB - xOff, yOff - yB + yS * 2);
        paintPos.put(MAGENTA, magenta);
        Point2D.Double yellow = new Point2D.Double(totW - xB - xOff, yOff - yB + yS * 3);
        paintPos.put(YELLOW, yellow);
        Point2D.Double water = new Point2D.Double(totW - xB - xOff, yOff - yB + yS * 4);
        paintPos.put(WHITE, water);
    }

    protected double getGrey(double cx, double cy) {
        if ((int) cx >= imageW || (int) cy >= imageH || (int) cx < 0 || (int) cy < 0) {
            return 0;
        }
        int rgb = ibi.getRGB((int) cx, (int) cy);
        Color c = new Color(rgb);
        double R = (double) (c.getRed()) / 255.0;
        double G = (double) (c.getGreen()) / 255.0;
        double B = (double) (c.getBlue()) / 255.0;
        double grey = ((R + G + B) / 3.0);
        return grey;
    }

    protected double getAverageColor(double x, double y, double r, Color penCol) {
        double i = 0;
        double totGrey = 0;
        double greyAngInc = 30;
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
                if (penCol.equals(BLACK)) {
                    totGrey = totGrey + ((R + G + B) / 3.0);
                } else {
                    float[] rgbF = {(float) R, (float) G, (float) B};
                    float[] cmyk = rgbToCmyk(rgbF);

                    if (penCol.equals(CYAN)) {
                        totGrey = totGrey + (cmyk[0]);
                    } else if (penCol.equals(Color.MAGENTA)) {
                        totGrey = totGrey + (cmyk[1]);
                    } else if (penCol.equals(Color.YELLOW)) {
                        totGrey = totGrey + (cmyk[2]);
                    }
                }
                i++;
            }
        }


        double grey = totGrey / i;
        return grey;
    }

    protected Color getBestCMYKColor(double xc, double yc, double r) {
        double i = 0;
        float totC = 0;
        float totM = 0;
        float totY = 0;
        float totK = 0;
        double greyAngInc = 30;
        for (double ang = 0; ang < 360; ang = ang + greyAngInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = xc + rr * Math.cos(Math.toRadians(ang));
                double yy = yc + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= imageW || (int) yy >= imageH || (int) xx < 0 || (int) yy < 0) {
                    continue;
                }
                int rgb = ibi.getRGB((int) xx, (int) yy);
                Color col = new Color(rgb);
                double R = (double) (col.getRed()) / 255.0;
                double G = (double) (col.getGreen()) / 255.0;
                double B = (double) (col.getBlue()) / 255.0;
                float[] rgbF = {(float) R, (float) G, (float) B};
                float[] cmyk = rgbToCmyk(rgbF);
                float c = cmyk[0];
                float m = cmyk[1];
                float y = cmyk[2];
                float k = cmyk[3];
                if (largest(c, m, y, k)) {
                    totC = totC + c;
                } else if (largest(m, c, y, k)) {
                    totM = totM + c;
                } else if (largest(y, c, m, k)) {
                    totY = totY + y;
                } else {
                    totK = totK + k;
                }
                i++;
            }
        }

        if (largest(totC, totM, totY, totK)) {
            return CYAN;
        } else if (largest(totM, totC, totY, totK)) {
            return MAGENTA;
        } else if (largest(totY, totC, totM, totK)) {
            return YELLOW;
        } else {
            return BLACK;
        }
    }

    private boolean largest(float a, float a1, float a2, float a3) {
        if (a > a1 && a > a2 && a > a3) {
            return true;
        }

        return false;
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

    protected void dipPaint(Color col, int numTimes) {
        for (int i = 0; i < numTimes; i++) {
            dipPaint(col);
            moveTo(imageW, imageH);
        }

    }

    protected void dipPaint(Color col) {
        double rad = paintDipRadmm * scale;
        Point2D.Double paint = paintPos.get(col);
        circleAt(paint.x, paint.y, rad);
    }

    protected void paintFilledCircle(double cx, double cy, double rad, double ang, double num) {
        //circleAt(cx, cy, rad);

        double cosA = cos(toRadians(ang));
        double sinA = sin(toRadians(ang));
        double rad2 = rad * 2;
        boolean first = true;
        boolean l2r = true;
        double lastX = 0;
        double lastY = 0;
        for (double n = -num * 2; n <= num * 2; n++) {
            double r = rad * (2 * n / num);
            double ccx = cx + r * sinA;
            double ccy = cy - r * cosA;
            //circleAt(ccx, ccy, 1);
            for (double rr = rad2; rr > 0; rr = rr - 1) {
                double rx1 = ccx + rr * cosA;
                double ry1 = ccy + rr * sinA;
                double rx2 = ccx - rr * cosA;
                double ry2 = ccy - rr * sinA;
                if (intersectsForFilledCircle(rx1, ry1, rx2, ry2, cx, cy, rad)) {
                    if (first) {
                        moveTo(rx1, ry1);
                        lineTo(rx2, ry2);
                        first = false;
                        l2r = false;
                    } else {
                        if (l2r) {
                            lineTo(rx1, ry1);
                            lineTo(rx2, ry2);
                        } else {
                            lineTo(rx2, ry2);
                            lineTo(rx1, ry1);
                        }
                        l2r = !l2r;
                    }
                    lastX = rx2;
                    lastY = ry2;
                    //System.out.println(rx1 + "," + ry1 + ":" + rx2 + "," + ry2);
                    break;
                }

            }

        }
    }

    protected boolean intersectsForFilledCircle(double rx1, double ry1, double rx2, double ry2, double cx, double cy, double rad) {
        double test = 0.5;
        double x1 = rx1 - cx;
        double y1 = ry1 - cy;
        double x2 = rx2 - cx;
        double y2 = ry2 - cy;
        if (abs(x1 - x2) < test && abs(y1 - y2) < test) {
            return false;
        }
        double r1 = sqrt(x1 * x1 + y1 * y1);
        double r2 = sqrt(x2 * x2 + y2 * y2);
        if (abs(rad - r1) < test && abs(rad - r2) < test) {
            return true;
        }
        return false;
    }

    protected String getColorName(Color col) {
        if (col.equals(BLACK)) {
            return "-BLACK";
        } else if (col.equals(CYAN)) {
            return "-CYAN";
        } else if (col.equals(MAGENTA)) {
            return "-MAGENTA";
        } else if (col.equals(YELLOW)) {
            return "-YELLOW";
        } else {
            return "-LAYER";
        }
    }


}
