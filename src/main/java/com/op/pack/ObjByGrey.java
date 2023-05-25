package com.op.pack;

import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class ObjByGrey extends Base {

    private static ObjByGrey legoPack = new ObjByGrey();
    //  R.A. Robertson 2012.03 "Circle Packing 3" ~ www.rariora.org ~
    private String dir = host + "legoPack/";
    private String ipFile = "VirgaColSq3";
    //private String ipFile = "VirgaColSq2";
    private String opFile = "_objPack";
    private int w = 0;
    private int h = 0;
    private double dpi = 300;
    private BufferedImage ibi;
    private BufferedImage obi;
    private Graphics2D opG;
    private int brickUnit = 25;

    Color ground = Color.WHITE;  // Background color.
    Color fill = Color.BLACK;  // Background color.
    private int seed = 1;
    private Random random = new Random(seed);
    private double sdMax = 127;
    private float stroke = 2;
    ArrayList<Brick> brickUsed = new ArrayList<>();

    private PrintWriter writer;
    private Rectangle2D.Double brick;

    public static void main(String[] args) throws Exception {
        legoPack.run();
    }

    private void run() throws Exception {
        setup();
        brick = new Rectangle2D.Double(0, 0, brickUnit, brickUnit);

        drawAll();

        save();

        saveOBJ();
    }

    private void drawAll() {
        for (int y = 0; y < h; y = y + (int) brickUnit) {
            for (int x = 0; x < w; x = x + brickUnit ) {
                int[] xy = draw(x, y);
            }
        }
    }

    private int[] draw(int x, int y) {
        return getBrick(x, y, brick);
    }

    private int[] getBrick(int x, int y, Rectangle2D rect) {
        double off = brickUnit*0.25;
        System.out.println(x + ", " + y);
        Rectangle2D.Double newRect = new Rectangle2D.Double(x, y, rect.getWidth(), rect.getHeight());
        double str = stroke/2;
        Color col = new Color(ibi.getRGB(x, y));
        double grey = (((double)(col.getRed() + col.getGreen() + col.getBlue()))/(3 * 255));
        Brick brick1 = new Brick(newRect, grey);

        opG.setColor(col);
        brickUsed.add(brick1);
        opG.fill(newRect);
        opG.setColor(col.darker().darker());
        opG.setStroke(new BasicStroke(stroke));
        opG.draw(newRect);

        int[] xy = {0, 0};
        return xy;
    }

    void setup() throws IOException {
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
        writer.println("# www.blender.org");
        for (Brick brick : brickUsed) {
            writeObj(brick);
        }
        writer.close();
    }

    private void writeObj(Brick brick) {
        if (brick.z < 0.33) {
            return;
        }

        double fact = 1/((double)w); //0.05 *0.064;
        double zF = brick.z > 0.66 ? -1 : 0;
        double cx = fact * (brick.getX() + (brick.getWidth()) / 2);
        double cy = fact * (brick.getY() + (brick.getHeight()) / 2);
        double cz = zF;

        writer.println("v " + cx + " " + cy + " " + cz);
    }

    private class Brick extends Rectangle2D.Double {
        double z = 0;

        private Brick(Rectangle2D.Double rect, double z) {
            this.x = rect.x;
            this.y = rect.y;
            this.z = z;
            this.width = rect.width;
            this.height = rect.height;
        }
    }
}