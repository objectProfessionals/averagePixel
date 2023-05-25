package com.op.pack;

import com.jhlabs.image.EmbossFilter;
import com.op.Base;
import com.op.HeartShape;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class LegoPack extends Base {

    private static LegoPack legoPack = new LegoPack();
    //  R.A. Robertson 2012.03 "Circle Packing 3" ~ www.rariora.org ~
    private String dir = host + "legoPack/";
    private String ipFile = "VirgaColSq4";
    //private String ipFile = "VirgaColSq2";
    private String lipFile = "lego1-bump";
    private String opFile = "_LegoPack";
    private int w = 0;
    private int h = 0;
    private double dpi = 300;
    private BufferedImage ibi;
    private BufferedImage obi;
    private BufferedImage lbi;
    private Graphics2D opG;
    private int brickUnit = 10;

    ArrayList<Brick> brickSizes = new ArrayList<>();
    ArrayList<Brick> brickUsed = new ArrayList<>();
    Color ground = Color.WHITE;  // Background color.
    Color fill = Color.BLACK;  // Background color.
    private int seed = 1;
    private Random random = new Random(seed);
    private double sdMax = 127;
    private float stroke = 2;

    private PrintWriter writer;
    private static final String TYPE_1x1 = "1x1";
    private static final String TYPE_2x1 = "2x1";
    private static final String TYPE_1x2 = "1x2";
    private static final String TYPE_2x2 = "2x2";
    private static final String TYPE_3x2 = "3x2";
    private static final String TYPE_2x3 = "2x3";
    private static final String TYPE_4x1 = "4x1";
    private static final String TYPE_1x4 = "4x1";
    private static final String TYPE_4x2 = "4x2";
    private static final String TYPE_2x4 = "2x4";
    private static final String[] ALL_USED_TYPES = {TYPE_1x1, TYPE_2x1, TYPE_2x2, TYPE_3x2, TYPE_4x1, TYPE_4x2};
    //private static final String[] ALL_TYPES = {TYPE_1x1, TYPE_2x1, TYPE_1x2, TYPE_2x2, TYPE_3x2, TYPE_2x3, TYPE_4x1, TYPE_1x4, TYPE_4x2, TYPE_2x4};
    private static final String[] ALL_TYPES = {TYPE_1x1, TYPE_1x2, TYPE_2x1, TYPE_2x2, TYPE_2x3, TYPE_2x4, TYPE_3x2, TYPE_4x2};

    public static void main(String[] args) throws Exception {
        legoPack.run();
    }

    private void run() throws Exception {
        setup();
        initBrickSizes();

        //testShape();
        for (int i = 0; i < 2; i++) {
            drawAll(i);
        }

        //addShadows();

        save();

        saveOBJ();
    }

    private void initBrickSizes() {
        Brick r1x1 = new Brick(new Rectangle2D.Double(0, 0, brickUnit, brickUnit), 0, TYPE_1x1);
        Brick r2x1 = new Brick(new Rectangle2D.Double(0, 0, 2 * brickUnit, brickUnit), 0, TYPE_2x1);
        Brick r2x2 = new Brick(new Rectangle2D.Double(0, 0, 2 * brickUnit, 2 * brickUnit), 0, TYPE_2x2);
        Brick r3x2 = new Brick(new Rectangle2D.Double(0, 0, 3 * brickUnit, 2 * brickUnit), 0, TYPE_3x2);
        Brick r4x2 = new Brick(new Rectangle2D.Double(0, 0, 4 * brickUnit, 2 * brickUnit), 0, TYPE_4x2);
        //Brick r4x1 = new Brick(new Rectangle2D.Double(0, 0, 4 * brickUnit, 1 * brickUnit), 0, TYPE_4x1);
        brickSizes.add(r1x1);
        brickSizes.add(r2x1);
        brickSizes.add(r2x2);
        brickSizes.add(r3x2);
        //brickSizes.add(r4x1);
        brickSizes.add(r4x2);
    }

    private void testShape() {
        HeartShape.test(200, 0, 200, 200, opG);
    }

    private void drawAll(int i) {
        for (int y = 0; y < h; y = y + (int) brickUnit) {
            for (int x = 0; x < w; ) {
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
        ArrayList<Brick> bricks = randomiseBrickRotations();


        Brick rect = null;
        for (int i = bricks.size() - 1; i >= 0; i--) {
            rect = bricks.get(i);
            if (sdForBrick(rect, x, y)) {
                return getBrick(x, y, rect);
            }
        }

        if (c == 1 && onlyGround(x, y, rect)) {
            return getBrick(x, y, rect);
        }

        int[] xy = {x + brickUnit, y};
        return xy;
    }

    private ArrayList<Brick> randomiseBrickRotations() {
        ArrayList<Brick> bricks = new ArrayList<>();


        for (Brick brick : brickSizes) {
            if (random.nextBoolean() && brick.getWidth() != brick.getHeight()) {
                String newBrickType = getRotatedBrickType(brick);
                Brick newRect = new Brick(new Rectangle2D.Double(0, 0, brick.getHeight(), brick.getWidth()), 0, newBrickType);
                bricks.add(newRect);
            } else {
                bricks.add(brick);
            }
        }

        return bricks;
    }

    private String getRotatedBrickType(Brick brick) {
        String type = brick.type;
        String row = type.substring(0, 1);
        String col = type.substring(2, 3);
        return col +"x"+row;
    }

    private int[] getBrick(int x, int y, Brick brick) {
        double cornerRad = brickUnit/5;
        double dotRad = brickUnit*0.5;
        double off = brickUnit*0.25;
        System.out.println(x + ", " + y);
        Brick copiedBrick = new Brick(new Rectangle2D.Double(x, y, brick.getWidth(), brick.getHeight()), 0, brick.type);
        double str = stroke/2;
        RoundRectangle2D rRect = new RoundRectangle2D.Double(x+str, y+str, brick.getWidth()-str*2, brick.getHeight()-str*2, cornerRad, cornerRad);
        brickUsed.add(copiedBrick);
        Color col = new Color(ibi.getRGB(x, y));
        opG.setColor(col);
        opG.fill(rRect);
        opG.setColor(col.darker().darker());
        opG.setStroke(new BasicStroke(stroke));
        opG.draw(rRect);

        double c = 1;
        double r = 1;
        for (double yy = y; yy<y+copiedBrick.getHeight();) {
            for (double xx = x; xx < x+copiedBrick.getWidth(); ) {
                //opG.draw(new Ellipse2D.Double(xx + off * c, yy + off * r, dotRad, dotRad));
                c++;
                xx = xx + off*c;
            }
            c = 1;
            r = r + 2;
            yy = yy + off*r;
        }

        int[] xy = {0, 0};
        xy[0] = x + (int) brick.getWidth();
        xy[1] = brickUnit; //y + (int) rect.getHeight();
        return xy;
    }

    private boolean sdForBrick(Rectangle2D rectangle2D, int x, int y) {
        if (!onlyGround(x, y, rectangle2D)) {
            return false;
        }
        double max = getSDColor(ibi, x, y, rectangle2D);
        if (max < sdMax) {
            return true;
        }

        return false;
    }

    private boolean onlyGround(int x, int y, Rectangle2D rectangle2D) {
        int str = (int) (stroke);
        for (int yyy = y + str; yyy < y + rectangle2D.getHeight() - str; yyy++) {
            for (int xxx = x + str; xxx < x + rectangle2D.getWidth() - str; xxx++) {
                if (xxx >= w || yyy >= h) {
                    continue;
                } else if (!new Color(obi.getRGB(xxx, yyy)).equals(ground)) {
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

        ip = new File(dir + lipFile + ".jpg");
        lbi = ImageIO.read(ip);
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
        saveBricks();
        writer.close();
    }

    private void saveBricks() throws FileNotFoundException, UnsupportedEncodingException {
        for (Brick brick : brickUsed) {
            writeObjForType(brick);
        }
    }

    private void writeObjForType(Brick brick) {
        int bu = brickUnit;
        double fact = 0.0032; //0.05 *0.064;
        double zF = 1;
        double cx = dp(fact * (brick.getX() + (brick.getWidth() / 2)));
        double cy = dp(fact * (brick.getY() + (brick.getHeight() / 2)));

        double ind = Arrays.asList(ALL_TYPES).indexOf(brick.type);
        if (ind < 0) {
            return;
        }
        double zz = dp(zF * ind);
        writer.println("v " + cx + " " + zz + " " + cy);
        System.out.println(brick.type + ":v " + dp(cx) + " " + dp(cy) + " " + dp(zz));

//        if (TYPE_1x1.equals(type) && brick.getWidth() == bu && brick.getWidth() == bu) {
//            writer.println("v " + cx + " " + cy + " " + (0 * zF));
//        } else if (TYPE_2x1.equals(type) && brick.getWidth() == 2 * bu && brick.getHeight() == 1 * bu) {
//            writer.println("v " + cx + " " + cy + " " + (1 * zF));
//        } else if (TYPE_1x2.equals(type) && brick.getWidth() == 1 * bu && brick.getHeight() == 2 * bu) {
//            writer.println("v " + cx + " " + cy + " " + (2 * zF));
//        } else if (TYPE_2x2.equals(type) && brick.getWidth() == 2 * bu && brick.getHeight() == 2 * bu) {
//            writer.println("v " + cx + " " + cy + " " + (3 * zF));
//        } else if (TYPE_3x2.equals(type) && brick.getWidth() == 3 * bu && brick.getHeight() == 2 * bu) {
//            writer.println("v " + cx + " " + cy + " " + (4 * zF));
//        } else if (TYPE_2x3.equals(type) && brick.getWidth() == 2 * bu && brick.getHeight() == 3 * bu) {
//            writer.println("v " + cx + " " + cy + " " + (5 * zF));
//        } else if (TYPE_4x1.equals(type) && brick.getWidth() == 4 * bu && brick.getHeight() == 1 * bu) {
//            writer.println("v " + cx + " " + cy + " " + (6 * zF));
//        } else if (TYPE_1x4.equals(type) && brick.getWidth() == 1 * bu && brick.getHeight() == 4 * bu) {
//            writer.println("v " + cx + " " + cy + " " + (7 * zF));
//        } else if (TYPE_4x2.equals(type) && brick.getWidth() == 4 * bu && brick.getHeight() == 2 * bu) {
//            writer.println("v " + cx + " " + cy + " " + (8 * zF));
//        } else if (TYPE_2x4.equals(type) && brick.getWidth() == 2 * bu && brick.getHeight() == 4 * bu) {
//            writer.println("v " + cx + " " + cy + " " + (9 * zF));
//        }
    }

    protected double dp(double p) {
        BigDecimal bd = BigDecimal.valueOf(p);
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    private void addShadows() throws NoninvertibleTransformException {
        Graphics2D g = (Graphics2D) ibi.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        EmbossFilter filter = new EmbossFilter();
        filter.setAzimuth((float)Math.toRadians(45));
        filter.setBumpHeight(10f);
        filter.setElevation((float)Math.toRadians(30));
        filter.filter(lbi, obi);

    }
    private class Brick extends Rectangle2D.Double {
        double z = 0;
        String type;

        private Brick(Rectangle2D.Double rect, double z, String type) {
            this.x = rect.x;
            this.y = rect.y;
            this.z = z;
            this.width = rect.width;
            this.height = rect.height;
            this.type = type;
        }
    }

    private class RandomShape extends Path2D.Double {
        double z = 0;
        String type;

    }
}