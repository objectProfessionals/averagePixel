package com.op.pcb;


import com.op.Base;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class PCBPaint extends Base {

    private static PCBPaint pcbPaint = new PCBPaint();

    private String dir = host + "pcb/";
    private String ipFile = "PCB";
    private int w = 1000;
    private int h = w;
    private String opFile = "PCB";

    private BufferedImage obi;
    private Graphics2D opG;
    private Random random = new Random(1);


    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        pcbPaint.draw();

    }

    private void draw() throws Exception {
        init();

        drawAll();

        save();
    }

    private void drawAll() {
        double pix = 25;
        double ww = ((double) w) / pix;
        double hh = ((double) h) / pix;
        int num = 100;
        ArrayList<Path2D> paths = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Path2D.Double path = new Path2D.Double();
            int x1 = (int) pix * (int) (ww * random.nextDouble());
            int y1 = (int) pix * (int) (hh * random.nextDouble());

            int x2 = (int) pix * (int) (ww * random.nextDouble());
            int y2 = (int) pix * (int) (hh * random.nextDouble());

            path.moveTo(x1, y1);
            path.lineTo(x1, y2);
            path.moveTo(x1, y2);
            path.lineTo(x2, y2);

            Rectangle2D rect = path.getBounds2D();

            boolean intersects = false;
            for (Path2D path2D : paths) {
                if (path2D.intersects(rect)) {
                    i--;
                    intersects = true;
                    break;
                }
            }

            if (!intersects) {
                paths.add(path);
            }
        }

        for (Path2D path2D : paths) {
            opG.draw(path2D);
        }
    }

    public void init() throws IOException {
        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);

        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(5));
    }

    private void save() throws Exception {
        File op1 = new File(dir + opFile + ".png");
        savePNGFile(obi, op1, 300);
    }


}
