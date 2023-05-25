package com.op.colors;

import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

public class GooglePhotosColorGrabber extends Base {

    private static final GooglePhotosColorGrabber generate = new GooglePhotosColorGrabber();

    private String dir = host + "color/";
    private String png = ".png";
    private String ipFileName = "sv2";
    private String ipF = dir + ipFileName + png;
    private String opF = dir + ipFileName + "_GRAB" + png;

    private BufferedImage ibi;
    private BufferedImage obi;
    private Graphics2D opG;

    private int ww = -1;
    private int hh = -1;

    private int tot = 550;
    private int box = 200;

    private int w = (int)(Math.sqrt(tot) * box);
    private int h = (int)(Math.sqrt(tot) * box);
    private int rectMinD = 100;
    private int aveColRectD = 2;
    private double threshold = 0.75;

    private Random random = new Random(0);
    private TreeMap<Float, BufferedImage> hsb2Image = new TreeMap<>();
    private TreeMap<Float, Color> hsb2Color = new TreeMap<>();
    private ArrayList<Rectangle> rects = new ArrayList();

    public static void main(String[] args) throws Exception {
        generate.draw();
    }

    private void draw() throws Exception {
        setup();

        findAll();
        drawAll();
        save();
    }

    private void findAll() {

        while (rects.size() < tot) {
            int x = (int) (((double) ww) * random.nextDouble());
            int y = (int) (((double) hh) * random.nextDouble());

            boolean already = false;
            for (Rectangle rect : rects) {
                if (rect.contains(x, y)) {
                    already = true;
                }
            }
            if (!already) {
                find(x, y);
            }
        }
    }

    private void find(int x, int y) {
        boolean bg = hasBGColor(x, y);
        Rectangle rect = new Rectangle(x, y, 1, 1);
        while (!bg) {
            boolean tl = hasBGColor(rect.x, rect.y);
            boolean tr = hasBGColor(rect.x + rect.width - 1, rect.y);
            boolean bl = hasBGColor(rect.x, rect.y + rect.height - 1);
            boolean br = hasBGColor(rect.x + rect.width - 1, rect.y + rect.height - 1);
            boolean t = hasBGColor(rect.x + ((rect.width -1)/2), rect.y);
            boolean b = hasBGColor(rect.x + ((rect.width -1)/2), rect.y + rect.height -1);
            boolean l = hasBGColor(rect.x, rect.y  + ((rect.height -1)/2));
            boolean r = hasBGColor(rect.x + rect.width - 1, rect.y  + ((rect.height -1)/2));

            if (tl && tr && bl && br && t && b && l && r) {
                bg = true;
            } else {
                if (!tl) {
                    rect.setLocation(rect.x - 1, rect.y - 1);
                    rect.setSize(rect.width + 1, rect.height + 1);
                } else if (!tr) {
                    rect.setLocation(rect.x, rect.y - 1);
                    rect.setSize(rect.width, rect.height + 1);
                } else if (!bl) {
                    rect.setLocation(rect.x - 1, rect.y);
                    rect.setSize(rect.width + 1, rect.height + 1);
                } else if (!br) {
                    rect.setLocation(rect.x, rect.y);
                    rect.setSize(rect.width + 1, rect.height + 1);
                } else if (!l) {
                    rect.setLocation(rect.x - 1, rect.y);
                    rect.setSize(rect.width + 1, rect.height);
                } else if (!r) {
                    rect.setLocation(rect.x, rect.y);
                    rect.setSize(rect.width + 1, rect.height);
                } else if (!t) {
                    rect.setLocation(rect.x, rect.y -1);
                    rect.setSize(rect.width, rect.height + 1);
                } else if (!b) {
                    rect.setLocation(rect.x, rect.y);
                    rect.setSize(rect.width, rect.height + 1);
                }
                bg = false;
            }
        }


        if (rect.width < rectMinD || rect.height < rectMinD) {
            return;
        }
        if (rect.x < 0 || rect.x + rect.width > ww
                || rect.y < 0 || rect.y + rect.height > hh ) {
            return;
        }
        BufferedImage sub = ibi.getSubimage(rect.x, rect.y, rect.width, rect.height);

        double cx = rect.x + (rect.width/2);
        double cy = rect.y + (rect.height/2);
        double r = (rect.width + rect.height)/4;

        //Color ave = getAverageColor(cx, cy, r, rect, sub);
        Color ave = getAverageColor(rect);
        float[] hsb = {0, 0, 0};
        Color.RGBtoHSB(ave.getRed(), ave.getGreen(), ave.getBlue(), hsb);
        float val = hsb[0]*100 + hsb[1]*10 + hsb[2]*1;
        double off = rects.size();
        if (hsb2Image.get(val) != null) {
            val = val + ((float)off)* 0.00001F;
        }
        hsb2Image.put(val, sub);
        hsb2Color.put(val, ave);
        rects.add(rect);
        System.out.println("add rect=" +rects.size());
    }

    private boolean hasBGColor(int x, int y) {
        if (x < 0 || x >= ww || y < 0 || y >= hh) {
            return true;
        }
        int rgb = ibi.getRGB(x, y);
        Color c = new Color(rgb);
        double R = (double) (c.getRed()) / 255.0;
        double G = (double) (c.getGreen()) / 255.0;
        double B = (double) (c.getBlue()) / 255.0;
        return R > threshold && G > threshold && B > threshold;
    }

    private void drawAll() {
        int xx = 0;
        int yy = 0;
        int str = 5;
        int c = 0;
        for (Float key : hsb2Image.keySet()) {
            System.out.println("hsb:" + key);
            BufferedImage sub = hsb2Image.get(key);
            int minL = Math.min(sub.getWidth(), sub.getHeight());
            int newX = (sub.getWidth() - minL)/2;
            int newY = (sub.getHeight() - minL)/2;
            BufferedImage subSq = sub.getSubimage(newX, newY, minL, minL);
            opG.drawImage(subSq, xx, yy, box, box, null);
            opG.setColor(hsb2Color.get(key));
            //opG.setColor(Color.WHITE);
            opG.setStroke(new BasicStroke(str));
            opG.drawRect(xx + str / 2, yy + str / 2, box - (str), box - (str));

            xx = xx + box;
            if (xx >= w) {
                xx = 0;
                yy = yy + box;
            }
            c++;
        }

        System.out.println("count=" + c);
    }

    private Color getAverageColor(Rectangle rectangle) {
        double i = 0;
        double totR = 0;
        double totG = 0;
        double totB = 0;
        for (int y= rectangle.y; y<rectangle.y + rectangle.height; y= y + aveColRectD){
            for (int x= rectangle.x; x<rectangle.x + rectangle.width; x= x + aveColRectD){
                if (!rectangle.contains(x, y)) {
                    continue;
                }
                if ((int) x >= ww || (int) y >= hh || (int) x < 0 || (int) y < 0) {
                    continue;
                }
                int rgb = ibi.getRGB(x, y);
                Color c = new Color(rgb);
                totR = totR + (double) (c.getRed()) / 255.0;
                totG = totG + (double) (c.getGreen()) / 255.0;
                totB = totB + (double) (c.getBlue()) / 255.0;
                i++;
            }

        }
        Color aveCol = new Color(getRGB(totR, i), getRGB(totG, i), getRGB(totB, i));
        return aveCol;
    }

    private Color getAverageColor(double x, double y, double r, Shape shape, BufferedImage image) {
        Color aveCol = Color.BLACK;
        double angInc = 1;
        double i = 0;
        double totR = 0;
        double totG = 0;
        double totB = 0;
        double radStart = r;
        double radEnd = r * 0.9;
        double radInc = 1;
        for (double ang = 0; ang < 360; ang = ang + angInc) {
//            for (double rr = radStart; rr < r; rr= rr + radInc) {
            for (double rr = radStart; rr > radEnd; rr = rr - radInc) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if (!shape.contains(xx, yy)) {
                    continue;
                }
                if ((int) xx >= ww || (int) yy >= hh || (int) xx < 0 || (int) yy < 0) {
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


        aveCol = new Color(getRGB(totR, i), getRGB(totG, i), getRGB(totB, i));
        return aveCol;
    }

    private int getRGB(double totR, double i) {
        return (int) ((totR / i) * 255.0);
    }

    void setup() throws IOException {

        File ip = new File(ipF);
        ibi = ImageIO.read(ip);

        ww = ibi.getWidth();
        hh = ibi.getHeight();

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);
    }

    private void save() throws Exception {
        File op1 = new File(opF);
        savePNGFile(obi, op1, 300);
    }
}
