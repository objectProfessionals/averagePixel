package com.op.pack;

import com.op.Base;
import com.op.HeartShape;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class RBGCirclePack extends Base {

    private static RBGCirclePack circlePack = new RBGCirclePack();
    //  R.A. Robertson 2012.03 "Circle Packing 3" ~ www.rariora.org ~
    private String dir = host + "";
    private String ipFile = "VirgaCol";
    private String opFile = "CirclePack";
    private int w = 0;
    private int h = 0;
    private double dpi = 300;
    private BufferedImage ibi;
    private BufferedImage obi;
    private Graphics2D opG;

    private BufferedImage obi1;
    private Graphics2D opG1;

    ArrayList<Circle> circleList = new ArrayList<>();
    Color ground = new Color(255, 255, 255, 255);  // Background color.
    Color fill = Color.BLACK;  // Background color.
    int maxCircles = 0;
    double minR = 20;
    double maxR = 75;
    double spacer = 0;
    double angInc = 10;
    double alpha = 0.5;
    double maxCirclesF = 1;// 3=20 75 0 10 0.25; 2= 10 30 0

    public static void main(String[] args) throws Exception {
        circlePack.run();
    }

    private void run() throws Exception {
        doRGB();
        save();
    }

    private void doRGB() throws IOException {
        String colors[] = {"red", "green", "blue"};
        double mr = minR;
        for (String color : colors) {
            setup(color);
            drawAll(color);
            minR = mr;
        }
//        color = "blue";
//        setup(color);
//        drawAll(color);

        //drawAllCircles();
    }

    private void drawAllCircles() {
        for (Circle circle : circleList) {
            drawOne(circle.x, circle.y, circle.r, circle.col);
        }
    }

    private void drawAll(String col) {
        for (int i = 0; i < maxCircles; i++) {
            draw(i, col);
        }
        opG.drawImage(obi1, null, null);
    }


    void setup(String col) throws IOException {

        File ip = new File(dir + ipFile + "-"+col+".jpg");
        ibi = ImageIO.read(ip);
        w = ibi.getWidth();
        h = ibi.getHeight();
        maxCircles = (int) (((double) w) * maxCirclesF);

        if (opG == null) {
            obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            opG = (Graphics2D) obi.getGraphics();
            opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            opG.setColor(ground);
            opG.fillRect(0, 0, w, h);
            AlphaComposite myAlpha = AlphaComposite.getInstance(
                    AlphaComposite.XOR, (float)alpha);
            opG.setComposite(myAlpha);
        }
        obi1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG1 = (Graphics2D) obi1.getGraphics();
        opG1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG1.setColor(ground);
        opG1.fillRect(0, 0, w, h);
    }

    int draw(int numTry, String color) {
        int tryCount = 0;
        double ww = (double) w;
        double hh = (double) h;
        boolean hasGroundOnlyColor;
        double r = minR + Math.random() * maxR;
        double x = Math.random() * ww;
        double y = Math.random() * hh;
        double rr = r + spacer;
        hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);

        while (!hasGroundOnlyColor && rr > minR) {
            rr--;
            hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);
            tryCount++;
            //System.out.println("rr"+rr);
            if (!hasGroundOnlyColor && rr <= minR) {
                r = minR + Math.random() * maxR;
                x = Math.random() * ww;
                y = Math.random() * hh;
                rr = r + spacer;
                //System.out.println("tryAgain");
                hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);
            }
        }

        if (hasGroundOnlyColor) {
            Color col = getAverageColor(color, x, y, r);
            circleList.add(new Circle(x, y, r, col));
            drawOne(x, y, rr, col);
            //drawOneSVG(x, y, rr, col);
        }
        if (tryCount > 200000) {
            minR = Math.max(2, minR - 1);
        }
        System.out.println("hasOnlyGroundColor:" + hasGroundOnlyColor + " numTry:" + numTry + " circles:" + circleList.size() + " tryCount:" + tryCount + " minRad:" + minR);


        return tryCount;
    }

    private void drawOne(double x, double y, double rr, Color col) {
        int rrr = (int) (rr - spacer);

        opG1.setColor(col);
        Shape shape = getShape(x, y, rrr);
        opG1.fill(shape);

//        opG.setColor(col.darker());
//        opG.setStroke(new BasicStroke((float) (minR * strokeF)));
//        opG.draw(shape);
    }

    private Shape getShape(double x, double y, int rrr) {
        return new Ellipse2D.Double((int) (x - rrr), (int) (y - rrr), rrr * 2, rrr * 2);
    }

    private Shape getShapeHeart(double x, double y, int rrr) {
        int size = 9 * rrr / 10;
        int off = 1 * rrr / 10;
        Shape heart = new HeartShape().getShape(size, off, (int) x, (int) y);
        return heart;
    }

    private Color getAverageColor(String color, double x, double y, double r) {
        Color aveCol = Color.BLACK;
        double i = 0;
        double totR = 0;
        double totG = 0;
        double totB = 0;
        for (double ang = 0; ang < 360; ang = ang + angInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= w || (int) yy >= h || (int) xx < 0 || (int) yy < 0) {
                    continue;
                }
                int rgb = ibi.getRGB((int) xx, (int) yy);
                Color c = new Color(rgb);
                totR = totR + (double) (c.getRed()) / 255.0;
                totG = totG + (double) (c.getGreen()) / 255.0;
                totB = totB + (double) (c.getBlue()) / 255.0;
                i++;
            }
        }


        if ("red".equals(color)) {
            aveCol = new Color(getRGB(totR, i),0, 0);
        } else if ("green".equals(color)) {
            aveCol = new Color(0, getRGB(totG, i), 0);
        } else if ("blue".equals(color)) {
            aveCol = new Color(0, 0, getRGB(totB, i));
        }
        return aveCol;
    }

    private int getRGB(double totR, double i) {
        return (int) ((totR / i) * 255.0);
    }

    private boolean hasOnlyGroundColor(double x, double y, double r) {
        int rgb = obi1.getRGB((int) x, (int) y);
        Color colorTest = new Color(rgb);
        boolean hasGroundColor = (colorTest.equals(ground));
        if (!hasGroundColor) {
            return false;
        }

        for (double ang = 0; ang < 360; ang = ang + angInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= w || (int) yy >= h || (int) xx < 0 || (int) yy < 0) {
                    continue;
                }
                rgb = obi1.getRGB((int) xx, (int) yy);
                colorTest = new Color(rgb);
                hasGroundColor = (colorTest.equals(ground));
                if (!hasGroundColor) {
                    return false;
                }
            }
        }

        return true;
    }

/* ======================= Circle Class ======================= */

    class Circle {
        double x, y, r;
        Color col;

        Circle(double x, double y, double r, Color c) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.col = c;
            //ellipse(x, y, r, r);  // Turn off and run with lines only for variety.
        }
    }

    private void save() throws Exception {
        File op1 = new File(dir + ipFile + "_" + opFile + ".png");
        savePNGFile(obi, op1, dpi);
    }


}
