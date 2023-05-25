package com.op.fonts;

import com.op.Base;
import com.op.PathLength;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.awt.Color.*;

public class FontTest extends Base {
    private static final FontTest gridPlaces = new FontTest();

    protected String dir = host + "fonts/";
    protected String opFile = "fontTest";
    protected int w = 1000;
    protected int h = 1000;

    private BufferedImage obi;
    private Graphics2D opG;
    private String fontFile = "CNC.TTF";
    private String dirFont = "../host/fonts/" + fontFile;
    private Font font;
    private float fontSize = 500;
    private float pathSections = 10;
    private Point2D[][] pointsPerGlyph = null;
    private Shape[] glyphs = new Shape[10];

    public static void main(String[] args) throws Exception {
        gridPlaces.run();
    }

    private void run() throws Exception {
        setup();

        //draw();
        drawAll();

        save();
    }

    private void save() throws Exception {
        File op1 = new File(dir + opFile + ".png");
        savePNGFile(obi, op1, 300.0);
    }

    private void draw() throws IOException {
        FontRenderContext frc = opG.getFontRenderContext();
        String all = "0123456789";
        GlyphVector gv = font.createGlyphVector(frc, all);
        Shape glyph = gv.getOutline();
        Rectangle2D rect = glyph.getBounds2D();
        AffineTransform tr = AffineTransform.getTranslateInstance(w / 2 - (rect.getWidth() / 2), h / 2 - (rect.getHeight() / 2));
        Shape transformedGlyph = tr.createTransformedShape(glyph);
        PathLength pl = new PathLength(transformedGlyph);
        opG.setColor(BLACK);
        opG.draw(transformedGlyph);
    }

    private void drawAll() throws IOException {
        for (int i = 0; i < 10; i++) {
            setupPoints(i);
        }


        for (int number = 0; number < 2; number++) {
            int start = number;
            int end = number == 9 ? 0 : number + 1;
            Point2D[] numberPoints1 = pointsPerGlyph[start];
            Point2D[] numberPoints2 = pointsPerGlyph[end];

            for (int n=0; n<10; n++) {
                double x1 = numberPoints1[n].getX();
                double y1 = numberPoints1[n].getY();
                double x2 = numberPoints2[n].getX();
                double y2 = numberPoints2[n].getY();

                double pc = 0.1;
                double xs = x1 + pc * (x2 - x1);
                double ys = y1 + pc * (y2 - y1);
                double xe = x1 + (1 - pc) * (x2 - x1);
                double ye = y1 + (1 - pc) * (y2 - y1);

                int dEdge = 2;
                opG.setColor(RED);
                opG.fillOval((int)(x1 - dEdge), (int)(y1 - dEdge), dEdge*2, dEdge*2);
                opG.drawString(number + ":" + n, (int) (x1 - dEdge), (int) (y1 - dEdge));
                opG.setColor(GRAY);
                opG.drawLine((int) xs, (int) ys, (int) xe, (int) ye);
                opG.setColor(BLUE);
                opG.draw(glyphs[number]);
            }
        }
    }

    private void setupPoints(int n) throws IOException {

        double xd = w / 11;
        FontRenderContext frc = opG.getFontRenderContext();
        String all = "" + n;
        GlyphVector gv = font.createGlyphVector(frc, all);
        Shape glyph = gv.getOutline();
        Rectangle2D rect = glyph.getBounds2D();
        AffineTransform tr = AffineTransform.getTranslateInstance(w / 2 - (rect.getWidth() / 2), h / 2 + (rect.getHeight() / 2));
        Shape transformedGlyph = tr.createTransformedShape(glyph);
        glyphs[n] = transformedGlyph;
        PathLength pl = new PathLength(transformedGlyph);
        float len = pl.lengthOfPath();
        float dLen = len / pathSections;
        int i = 0;
        for (float f = 0; f < len; f = f + dLen) {
            Point2D p = pl.pointAtLength(f);
            pointsPerGlyph[n][i] = p;
            i++;
        }
    }

    private void setup() throws IOException, FontFormatException {
        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);

        font = Font.createFont(Font.TRUETYPE_FONT, new File(dirFont));
        font = font.deriveFont(Font.BOLD, fontSize);
        //opG.setFont(font);

        pointsPerGlyph = new Point2D[10][(int) pathSections + 1];
    }

}
