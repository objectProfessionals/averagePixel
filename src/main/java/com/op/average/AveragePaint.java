package main.java.com.op.average;


import main.java.com.op.Base;
import main.java.com.op.HeartShape;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AveragePaint extends Base {

    private static AveragePaint averagePaint = new AveragePaint();

    private String dir = host + "";
    private String ipFile = "Virga";
    private String opFilePre = "Out";
    private int w = 0;
    private int h = 0;
    private Type type = Type.LINE;
    private String opFile = opFilePre + type.name();

    private double scale = 12;
    private int radStart = 0;
    private double radMin = 0;
    private double lowThreshold = 30.0;//300
    private boolean addBorder = false;
    private BufferedImage obi;
    private Graphics2D opG;

    private Path2D.Double path = new Path2D.Double();
    private ArrayList<Circle> circles = new ArrayList<Circle>();
    private int varianceF = 2; // 2

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        averagePaint.draw();

    }

    public void draw() throws IOException {
        File ip = new File(dir + ipFile + ".jpg");
        BufferedImage bi = ImageIO.read(ip);
        w = bi.getWidth();
        h = bi.getHeight();

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);

        radStart = (int) (((double) w) / scale);
        radMin = radStart / scale;

        if (type == Type.HEX) {
            paintAllHex(bi);
        } else {
            paintVariance(bi, 0, 0, w, h, radStart);
        }

        if (type == Type.SCRIB) {
            drawScibble();
        }

        save();
    }

    private void drawScibble() {
        Collections.sort(circles);
        Circle c1 = circles.get(0);
        path.moveTo(c1.x, c1.y);
        for (Circle c : circles) {
            drawCurveToPath((int) c.x, (int) c.y, (int) c.rad, c.c);
        }
        if (addBorder) {
            opG.setColor(Color.BLACK);
            opG.setStroke(new BasicStroke(5));
            opG.draw(path);
        }
    }

    private void paintAllHex(BufferedImage bi) {
        int rad = (int) radStart;
        double x = rad;
        double y = rad;
        int numX = 0;
        int numY = 0;
        boolean calcNumY = true;
        boolean toggleOff = false;
        while (x < w - rad) {
            y = rad;
            while (y < h - rad) {
                double yy = y;
                if (toggleOff) {
                    yy = y + rad;
                }
                paintOneHex(bi, rad, (int) x, (int) y);
                // drawRotatedScaled(x, yy, Math.PI, 1.1);
                y = y + rad;
                if (calcNumY) {
                    numY++;
                }
            }
            x = x + rad * 1.5;
            toggleOff = !toggleOff;
            calcNumY = false;
            numX++;
        }

    }

    private void paintOneHex(BufferedImage bi, int rad, int x, int y) {
        double var = getSD(bi, x, y, rad);
        if (var < lowThreshold) {
            paintHex(bi, x, y, rad);
        } else {
            if (rad > 50) {
                paintInnerHex(bi, rad, x, y);
            } else {
                paintHex(bi, x, y, rad);
            }
        }
    }

    private void paintInnerHex(BufferedImage bi, int rad, int x, int y) {
        paintHex(bi, x, y, rad / 2);
    }

    private void paintHex(BufferedImage bi, int x, int y, int rad) {
        int dia = rad * 2;
        if (x + dia > bi.getWidth() || y + dia > bi.getHeight()) {
            return;
        }
        BufferedImage sub = bi.getSubimage(x, y, dia, dia);
        int r = (int) meanValue(sub, 0);
        int g = (int) meanValue(sub, 1);
        int b = (int) meanValue(sub, 2);
        Color c = new Color(r, g, b);
        opG.setColor(c);
        HexShape hex = new HexShape(x, y, rad);
        opG.fill(hex);
    }

    private void paintVariance(BufferedImage bi, int x1, int y1, int ww,
                               int hh, int rad) {
        for (int y = y1; y < y1 + hh - rad; y = y + 2 * rad) {
            for (int x = x1; x < x1 + ww - rad; x = x + 2 * rad) {
                paintOne(bi, rad, x, y);
            }
        }
    }

    private void paintOne(BufferedImage bi, int rad, int x, int y) {
        double var = getSD(bi, x, y, rad);
        if (var < lowThreshold) {
            paintOneType(bi, x, y, rad);
        } else {
            if (rad > radMin) {
                paintVariance(bi, x, y, rad * 2, rad * 2, rad / varianceF);
            } else {
                paintOneType(bi, x, y, rad);
            }
        }
    }

    private void paintOneType(BufferedImage bi, int x, int y, int rrad) {
        int dia = rrad * 2;
        System.out.println("x,y,r=" + x + "," + y + "," + rrad);

        if (x + dia >= bi.getWidth() || y + dia >= bi.getHeight() || dia <= 0) {
            return;
        }
        BufferedImage sub = bi.getSubimage(x, y, 2 * rrad, 2 * rrad);
        int r = (int) meanValue(sub, 0);
        int g = (int) meanValue(sub, 1);
        int b = (int) meanValue(sub, 2);
        Color c = new Color(r, g, b);
        opG.setColor(c);
        if (type == Type.SCRIB) {
            setScribble(x, y, rrad, c);
        } else if (type == Type.CIRCLE) {
            drawCircle(x, y, rrad, c);
        } else if (type == Type.LINE) {
            drawLine(x, y, rrad, c);
        } else if (type == Type.RECT) {
            opG.setColor(getAlphaColor(c, 225));
            int border = rrad / 5;
            opG.fillRoundRect(x + border, y + border, 2 * (rrad - border),
                    2 * (rrad - border), border * 4, border * 4);

            if (addBorder) {
                opG.setStroke(new BasicStroke(rrad / 20));
                opG.setColor(c);
                opG.drawRoundRect(x + border, y + border, 2 * (rrad - border),
                        2 * (rrad - border), border * 4, border * 4);
            }
        } else if (type == Type.HEART) {
            int size = 9 * rrad / 10;
            int off = 1 * rrad / 10;
            Shape heart = new HeartShape().getShape(size, off, x, y);
            opG.setColor(getAlphaColor(c, 200));
            opG.fill(heart);
            if (addBorder) {
                opG.setStroke(new BasicStroke(rrad / 20));
                opG.setColor(c);
                opG.draw(heart);
            }
        }
    }

    private void setScribble(int x, int y, double rad, Color c) {
        Circle cir = new Circle(circles.size(), x, y, rad, c);
        circles.add(cir);
    }

    private void drawCurveToPath(int x, int y, double rad, Color c) {
        double rrad = 2 * rad * 1;
        double x1 = (x);
        double y1 = (y);
        double x2 = (x + rrad);
        double y2 = (y);
        double x3 = (x + rrad);
        double y3 = (y + rrad);
        double x4 = (x);
        double y4 = (y + rrad);

        double xy1[] = {x1, y1};
        double xy2[] = {x2, y2};
        double xy3[] = {x3, y3};
        double xy4[] = {x4, y4};

        double coords[][] = {xy1, xy2, xy3, xy4};

        int grey = 255 - (c.getRed() + c.getGreen() + c.getBlue()) / 3;
        grey = grey / 50;
        //double variationsF = 4.0;
        double variationsF = (rad/radMin)/3;
        for (int i = 0; i < grey; i++) {
            int r1 = (int) (Math.random() * variationsF);
            double[] coord1 = coords[r1];
            int r2 = (int) (Math.random() * variationsF);
            double[] coord2 = coords[r2];
            int r3 = (int) (Math.random() * variationsF);
            double[] coord3 = coords[r3];
            path.curveTo(coord1[0], coord1[1], coord2[0], coord2[1], coord3[0],
                    coord3[1]);
            opG.drawRect(x, y, (int) rad * 2, (int) rad * 2);
        }
    }

    private void drawCircle(int x, int y, int rrad, Color c) {
        opG.setColor(getAlphaColor(c, 225));
        opG.fillOval(x, y, 2 * rrad, 2 * rrad);

        if (addBorder) {
            opG.setStroke(new BasicStroke(rrad / 20));
            opG.setColor(c);
            opG.drawOval(x, y, 2 * rrad, 2 * rrad);
        }
    }

    private void drawLine(int x, int y, int rrad, Color c) {
        opG.setColor(Color.BLACK);
        //opG.setColor(getAlphaColor(c, 225));
        double r = rrad;
        Path2D p = new Path2D.Double();
        p.moveTo(r, r*2);
        p.lineTo(r, 0);
        AffineTransform tr = new AffineTransform();
        AffineTransform mv = AffineTransform.getTranslateInstance(x, y);
        double f = ((c.getRed() + c.getBlue() + c.getGreen())/3.0)/255.0;
        AffineTransform ro = AffineTransform.getRotateInstance(Math.PI * f, r, r);
        tr.concatenate(mv);
        tr.concatenate(ro);
        p.transform(tr);
        //opG.clip(new Ellipse2D.Double(x, y, 2 * rrad, 2 * rrad));
        opG.setStroke(new BasicStroke(5));
        //System.out.println("M"+p.);
        opG.draw(p);
    }

    private Color getAlphaColor(Color orig, int alpha) {
        Color newC = new Color(orig.getRed(), orig.getGreen(), orig.getBlue(),
                alpha);
        return newC;
    }

    private void save() throws IOException {
        File op1 = new File(dir + ipFile + opFile + ".jpg");
        ImageIO.write(obi, "jpg", op1);
        System.out.println("Saved " + op1.getPath());
    }


    /* Red Standard Deviation */
    public double standardDeviationRed(BufferedImage image, int ind) {
        return Math.sqrt(getVariance(image, ind));
    }

    private class Circle implements Comparable<Circle> {
        private int n = 0;
        private double x = 0;
        private double y = 0;
        private double rad = 0;
        private Color c;

        Circle(int n, double x, double y, double rad, Color c) {
            this.n = n;
            this.x = x;
            this.y = y;
            this.rad = rad;
            this.c = c;
        }

        @Override
        public int compareTo(Circle c) {
            //return (int) (this.rad - c.rad);
            return (int) (this.n - c.n);
        }
    }

    private class HexShape extends Polygon {
        private static final long serialVersionUID = 8174789653342556173L;
        double cos60 = Math.cos(Math.PI / 3);

        HexShape(double cx, double cy, double rad) {
            super();
            double radXOut = rad;
            double radYOut = rad;
            int x1 = (int) (cx + radXOut * cos60);
            int y1 = (int) (cy - radYOut);
            int x2 = (int) (cx + radXOut);
            int y2 = (int) (cy);
            int x3 = (int) (cx + radXOut * cos60);
            int y3 = (int) (cy + radYOut);
            int x4 = (int) (cx - radXOut * cos60);
            int y4 = (int) (cy + radYOut);
            int x5 = (int) (cx - radXOut);
            int y5 = (int) (cy);
            int x6 = (int) (cx - radXOut * cos60);
            int y6 = (int) (cy - radYOut);
            this.addPoint(x1, y1);
            this.addPoint(x2, y2);
            this.addPoint(x3, y3);
            this.addPoint(x4, y4);
            this.addPoint(x5, y5);
            this.addPoint(x6, y6);
        }
    }

    public enum Type {
        CIRCLE("CIR"),
        RECT("RECT"),
        LINE("LINE"),
        HEART("HEART"),
        SCRIB("SCRIB"),
        HEX("HEX");

        final String name;

        Type(String name) {
            this.name = name;
        }
    }
}
