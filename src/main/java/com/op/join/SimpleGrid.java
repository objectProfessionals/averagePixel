package com.op.join;

import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SimpleGrid extends Base {
    private static final SimpleGrid simpleGrid = new SimpleGrid();

    protected String dir = host + "join/";
    protected String hostSources = dir + "emission2/";
    protected String iPrefix = "nebulaEmission3-60x60";
    protected String opFile = "joined";

    private BufferedImage obi;
    private Graphics2D opG;

    private int rows = 2;
    private int cols = 2;

    public static void main(String[] args) throws Exception {
        simpleGrid.run();
    }

    private void run() throws Exception {
        setup();

        join();

        save();
    }

    private void save() throws Exception {
        File op1 = new File(hostSources + iPrefix +"_"+ opFile + ".jpg");
        saveJPGFile(obi, op1, 300);
    }

    private void join() throws IOException {
        for (int col = 1; col <= cols; col++) {
        for (int row = 1; row <= rows; row++) {
                String file = iPrefix + "_" +row +"_"+col;
                System.out.println("file=" + file);
                File ip = new File(hostSources + file + ".jpg");
                BufferedImage ibi = ImageIO.read(ip);
                int w = ibi.getWidth();
                int h = ibi.getHeight();
                int posw = (w) * (col-1);
                int posh = (h) * (row-1);
                opG.drawImage(ibi, null, posw, posh);
            }
        }
    }

    private void setup() throws IOException, FontFormatException {
        File ip = new File(hostSources + iPrefix + "_1_1.jpg");
        BufferedImage ibi = ImageIO.read(ip);
        int ww = ibi.getWidth();
        int hh = ibi.getHeight();
        int w = (ww) * (cols);
        int h = (hh) * (rows);

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);
    }

}
