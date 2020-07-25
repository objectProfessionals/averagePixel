package com.op.string;

import com.op.Base;
import com.op.PathLength;
import org.apache.batik.transcoder.TranscoderException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class StringPaint extends Base {

    private static StringPaint stringPaint = new StringPaint();
    //  R.A. Robertson 2012.03 "Circle Packing 3" ~ www.rariora.org ~
    private String dir = host + "sine/";
    private String ipFile = "VirgaColFace500";
    private String opFile = ipFile + "_STRING";
    private int w = 0;
    private int h = 0;
    private int ww = 0;
    private int hh = 0;
    private double scaleOrig = 1;
    private double dpi = 300;
    private BufferedImage ibi;
    private BufferedImage obi;
    private Graphics2D opG;
    private double angInc = 15;
    private double border = -1;
    private double greyAngInc = 15;
    private double borderF = 0.025;
    private double delta = 5; //5;
    private double deltaY = delta;
    private double deltaX = delta * 0.05;
    private double deltaAmp = delta * 0.5;
    private double deltaGreyR = delta * 1;
    private float stroke = 1f;
    private Random random = new Random(2);
    private ColorSpace instance = null;
    private String pathToCMYKProfile = dir + "UncoatedFOGRA29.icc";

    private PrintWriter writer;
    private boolean allColors = true;
    private double maxPoints = 200;
    double maxChange = 25;

    public static void main(String[] args) throws Exception {
        stringPaint.run();
    }

    private void run() throws Exception {
        setup();

        if (allColors) {
            drawSingle(0);
            drawSingle(1);
            drawSingle(2);
            drawSingle(3);
        } else {
            drawSingle(0);
        }

        save();
    }

    private void drawSingle(double i) {
        Path2D.Double pathOut = new Path2D.Double();

        Path2D path = calculatePath();
        PathLength pathLength = new PathLength(path);
        float len = pathLength.lengthOfPath();
        double x1 = 0;
        double y1 = 0;
        for (float p = 0f; p< len; p++) {
            Point2D point2D = pathLength.pointAtLength(p);
            double xx = point2D.getX();
            double yy = point2D.getY();
            if (p == 0) {
                x1 = xx;
                y1 = yy;
                continue;
            }
            double[] cmyk = getCMYK(xx, yy);
            double c = cmyk[0];
            double m = cmyk[1];
            double y = cmyk[2];
            double k = cmyk[3];
            double max = Math.max(Math.max(Math.max(c, m), y), k);
            double ii = 0;
            for (double cc : cmyk) {
                if (ii == i && cc == max) {
                    pathOut.moveTo(x1, y1);
                    pathOut.lineTo(xx, yy);
                } else {
                    //System.out.println("XX");
                }
                x1 = xx;
                y1 = yy;
                ii++;
            }
        }
        Color[] cols = {Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK};
        opG.setColor(cols[(int) i]);
        opG.setStroke(new BasicStroke(1));
        opG.draw(pathOut);
    }

    private Path2D.Double calculatePath() {
        Path2D.Double path = new Path2D.Double();
        double x1 = w * random.nextDouble();
        double y1 = h * random.nextDouble();
        path.moveTo(x1, y1);
        double b = maxChange*0.5;
        Rectangle2D.Double rect = new Rectangle2D.Double(b, b, w-b, h-b);
        for (double i =0; i< maxPoints; i ++){
            Path2D.Double pathT = new Path2D.Double();
            double x2 = x1 -(maxChange*0.5) + maxChange * random.nextDouble();
            double y2 = y1 -(maxChange*0.5) + maxChange * random.nextDouble();
            double x3 = x1 -(maxChange*0.5) + maxChange * random.nextDouble();
            double y3 = y1 -(maxChange*0.5) + maxChange * random.nextDouble();
            pathT.moveTo(x1, y1);
            pathT.quadTo(x2, y2, x3, y3);
            if (rect.contains(pathT.getBounds2D())) {
                path.quadTo(x2, y2, x3, y3);
            }
            x1 = w * random.nextDouble();
            y1 = h * random.nextDouble();
        }
        return path;
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

            System.out.println("x1,y1,x2,y2:" + x1 + "," + y1 + "," + (x1 + xD) + "," + (y1 + yD) + " darkness=" + darkness);
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
            path.append(new Arc2D.Double(dp(2 * border + (x * scaleOrig)), dp(2 * border + (y * scaleOrig)), dp(deltaY * scaleOrig),
                    dp(deltaY * scaleOrig), 90, -180, Arc2D.OPEN), true);
            writer.println("A"+dp(deltaY * scaleOrig*0.5)+" "+dp(deltaY * scaleOrig*0.5) +" 0 0 1 "+dp(2 * border + x*scaleOrig)+ " "+ dp(2* border+y*scaleOrig));
        } else {
            path.append(new Arc2D.Double(dp(2 * border + (x * scaleOrig) - (deltaY * scaleOrig)), dp(2 * border + (y * scaleOrig)), dp((deltaY * scaleOrig)),
                    dp(deltaY * scaleOrig), 90, 180, Arc2D.OPEN), true);
            writer.println("A"+dp(deltaY * scaleOrig*0.5)+" "+dp(deltaY * scaleOrig*0.5) +" 0 0 1 "+dp(2 * border - x*scaleOrig)+ " "+ dp(2* border+y*scaleOrig));
        }
    }

    private void drawLine(Path2D.Double path, double x1, double y1, double x2, double y2, boolean l2r) {
        //opG.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));

        if (path.getCurrentPoint() == null) {
            path.moveTo(dp(2 * border + (x1 * scaleOrig)), dp(2 * border + (y1 * scaleOrig)));
            path.lineTo(dp(2 * border + (x2 * scaleOrig)), dp(2 * border + (y2 * scaleOrig)));
            writer.println("M"+dp(2 * border + (x1 * scaleOrig))+" "+dp(2 * border + (y1 * scaleOrig))+" ");
            writer.println("L"+dp(2 * border + (x2 * scaleOrig)) +" "+ dp(2 * border + (y2 * scaleOrig))+" ");
        } else {
            path.lineTo(dp(2 * border + (x2 * scaleOrig)), dp(2 * border + (y2 * scaleOrig)));
            writer.println("L"+dp(2 * border + (x2 * scaleOrig)) +" "+ dp(2 * border + (y2 * scaleOrig))+" ");
        }
    }

    private double dp(double p) {
        BigDecimal bd = BigDecimal.valueOf(p);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private double[] getCMYK(double x, double y) {
        double[] arr = {0, 0, 0, 0};
        int rgb = ibi.getRGB((int) x, (int) y);
        Color c = new Color(rgb);
        double R = (double) (c.getRed()) / 255.0;
        double G = (double) (c.getGreen()) / 255.0;
        double B = (double) (c.getBlue()) / 255.0;
        double grey = ((R + G + B) / 3.0);
        float[] rgbF = {(float) R, (float) G, (float) B};
        float[] cmyk = rgbToCmyk(rgbF);
        arr[0] = cmyk[0];
        arr[1] = cmyk[1];
        arr[2] = cmyk[2];
        arr[3] = grey;

        return arr;
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

    public float[] rgbToCmyk(float... rgb) {
        if (rgb.length != 3) {
            throw new IllegalArgumentException();
        }

        float[] fromRGB = instance.fromRGB(rgb);
        return fromRGB;
    }


    void setup() throws IOException {

        File ip = new File(dir + ipFile + ".jpg");
        ibi = ImageIO.read(ip);
        w = ibi.getWidth();
        h = ibi.getHeight();


        border = 0.5 * ((double) w * scaleOrig) * borderF;

        ww = (int) (4 * border + w * scaleOrig);
        hh = (int) (4 * border + h * scaleOrig);

        obi = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, ww, hh);
        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(stroke));

        try {
            instance = new ICC_ColorSpace(ICC_Profile.getInstance(pathToCMYKProfile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        startSVG();
    }

    private void save() throws Exception {
        File op1 = new File(dir + opFile + ".png");
        savePNGFile(obi, op1, dpi);

        endSVG();
    }

    private void startSVG() throws FileNotFoundException, UnsupportedEncodingException {
        File op = new File(dir + opFile + ".svg");

        writer = new PrintWriter(op, "UTF-8");
        writer.println("<svg width=\"" + ww + "\" height=\"" + hh + "\" xmlns=\"http://www.w3.org/2000/svg\">");
    }

    private void startSVGPath() {
        writer.println("<path id=\"path\" d=\"");

    }
    private void endSVGPath(Color col) {
        String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
        writer.print("\" style=\"fill:none;stroke:"+hex+"\" />");
    }
    private void endSVG() throws IOException, TranscoderException {

        writer.println("</svg>");
        writer.close();
    }
}
