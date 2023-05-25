package com.op.kickstarter;


import com.op.Base;
import com.op.HeartShape;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AddText extends Base {

    private static AddText averagePaint = new AddText();

    private String ipFile = "nebulaeSupernova-green";
    private int w = 0;
    private int h = 0;
    private String opFile = ipFile + "_Signed";

    private BufferedImage obi;
    private Graphics2D opG;
    private String textPre = "NGC-KS-";
    private String textPost = "-Nebula";
    private String textUrl = "www.lonGenArt.com";

    private String fontFile = "ARIAL.TTF";
    private String dirFont = "../host/fonts/" + fontFile;
    private Font font;
    private float fontSize = 70;

    private int seed = 0;
    private Random random = new Random(seed);

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, FontFormatException {
        averagePaint.draw();

    }

    public void draw() throws IOException, FontFormatException {
        host = "../host/prints to sell/nebula/";
        File ip = new File(host + ipFile + ".jpg");
        obi = ImageIO.read(ip);
        w = obi.getWidth();
        h = obi.getHeight();

        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);

        addText();
        save();
    }

    private void addText() throws IOException, FontFormatException {
        opG.setColor(new Color(1f,1f,1f,0.5f));
        font = Font.createFont(Font.TRUETYPE_FONT, new File(dirFont));
        font = font.deriveFont(Font.BOLD, fontSize);
        opG.setFont(font);

        String dig = String.format("%04d" , ((int)(random.nextDouble()*1000)));
        FontRenderContext frc = opG.getFontRenderContext();
        String name = textPre + dig + textPost;
        String url = textUrl;
        GlyphVector gv1 = font.createGlyphVector(frc, name);
        Shape glyph1 = gv1.getOutline();
        Rectangle2D rect1 = glyph1.getBounds2D();
        GlyphVector gv2 = font.createGlyphVector(frc, url);
        Shape glyph2 = gv2.getOutline();
        Rectangle2D rect2 = glyph2.getBounds2D();

        AffineTransform tr1 = AffineTransform.getTranslateInstance(w - rect1.getWidth() * 1.1, h - (rect1.getHeight() + rect2.getHeight()) *1.1);
        Shape transformedGlyph1 = tr1.createTransformedShape(glyph1);
        opG.fill(transformedGlyph1);

        AffineTransform tr2 = AffineTransform.getTranslateInstance(w - rect2.getWidth() * 1.1, h - (rect1.getHeight()) *1.1);
        Shape transformedGlyph2 = tr2.createTransformedShape(glyph2);
        opG.fill(transformedGlyph2);
    }

    private void save() throws IOException {
        File op1 = new File(host + opFile + ".jpg");
        ImageIO.write(obi, "jpg", op1);
        System.out.println("Saved " + op1.getPath());
    }
}
