package com.op.pack;

import com.op.Base;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

public class CityPack extends Base {

    private static CityPack legoPack = new CityPack();
    private String dir = host + "cityPack/";
    private String opFile = "CityPack";
    private int w = 4000;
    private int h = w;
    private double dpi = 300;
    private BufferedImage obi;
    private Graphics2D opG;
    private int buildingUnit = 50;

    ArrayList<Building> buildingSizes = new ArrayList<>();
    ArrayList<Building> buildingUsed = new ArrayList<>();
    Color ground = new Color(5,5,5);  // Background color.
    private int seed = 1;
    private Random random = new Random(seed);
    private double sdMax = 127;
    private float stroke = buildingUnit / 25;

    private static final String TYPE_1x1 = "1x1";
    private static final String TYPE_2x1 = "2x1";
    private static final String TYPE_2x2 = "2x2";
    private static final String TYPE_3x2 = "3x2";
    private static final String TYPE_3x3 = "3x3";
    private static final String TYPE_4x1 = "4x1";
    private static final String TYPE_4x2 = "4x2";
    private static final String TYPE_4x3 = "4x3";
    private static final String TYPE_4x4 = "4x4";

    public static void main(String[] args) throws Exception {
        legoPack.run();
    }

    private void run() throws Exception {
        setup();
        drawRoads();

        initBrickSizes();

        for (int i = 0; i < 2; i++) {
            drawAll(i);
        }

        save();
    }

    private void drawRoads() {
        Stroke before = opG.getStroke();
        opG.setColor(new Color(0, 0, 0));
        int strMax = 10;
        int rnd = w / 4;
        int x1 = -w / 10;
        for (int i = 0; i < 5; i++) {
            float str = strMax/2 + random.nextInt((int)stroke * strMax);
            opG.setStroke(new BasicStroke(str));
            x1 = x1 + random.nextInt(rnd);
            int x2 = x1 + random.nextInt(rnd);
            opG.drawLine(x1, -h / 10, x2, h * 11 / 10);
            x1 = x2;
        }

        int y1 = -h / 10;
        for (int i = 0; i < 5; i++) {
            float str = strMax/2 + random.nextInt((int)stroke * strMax);
            opG.setStroke(new BasicStroke(str));
            y1 = y1 + random.nextInt(rnd);
            int y2 = y1 + random.nextInt(rnd);
            opG.drawLine(-w / 10, y1, w * 11 / 10, y2);
            y1 = y2;
        }

        opG.setStroke(before);
    }

    private void initBrickSizes() {
        Building r1x1 = new Building(new Rectangle2D.Double(0, 0, buildingUnit, buildingUnit), 0, TYPE_1x1);
        Building r2x1 = new Building(new Rectangle2D.Double(0, 0, 2 * buildingUnit, buildingUnit), 0, TYPE_2x1);
        Building r2x2 = new Building(new Rectangle2D.Double(0, 0, 2 * buildingUnit, 2 * buildingUnit), 0, TYPE_2x2);
        Building r3x2 = new Building(new Rectangle2D.Double(0, 0, 3 * buildingUnit, 2 * buildingUnit), 0, TYPE_3x2);
        Building r3x3 = new Building(new Rectangle2D.Double(0, 0, 3 * buildingUnit, 3 * buildingUnit), 0, TYPE_3x3);
        Building r4x2 = new Building(new Rectangle2D.Double(0, 0, 4 * buildingUnit, 2 * buildingUnit), 0, TYPE_4x2);
        Building r4x3 = new Building(new Rectangle2D.Double(0, 0, 4 * buildingUnit, 3 * buildingUnit), 0, TYPE_4x3);
        Building r4x1 = new Building(new Rectangle2D.Double(0, 0, 4 * buildingUnit, 1 * buildingUnit), 0, TYPE_4x1);
        buildingSizes.add(r1x1);
        buildingSizes.add(r2x1);
        buildingSizes.add(r2x2);
        buildingSizes.add(r3x2);
        buildingSizes.add(r4x1);
        buildingSizes.add(r4x2);
        buildingSizes.add(r4x3);
        //buildingSizes.add(r3x3);
    }

    private void drawAll(int i) {
        for (int y = 0; y < h; y = y + (int) buildingUnit) {
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
        ArrayList<Building> buildings = randomiseBrickRotations();


        Building rect = null;
        for (int i = buildings.size() - 1; i >= 0; i--) {
            rect = buildings.get(i);
            if (sdForBrick(rect, x, y)) {
                return getBrick(x, y, rect);
            }
        }

        if (c == 1 && onlyGround(x, y, rect)) {
            return getBrick(x, y, rect);
        }

        int[] xy = {x + buildingUnit, y};
        return xy;
    }

    private ArrayList<Building> randomiseBrickRotations() {
        ArrayList<Building> buildings = new ArrayList<>();


        for (Building building : buildingSizes) {
            if (random.nextBoolean() && building.getWidth() != building.getHeight()) {
                String newBrickType = getRotatedBrickType(building);
                Building newRect = new Building(new Rectangle2D.Double(0, 0, building.getHeight(), building.getWidth()), 0, newBrickType);
                buildings.add(newRect);
            } else {
                buildings.add(building);
            }
        }

        return buildings;
    }

    private String getRotatedBrickType(Building building) {
        String type = building.type;
        String row = type.substring(0, 1);
        String col = type.substring(2, 3);
        return col + "x" + row;
    }

    private int[] getBrick(int x, int y, Building building) {
        double cornerRad = 0; //buildingUnit / 2;
        double off = buildingUnit * 0.25;
        System.out.println(x + ", " + y);

        Building copiedBuilding = new Building(new Rectangle2D.Double(x, y, building.getWidth(), building.getHeight()), 0, building.type);
        buildingUsed.add(copiedBuilding);
        double str = stroke * 2;

        int maxD = building.getWidth() > building.getHeight() ? (int) (building.getWidth() / 5) :
                (int) (building.getHeight() / 5);

        int height = 10 + random.nextInt(10);
        int bw = (int) building.getWidth();
        int bh = (int) building.getHeight();

        int x1 = (int) (x + str);
        int y1 = (int) (y + str);

        int rndHcount = (maxD / 8);
        rndHcount = 1 + random.nextInt(rndHcount);
        for (int i = 0; i < rndHcount; i++) {
            RoundRectangle2D rRect = new RoundRectangle2D.Double(x1, y1, bw - str * 2, bh - str * 2, cornerRad, cornerRad);
            Color col = new Color(height, height, height);
            opG.setColor(col);
            opG.fill(rRect);
            int rimHeight = height + 5;
            Color col2 = new Color(rimHeight, rimHeight, rimHeight);
            opG.setColor(col2);
            opG.setStroke(new BasicStroke((int) str));
            opG.draw(rRect);

            height = height + random.nextInt(50);

            int rDChange = random.nextInt(maxD);
            bw = bw - rDChange;
            bh = bh - rDChange;
            x1 = x1 + (int) rDChange/2;
            y1 = y1 + (int) rDChange/2;
        }

        int[] xy = {0, 0};
        xy[0] = x + (int) building.getWidth();
        xy[1] = buildingUnit; //y + (int) rect.getHeight();
        return xy;
    }

    private boolean sdForBrick(Rectangle2D rectangle2D, int x, int y) {
        if (!onlyGround(x, y, rectangle2D)) {
            return false;
        }
        int border = (int)(stroke * 2);
        double max = getSDColor(obi, x, y, rectangle2D, border);
        if (max < sdMax) {
            return true;
        }

        return false;
    }

    private boolean onlyGround(int x, int y, Rectangle2D rectangle2D) {
        int border = (int) (stroke * 1);
        for (int yyy = y - border; yyy < y + rectangle2D.getHeight() + border; yyy++) {
            for (int xxx = x - border; xxx < x + rectangle2D.getWidth() + border; xxx++) {
                if (xxx < 0 || yyy < 0 || xxx >= w || yyy >= h) {
                    continue;
                } else if (!new Color(obi.getRGB(xxx, yyy)).equals(ground)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void setup() throws IOException {

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(ground);
        opG.fillRect(0, 0, w, h);
    }

    private void save() throws Exception {
        File op1 = new File(dir + opFile + ".png");
        savePNGFile(obi, op1, dpi);
    }

    protected double dp(double p) {
        BigDecimal bd = BigDecimal.valueOf(p);
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private class Building extends Rectangle2D.Double {
        double z = 0;
        String type;

        private Building(Double rect, double z, String type) {
            this.x = rect.x;
            this.y = rect.y;
            this.z = z;
            this.width = rect.width;
            this.height = rect.height;
            this.type = type;
        }
    }
}