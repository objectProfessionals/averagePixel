package main.java.com.op.average;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class HexPaint extends Base {

    private static HexPaint hexPaint = new HexPaint();

    private String dir = host+"";
    private String ipFile = "Virga";
    private String opFile = "Out";
    private int w = 0;
    private int h = 0;
    private static final int TYPE_CIRCLE = 0;
    private static final int TYPE_RECT = 1;
    private static final int TYPE_HEART = 2;
    private static final int TYPE_SCRIBBLE = 3;
    private int type = TYPE_RECT;

    private double scale = 12;
    private int radStart = 0;
    private double radMin = 0;
    private double lowThreshold = 500.0;
    private BufferedImage obi;
    private Graphics2D opG;

    private Path2D.Double path = new Path2D.Double();
    private ArrayList<Circle> circles = new ArrayList<Circle>();

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        hexPaint.readImage();

    }

    public void readImage() throws IOException {
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

        paintVariance(bi, 0, 0, w, h, radStart);

        if (type == TYPE_SCRIBBLE) {
            drawScibble();
        }
        // paintAllHex(bi);

        save();
    }

    private void drawScibble() {
        Collections.sort(circles);
        Circle c1 = circles.get(0);
        path.moveTo(c1.x, c1.y);
        for (Circle c : circles) {
            drawCirclesPath((int) c.x, (int) c.y, (int) c.rad, c.c);
        }
        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(5));
        opG.draw(path);
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
            paintCircle(bi, x, y, rad);
        } else {
            if (rad > radMin) {
                paintVariance(bi, x, y, rad * 2, rad * 2, rad / 2);
            } else {
                paintCircle(bi, x, y, rad);
            }
        }
    }

    private void paintCircle(BufferedImage bi, int x, int y, int rrad) {
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
        if (type == TYPE_SCRIBBLE) {
            setScribble(x, y, rrad, c);
        } else if (type == TYPE_CIRCLE) {
            drawCircle(x, y, rrad, c);
        } else if (type == TYPE_RECT) {
            opG.setColor(getAlphaColor(c, 225));
            int border = rrad / 5;
            opG.fillRoundRect(x + border, y + border, 2 * (rrad - border),
                    2 * (rrad - border), border * 4, border * 4);

            opG.setStroke(new BasicStroke(rrad / 20));
            opG.setColor(c);
            opG.drawRoundRect(x + border, y + border, 2 * (rrad - border),
                    2 * (rrad - border), border * 4, border * 4);
        } else if (type == TYPE_HEART) {
            int size = 9 * rrad / 10;
            int off = 1 * rrad / 10;
            Shape heart = new HeartShape().getShape(size, off, x, y);
            opG.setColor(getAlphaColor(c, 200));
            opG.fill(heart);
            opG.setStroke(new BasicStroke(rrad / 20));
            opG.setColor(c);
            opG.draw(heart);
        }
    }

    private void setScribble(int x, int y, double rad, Color c) {
        Circle cir = new Circle(x, y, rad, c);
        circles.add(cir);
    }

    private void drawCirclesPath(int x, int y, double rad, Color c) {
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
        for (int i = 0; i < grey; i++) {
            int r1 = (int) (Math.random() * 4.0);
            double[] coord1 = coords[r1];
            int r2 = (int) (Math.random() * 4.0);
            double[] coord2 = coords[r2];
            int r3 = (int) (Math.random() * 4.0);
            double[] coord3 = coords[r3];
            path.curveTo(coord1[0], coord1[1], coord2[0], coord2[1], coord3[0],
                    coord3[1]);
            opG.drawRect(x, y, (int) rad * 2, (int) rad * 2);
        }
    }

    private void drawCircle(int x, int y, int rrad, Color c) {
        opG.setColor(getAlphaColor(c, 225));
        opG.fillOval(x, y, 2 * rrad, 2 * rrad);
        opG.setStroke(new BasicStroke(rrad / 20));
        opG.setColor(c);
        opG.drawOval(x, y, 2 * rrad, 2 * rrad);
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

    private double getSD(BufferedImage bi, int x, int y, int rad) {
        // HexShape hex = new HexShape(rad, rad, rad);
        int dia = rad * 2;
        if (x + dia >= bi.getWidth() || y + dia >= bi.getHeight() || dia <= 0) {
            return 255;
        }
        BufferedImage sub = bi.getSubimage(x, y, dia, dia);
        // double varRed = getVariance(sub, 0);
        // double varGreen = getVariance(sub, 1);
        // double varBlue = getVariance(sub, 2);
        // System.out.println("x,y=" + x + "," + y + " rgb=" + varRed + ":"
        // + varGreen + ":" + varBlue);
        double varGrey = getVariance(sub, 4);
        return varGrey;
    }

    public double getVariance(BufferedImage image, int ind) {
        double mean = meanValue(image, ind);
        double sumOfDiff = 0.0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int[] arr = getRGBAG(image, x, y);
                double colour = arr[ind] - mean;
                sumOfDiff += Math.pow(colour, 2);
            }
        }
        return sumOfDiff / ((image.getWidth() * image.getHeight()) - 1);
    }

    private double meanValue(BufferedImage image, int ind) {
        double tot = 0;
        double c = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                tot = tot + getRGBAG(image, x, y)[ind];
                c++;
            }
        }
        return tot / c;
    }

    public int[] getRGBAG(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        int aa = (rgb >>> 24) & 0x000000FF;
        int r = (rgb >>> 16) & 0x000000FF;
        int g = (rgb >>> 8) & 0x000000FF;
        int b = (rgb >>> 0) & 0x000000FF;
        int grey = (r + g + b) / 3;

        int[] arr = {r, g, b, aa, grey};
        return arr;
    }

    /* Red Standard Deviation */
    public double standardDeviationRed(BufferedImage image, int ind) {
        return Math.sqrt(getVariance(image, ind));
    }

    private class Circle implements Comparable<Circle> {
        private double x = 0;
        private double y = 0;
        private double rad = 0;
        private Color c;

        Circle(double x, double y, double rad, Color c) {
            this.x = x;
            this.y = y;
            this.rad = rad;
            this.c = c;
        }

        @Override
        public int compareTo(Circle c) {
            return (int) (this.rad - c.rad);
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

    private class HeartShape implements Shape {
        Path2D.Double p = new Path2D.Double();

        Shape getShape(int size, int off, int x1, int y1) {
            int d = size * 2;
            p.moveTo(off, off + d / 4);
            p.quadTo(off, off, off + d / 4, off);
            p.quadTo(off + d / 2, off, off + d / 2, off + d / 4);
            p.quadTo(off + d / 2, off, off + d * 3 / 4, off);
            p.quadTo(off + d, off, off + d, off + d / 4);
            p.quadTo(off + d, off + d / 2, off + d * 3 / 4, off + d * 3 / 4);
            p.lineTo(off + d / 2, off + d);
            p.lineTo(off + d / 4, off + d * 3 / 4);
            p.quadTo(off, off + d / 2, off, off + d / 4);
            p.closePath();
            AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
            return p.createTransformedShape(at);
        }

        @Override
        public boolean contains(Point2D arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean contains(Rectangle2D arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean contains(double arg0, double arg1) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean contains(double arg0, double arg1, double arg2,
                                double arg3) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Rectangle getBounds() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Rectangle2D getBounds2D() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PathIterator getPathIterator(AffineTransform arg0) {
            return p.getPathIterator(arg0);
        }

        @Override
        public PathIterator getPathIterator(AffineTransform arg0, double arg1) {
            return p.getPathIterator(arg0, arg1);
        }

        @Override
        public boolean intersects(Rectangle2D arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean intersects(double arg0, double arg1, double arg2,
                                  double arg3) {
            // TODO Auto-generated method stub
            return false;
        }
    }
}
