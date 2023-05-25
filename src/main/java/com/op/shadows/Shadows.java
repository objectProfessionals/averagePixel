package com.op.shadows;

import com.op.Base;
import org.apache.batik.transcoder.TranscoderException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Shadows extends Base {

    private static Shadows circlePack = new Shadows();
    private String ipFile1 = "San1";
    private String ipFile2 = "Vir1";
    private String ext = ".png";
    private String dir = host + "shadows/";
    private String opFile = "Shadows";
    private int w = 0;
    private int h = 0;
    private int ww = 0;
    private int hh = 0;
    private double scale = 10;
    private BufferedImage ibi1;
    private BufferedImage ibi2;
    private BufferedImage obi;
    private Graphics2D opG;
    private FileWriter writer;
    private boolean doSVG = false;

    Color ground = Color.WHITE;  // Background color.
    Color fill = Color.BLACK;  // Background color.
    private float str = 5;

    public static void main(String[] args) throws Exception {
        circlePack.run();
    }

    private void run() throws Exception {
        setup();

        drawAll();

        save();
    }

    private void drawAll() {
        for (int y = 0; y < h; y++) {
            boolean last = false;
            for (int x = 0; x < w; x++) {
                double grey = getGrey(x, y, ibi1);
                if (grey < 0.1) {
                    drawEdge(x, y, true, last);
                    last = true;
                } else {
                    last = false;
                }
                grey = getGrey(x, y, ibi2);
                if (grey < 0.1) {
                    drawEdge(x, y, false, last);
                    last = true;
                } else {
                    last = false;
                }
            }
        }
    }

    private double getGrey(int x, int y, BufferedImage ibi) {
        Color col = new Color(ibi.getRGB(x, y));
        return ((double) (col.getRed() + col.getGreen() + col.getBlue())) / (255 * 3);
    }

    private void drawEdge(int x, int y, boolean ltr, boolean last) {
        int xx = (int) (scale * x);
        int yy = (int) (scale * y);
        int off = (int) (scale * 0.5);
        int x1 = xx + (ltr ? -off : 0);
        int x2 = xx + (ltr ? 0 : off);
        int y1 = yy + (ltr ? -off : 0);
        int y2 = yy + (ltr ? 0 : -off);
        opG.drawLine(x1, y1, x2, y2);

        if (last) {
            writeLine(" L " + x2 + " " + y2);
        } else {
            writeLine("M " + x1 + " " + y1 + " L " + x2 + " " + y2);
        }
    }

    void setup() throws IOException {
        File ip = new File(dir + ipFile1 + ext);
        ibi1 = ImageIO.read(ip);

        File ip2 = new File(dir + ipFile2 + ext);
        ibi2 = ImageIO.read(ip2);

        w = ibi1.getWidth();
        h = ibi1.getHeight();

        ww = ibi1.getWidth() * (int) scale;
        hh = ibi1.getHeight() * (int) scale;

        obi = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(ground);
        opG.fillRect(0, 0, ww, hh);

        opG.setColor(fill);
        opG.setStroke(new BasicStroke(str));

        File op = new File(dir + ipFile1 + "_" + ipFile2 + "_" + opFile + ".svg");
        writer = new FileWriter(op);

        writeLine("<svg width=\"" + ww + "\" height=\"" + hh + "\" xmlns=\"http://www.w3.org/2000/svg\">");
        writeLine("<path d=\"");

    }

    private void writeLine(String str) {
        try {
            writer.write(str + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() throws Exception {
        File op1 = new File(dir + ipFile1 + "_" + ipFile2 + "_" + opFile + ".png");
        savePNGFile(obi, op1, 300);

        endSVG();
    }

    void endSVG() throws IOException, TranscoderException {
        writeLine("\" stroke=\"black\" stroke-width=\"1\" fill=\"none\" />");
        writeLine("  </svg>");

        writer.close();
        System.out.println("Saved " + dir + ipFile1 + "_" + ipFile2 + "_" + opFile + ".svg");

    }

}
