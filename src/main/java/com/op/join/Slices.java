package com.op.join;

import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;

public class Slices extends Base {
    private static final Slices grid = new Slices();

    protected String dir = host + "join/";
    protected String hostSources = dir + "slices/";
    protected String firstFile = "0001";
    protected String opFile = "slices";
    protected String ext = "png";
    private int rows = 2;
    private int cols = 2;
    private int num = 20;
    private boolean stackAll = false;

    private BufferedImage obi;
    private Graphics2D opG;
    private BufferedImage sbi;
    private Graphics2D spG;
    private boolean drawCorners = true;
    private int cornerLen = 200;
    private int cornerStr = 5;
    private int imgw = 0;
    private int imgh = 0;
    private double dpi = 600;
    private int opw = (int) (dpi * (210 / 25.4));
    private int oph = (int) (dpi * (297 / 25.4));

    public static void main(String[] args) throws Exception {
        grid.run();
    }

    private void run() throws Exception {
        setup();

        for (int i=1; i<= num; i=i+(cols*rows)) {
            join(i);
            int start = i;
            int end = start + (rows * cols) - 1;
            File op1 = new File(hostSources + opFile + start + "-" + end + "." + ext);
            savePNGFile(obi, op1, dpi);
        }

        if (stackAll) {
            File sp1 = new File(hostSources + opFile + "-STACK." + ext);
            savePNGFile(sbi, sp1, dpi);
        }
    }

    private void join(int n) throws IOException {
        double nn = n;
        int xoff = (int) (((opw - imgw) / 2));
        int yoff = (int) (((oph - imgh) / 2));
        for (int row = 1; row < rows + 1; row++) {
            for (int col = 1; col < cols + 1; col++) {
                double a = (nn + col-1 + ((row-1) * cols))/((double)num);
                double aa = 0.1 + (0.1*a);
                String file = String.format("%1$" + 4 + "s", "" + n).replace(' ', '0');
                //opFile + "-" + col + "_" + row;
                System.out.println("file=" + file);
                File ip = new File(hostSources + file + "." + ext);
                BufferedImage ibi = ImageIO.read(ip);
                int w = ibi.getWidth();
                int h = ibi.getHeight();
                int posw = (w) * (col - 1) + xoff;
                int posh = (h) * (row - 1) + yoff;
                drawImage(ibi, (float)aa, posw, posh);
                drawCorners(posw, posh, w, h, n);
                n++;
            }
        }
    }

    private void drawImage(BufferedImage ibi, float a, int posw, int posh) {
        opG.drawImage(ibi, null, posw, posh);

        if (stackAll) {
            float g = 0.9f;
            spG.setColor(new Color(g, g, g, 0.3f));
            spG.fillRect(0, 0, imgw, imgh);
            spG.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, a));
            spG.drawImage(ibi, null, 0, 0);
        }
    }

    private void drawCorners(int posw, int posh, int w, int h, int num) {
        if (drawCorners) {
            opG.setColor(Color.LIGHT_GRAY);
            opG.setStroke(new BasicStroke(cornerStr));

            int fSize = 6;
            Font currentFont = opG.getFont();
            Font newFont = currentFont.deriveFont(currentFont.getSize() * ((float) fSize));
            opG.setFont(newFont);
            opG.drawString("" + num, posw + cornerStr, posh + cornerStr * fSize * 2);
            opG.setFont(currentFont);

            opG.drawLine(posw, posh + cornerLen, posw, posh);
            opG.drawLine(posw, posh, posw + cornerLen, posh);
            opG.drawLine(posw + w, posh + cornerLen, posw + w, posh);
            opG.drawLine(posw + w, posh, posw + w - cornerLen, posh);

            opG.drawLine(posw, posh + h - cornerLen, posw, posh + h);
            opG.drawLine(posw, posh + h, posw + cornerLen, posh + h);
            opG.drawLine(posw + w, posh + h - cornerLen, posw + w, posh + h);
            opG.drawLine(posw + w, posh + h, posw + w - cornerLen, posh + h);
        }
    }

    private void setup() throws IOException, FontFormatException {
        File ip = new File(hostSources + firstFile + "." + ext);
        BufferedImage ibi = ImageIO.read(ip);
        int ww = ibi.getWidth();
        int hh = ibi.getHeight();
        imgw = (ww) * rows;
        imgh = (hh) * cols;

        obi = new BufferedImage(opw, oph, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, opw, oph);

        if (stackAll) {
            sbi = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_ARGB);
            spG = (Graphics2D) sbi.getGraphics();
            spG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                    RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }

}
