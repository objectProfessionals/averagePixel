package com.op.join;

import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.awt.Color.BLACK;

public class GridPlaces extends Base {
    private static final GridPlaces gridPlaces = new GridPlaces();

    protected String dir = host + "join/";
    protected String hostSources = dir + "emission2/";
    protected String[] places = {"London", "CapeTown", "Christchurch", "Honolulu", "NewYork"};
    protected String[] times = {"1-Jan-2020", "30-Apr-2020", "1-Aug-2020", "31-Dec-2020"};
    protected String opFile = "joined";
    protected int border = 50;

    private BufferedImage obi;
    private Graphics2D opG;
    private String fontFile = "TIMES.TTF";
    private String dirFont = "../host/fonts/" + fontFile;
    private Font font;
    private float fontSize = 25;

    public static void main(String[] args) throws Exception {
        gridPlaces.run();
    }

    private void run() throws Exception {
        setup();

        join();

        save();
    }

    private void save() throws Exception {
        File op1 = new File(dir + opFile + ".png");
        savePNGFile(obi, op1, 300.0);
    }

    private void join() throws IOException {
        for (int place = 0; place < places.length; place++) {
            for (int time = 0; time < times.length; time++) {
                String file = times[time] + "-" + places[place];
                System.out.println("file=" + file);
                File ip = new File(hostSources + file + ".png");
                BufferedImage ibi = ImageIO.read(ip);
                int w = ibi.getWidth();
                int h = ibi.getHeight();
                int posw = (border + w) * place;
                int posh = (border + h) * time;
                opG.drawImage(ibi, null, posw, posh);

                FontRenderContext frc = opG.getFontRenderContext();
                String name = places[place] + "   " + times[time];
                GlyphVector gv = font.createGlyphVector(frc, name);
                Shape glyph = gv.getOutline();
                Rectangle2D rect = glyph.getBounds2D();
                AffineTransform tr = AffineTransform.getTranslateInstance((place) * (w + border), (time + 1) * (h + 4*border/5));
                Shape transformedGlyph = tr.createTransformedShape(glyph);
                opG.setColor(BLACK);
                opG.fill(transformedGlyph);

            }
        }
    }

    private void setup() throws IOException, FontFormatException {
        String file = times[0] + "-" + places[0];
        File ip = new File(hostSources + file + ".png");
        BufferedImage ibi = ImageIO.read(ip);
        int ww = ibi.getWidth();
        int hh = ibi.getHeight();
        int w = (ww + border) * (places.length);
        int h = (hh + border) * (times.length);

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);

        font = Font.createFont(Font.TRUETYPE_FONT, new File(dirFont));
        font = font.deriveFont(Font.BOLD, fontSize);
        opG.setFont(font);

    }

}
