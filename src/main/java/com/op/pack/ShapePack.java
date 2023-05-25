package com.op.pack;

import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

public class ShapePack extends Base {

    private static ShapePack legoPack = new ShapePack();
    //  R.A. Robertson 2012.03 "Circle Packing 3" ~ www.rariora.org ~
    private String dir = host + "shapePack/";
    private String ipFile = "VirgaColSq3";
    private String opFile = "_ShapePack";
    private int w = 0;
    private int h = 0;
    private double dpi = 300;
    private BufferedImage ibi;
    private BufferedImage obi;
    private Graphics2D opG;
    private double brickUnit = 100;
    private double step = 1; //brickUnit / 4;

    ArrayList<RandomShape> shapeSizes = new ArrayList<>();
    ArrayList<RandomShape> brickUsed = new ArrayList<>();
    Color ground = Color.WHITE;  // Background color.
    Color fill = Color.BLACK;  // Background color.
    private int seed = 1;
    private Random random = new Random(seed);
    private double sdMax = 127;
    private float stroke = 2;
    private double pathPointsAngD = 15;
    private double pointsRadF = 0.5;

    private PrintWriter writer;
    private static final String TYPE_A = "A";
    private static final String TYPE_B = "B";
    private static final String TYPE_C = "C";
    private static final String[] ALL_TYPES = {TYPE_A, TYPE_B, TYPE_C};

    public static void main(String[] args) throws Exception {
        legoPack.run();
    }

    private void run() throws Exception {
        setup();
        initBrickSizes();

        //testShape();
        for (int i = 0; i < 20; i++) {
            drawAll(i);
            brickUnit = brickUnit * 0.9; //(10 + random.nextDouble()*10)/20;
        }

        //addShadows();

        save();

        //saveOBJ();
    }

    private void initBrickSizes() {
        RandomShape rA = new RandomShape(TYPE_A);
        RandomShape rB = new RandomShape(TYPE_B);
        RandomShape rC = new RandomShape(TYPE_C);
        //Brick r4x1 = new Brick(new Rectangle2D.Double(0, 0, 4 * brickUnit, 1 * brickUnit), 0, TYPE_4x1);
        shapeSizes.add(rA);
        shapeSizes.add(rB);
        shapeSizes.add(rC);
    }

    private RandomShape randomiseShapePath(RandomShape shape, double xxx, double yyy) {
        RandomShape copy = new RandomShape(xxx, yyy, 0, shape.type);
        double r = brickUnit/2;
        for (double a = 0; a < 360; a = a + pathPointsAngD) {
            double rr = r - (r* pointsRadF * random.nextDouble());
            double dx = rr * Math.cos(Math.toRadians(a));
            double dy = rr * Math.sin(Math.toRadians(a));
            if (a == 0) {
                copy.moveTo(xxx + dx, yyy + dy);
            } else {
                copy.lineTo(xxx + dx, yyy + dy);
            }
        }
        copy.closePath();

        return copy;
    }

    private void drawAll(int i) {
        for (int y = (int)brickUnit; y < h-(int)brickUnit; y = y + (int) (random.nextDouble()* brickUnit)) {
            for (int x = (int)brickUnit; x < w-(int)brickUnit; x = x + (int) 0) {
                int[] xy = draw(x, y, i);
                x = xy[0];
                if (x > w) {
                    x = 0;
                    //y = y + (int) d; //xy[1];
                    break;
                } else {
                    //x = x + (int) d;
                }
            }
        }
    }

    private int[] draw(int x, int y, int c) {
        ArrayList<RandomShape> bricks = randomiseBrickTransform(x, y);


        RandomShape rect = null;
        for (int i = bricks.size() - 1; i >= 0; i--) {
            rect = bricks.get(i);
            if (sdForBrick(rect)) {
                return getBrick(x, y, rect);
            }
        }

        if (c == 1 && onlyGround(rect)) {
            return getBrick(x, y, rect);
        }

        int[] xy = {x + (int)(random.nextDouble() * rect.getBounds().getWidth()), y};
        return xy;
    }

    private ArrayList<RandomShape> randomiseBrickTransform(double x, double y) {
        ArrayList<RandomShape> bricks = new ArrayList<>();


        for (RandomShape shape : shapeSizes) {
            bricks.add(randomiseShapePath(shape, x, y));
        }

        return bricks;
    }

    private int[] getBrick(int x, int y, RandomShape brick) {
        double cornerRad = brickUnit / 5;
        double dotRad = brickUnit * 0.5;
        double off = brickUnit * 0.25;
        System.out.println("drawing brick: "+brick.cx + ", " + brick.cy +" brickUnit:"+brickUnit);
        brickUsed.add(brick);
        Color col = new Color(ibi.getRGB((int)brick.cx, (int)brick.cy));
        opG.setColor(col);

        double c = 1;
        double r = 1;
        opG.fill(brick);
//        opG.setColor(col.darker().darker());
//        opG.setStroke(new BasicStroke(stroke));
//        opG.draw(brick);

        int[] xy = {0, 0};
        xy[0] = x + (int) (random.nextDouble() * brick.getBounds().getWidth());
        xy[1] = (int) (random.nextDouble() * brick.getBounds().getHeight()); //y + (int) rect.getHeight();
        return xy;
    }

    private boolean sdForBrick(RandomShape randomShape) {
        if (!onlyGround(randomShape)) {
            return false;
        }
        double max = getSDColor(randomShape);
        if (max < sdMax) {
            return true;
        }

        return false;
    }

    protected double getSDColor(RandomShape randomShape) {
        if (randomShape.getBounds().x + randomShape.getBounds().getWidth() >= ibi.getWidth()
                || randomShape.getBounds().y + randomShape.getBounds().getHeight() >= ibi.getHeight()) {
            return 255;
        }
        double varRed = getVariance(randomShape, 0);
        double varGreen = getVariance(randomShape,1);
        double varBlue = getVariance(randomShape, 2);
        return (varRed + varGreen + varBlue) / 3;
    }

    protected double getVariance(RandomShape randomShape, int ind) {
        double mean = meanValue(randomShape, ind);
        double sumOfDiff = 0.0;
        double count = 0;
        for (double y = randomShape.getBounds().y; y < randomShape.getBounds().y + randomShape.getBounds().height; y = y + step) {
            for (double x = randomShape.getBounds().x; x < randomShape.getBounds().x + randomShape.getBounds().width; x = x + step) {
                Point2D p = new Point2D.Double(x, y);
                if (randomShape.contains(p)) {
                    if (x<0 || y <0 || x >= w || y >= h) {
                        continue;
                    }
                    int[] arr = getRGBAG(ibi, (int) x, (int) y);
                    double colour = arr[ind] - mean;
                    sumOfDiff += Math.pow(colour, 2);
                    count++;
                }
            }
        }
        return sumOfDiff / (count - 1);
    }

    protected double meanValue(RandomShape randomShape, int ind) {
        double tot = 0;
        double c = 0;
        for (double y = randomShape.getBounds().y; y < randomShape.getBounds().y + randomShape.getBounds().height; y = y + step) {
            for (double x = randomShape.getBounds().x; x < randomShape.getBounds().x + randomShape.getBounds().width; x = x + step) {
                if (x<0 || y <0 || x >= w || y >= h) {
                    continue;
                }
                tot = tot + getRGBAG(ibi, (int)x, (int)y)[ind];
                c++;
            }
        }
        return tot / c;
    }


    private boolean onlyGround(RandomShape randomShape) {
        int str = (int) (stroke);
        for (double yyy = randomShape.getBounds().y - str; yyy < randomShape.getBounds().y + randomShape.getBounds().getHeight() + str; yyy = yyy + step) {
            for (double xxx = randomShape.getBounds().x - str; xxx < randomShape.getBounds().x + randomShape.getBounds().getWidth() + str; xxx = xxx + step) {
                if (xxx<0 || yyy <0 || xxx >= w || yyy >= h) {
                    continue;
                }
                Point2D p = new Point2D.Double(xxx, yyy);
                if (randomShape.contains(p) && !new Color(obi.getRGB((int)xxx, (int)yyy)).equals(ground)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void setup() throws IOException {

        File ip = new File(dir + ipFile + ".jpg");
        ibi = ImageIO.read(ip);
        w = ibi.getWidth();
        h = ibi.getHeight();

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(ground);
        opG.fillRect(0, 0, w, h);

        opG.setColor(fill);

    }

    private void save() throws Exception {
        File op1 = new File(dir + ipFile + "_" + opFile + ".png");
        savePNGFile(obi, op1, dpi);
    }

    private void saveOBJ() throws FileNotFoundException, UnsupportedEncodingException {
        File op = new File(dir + ipFile + "_" + opFile + ".obj");
        writer = new PrintWriter(op, "UTF-8");
        writer.println("# Blender v3.1.2 OBJ File: ''");
        writer.println("# www.blender.org type = ");
        writer.close();
    }


    protected double dp(double p) {
        BigDecimal bd = BigDecimal.valueOf(p);
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private class RandomShape extends Path2D.Double {
        double cx = 0;
        double cy = 0;
        double cz = 0;
        String type;

        private RandomShape(String type) {
            this.type = type;
            //randomisePath(this);
        }

        public RandomShape(double cx, double cy, double cz, String type) {
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.type = type;
        }
    }
}