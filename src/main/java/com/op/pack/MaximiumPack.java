package com.op.pack;

import com.op.Base;
import com.op.HeartShape;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MaximiumPack extends Base {

    private static MaximiumPack circlePack = new MaximiumPack();
    //  R.A. Robertson 2012.03 "Circle Packing 3" ~ www.rariora.org ~
    private String dir = host + "max/";
    private String ipFile = "VirgaCol";
    private String opFile = "MaxPack";
    private int w = 0;
    private int h = 0;
    private double dpi = 300;
    private BufferedImage ibi;
    private BufferedImage obi;
    private Graphics2D opG;

    ArrayList<Circle> circleList = new ArrayList<>();
    Color ground = Color.WHITE;  // Background color.
    Color fill = Color.BLACK;  // Background color.
    int maxCircles = 0;
    double minR = 20;
    double maxR = 75;
    double bf = 1;
    double spacer = 0;
    double angInc = 10;
    double strokeF = 0.25;
    double maxCirclesF = 3;// VirgaCol:3=20 75 1 0 10 0.25; 2= 10 30 0
    private FileWriter writer;
    private boolean innerCircleOnly = false;

    public static void main(String[] args) throws Exception {
        circlePack.run();
    }

    private void run() throws Exception {
        setup();
        //setupSVG();

        drawAll();

        save();
        //endSVG();
    }

    private void drawAll() {
        for (int i = 0; i < maxCircles; i++) {
            draw(i);
        }
    }


    void setup() throws IOException {

        File ip = new File(dir + ipFile + ".jpg");
        ibi = ImageIO.read(ip);
        w = ibi.getWidth();
        h = ibi.getHeight();
        maxCircles = (int) (((double) w) * maxCirclesF);

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(ground);
        opG.fillRect(0, 0, w, h);

        opG.setColor(fill);
    }

    void setupSVG() throws IOException {
        File op = new File(dir + ipFile + "_" + opFile + ".svg");
        writer = new FileWriter(op);

        double mm2in = 25.4;
        int ww = (int) (((double) w) * mm2in / dpi);
        int hh = (int) (((double) h) * mm2in / dpi);
        writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        writeLine("<svg");
        writeLine("        xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" id=\"svg2\"");
        writeLine("        viewBox=\"0 0 " + w + " " + h + "\"");
        writeLine("        height=\"" + hh + "mm\"");
        writeLine("        width=\"" + ww + "mm\">");
        writeLine("  <defs id=\"defs4\">");
        writeLine("  <g id=\"layer1\">");
    }

    void endSVG() throws IOException, TranscoderException {
        writeLine("  </g>");
        writeLine("  </svg>");

        writer.close();
        System.out.println("Saved " + dir + ipFile + "_" + opFile + ".svg");

        saveSVGtoPNG();

    }

    private void saveSVGtoPNG() throws IOException, TranscoderException {
        //Step -1: We read the input SVG document into Transcoder Input
        //We use Java NIO for this purpose
        //String svg_URI_input = Paths.get(dir + "watercolorPlain.svg").toUri().toURL().toString();
        String svg_URI_input = Paths.get(dir + ipFile + "_" + opFile + ".svg").toUri().toURL().toString();
        TranscoderInput input_svg_image = new TranscoderInput(svg_URI_input);
        //Step-2: Define OutputStream to PNG Image and attach to TranscoderOutput
        OutputStream png_ostream = new FileOutputStream(dir + ipFile + "_" + opFile + "_screen.png");
        TranscoderOutput output_png_image = new TranscoderOutput(png_ostream);
        // Step-3: Create PNGTranscoder and define hints if required
        PNGTranscoder my_converter = new PNGTranscoder();
        my_converter.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) w);
        my_converter.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) h);
        //JPEGTranscoder my_converter = new JPEGTranscoder();
        // Step-4: Convert and Write output
        System.out.println("Transcodeing svg-png...");
        my_converter.transcode(input_svg_image, output_png_image);
        // Step 5- close / flush Output Stream
        png_ostream.flush();
        png_ostream.close();
        System.out.println("Saved " + svg_URI_input + "_screen.png");

    }

    private void draw(int numTry) {
        double ww = (double) w;
        double hh = (double) h;
        double x = Math.random() * ww;
        double y = Math.random() * hh;
        int xyr[] = getMaxPosRadius(x, y);

        int r =0;
        if (r > 0) {
            double rr = r + spacer;

            Color col = getAverageColor(x, y, r);
            circleList.add(new Circle(x, y, r));
            drawOne(x, y, rr, col);
            //drawOneSVG(x, y, rr, col);

            System.out.println(" numTry:" + numTry + " circles:" + circleList.size() + " tryCount:" + " minRad:" + minR + " maxR:" + maxR);
        }
    }

    private boolean hasOnlyGroundColor(int x, int y) {
        if (x < 0 || y < 0 || x >= w | y >= h) {
            return false;
        }
        int rgb = obi.getRGB((int) x, (int) y);
        Color colorTest = new Color(rgb);
        return (colorTest.equals(ground));
    }

    private int[] getMaxPosRadius(double x, double y) {
        int xyr[] = {(int)x, (int)y, 0};
        int rgb = obi.getRGB((int) x, (int) y);
        Color colorTest = new Color(rgb);
        boolean hasGroundColor = hasOnlyGroundColor((int) x, (int) y);
        if (!hasGroundColor) {
            return xyr;
        }

        int minRad = (int) minR;
        int dnx = minRad;
        int dpx = minRad;
        int dny = minRad;
        int dpy = minRad;

        int dnxi = 1;
        int dpxi = 1;
        int dnyi = 1;
        int dpyi = 1;

        int xx = (int) x;
        int yy = (int) y;
        boolean hasGNx = hasOnlyGroundColor(xx - dnx, yy);
        boolean hasGPx = hasOnlyGroundColor(xx + dpx, yy);
        boolean hasGNy = hasOnlyGroundColor(xx, yy - dny);
        boolean hasGPy = hasOnlyGroundColor(xx, yy + dpy);
        boolean hasAll = hasGroundColor && hasGNx && hasGNy && hasGPx && hasGPy;

        double sd = 0.5;
        while (hasAll) {
            dnx = dnx + dnxi;
            dpx = dpx + dpxi;
            dny = dny + dnyi;
            dpy = dpy + dpyi;

            hasGNx = hasOnlyGroundColor(xx - dnx, yy);
            boolean goodSdNx = (getSDGrey(ibi, xx - dnx, yy-dny, dnx) < sd);
            if (!hasGNx || !goodSdNx) {
                dnxi = 0;
            }

            hasGPx = hasOnlyGroundColor(xx + dpx, yy);
            if (!hasGPx) {
                dpxi = 0;
            }

            hasGNy = hasOnlyGroundColor(xx, yy-dny);
            if (!hasGNy) {
                dnyi = 0;
            }

            hasGPy = hasOnlyGroundColor(xx, yy-dpy);
            if (!hasGPy) {
                dpyi = 0;
            }

            hasAll = hasGroundColor && hasGNx && hasGNy && hasGPx && hasGPy;
        }

        int d = Math.min(dpx - dnx, dpy-dny);
        if (d > minR && d < maxR) {
            return xyr;
        }

        return xyr;
    }

    private void drawOne(double x, double y, double rr, Color col) {
        int rrr = (int) (rr - spacer);

        opG.setColor(col);
        Shape shape = getShape(x, y, rrr);
        opG.fill(shape);

//        opG.setColor(col.darker());
//        opG.setStroke(new BasicStroke((float) (minR * strokeF)));
//        opG.draw(shape);
    }

    private void drawOneSVG(double x, double y, double rr, Color col) {
        int cx = (int) x;
        int cy = (int) y;
        int r = (int) rr;

        double rf = 1.25;
        int r2 = (int) (rr * rf);
        String color = toHexString(col);

        writeLine("  	<circle cx=\"" + cx + "\" cy=\"" + cy + "\" r=\"" + (r2) + "\" style=\"fill:" + color + "; />");
    }

    private String toHexString(Color colour) throws NullPointerException {
        String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }

    private void writeLine(String str) {
        try {
            writer.write(str + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Shape getShape(double x, double y, int rrr) {
        return new Ellipse2D.Double((int) (x - rrr), (int) (y - rrr), rrr * 2, rrr * 2);
    }

    private Shape getShapeHeart(double x, double y, int rrr) {
        int size = 9 * rrr / 10;
        int off = 1 * rrr / 10;
        Shape heart = new HeartShape().getShape(size, off, (int) x, (int) y);
        return heart;
    }

    private Color getAverageColor(double x, double y, double r) {
        Color aveCol = Color.BLACK;
        double i = 0;
        double totR = 0;
        double totG = 0;
        double totB = 0;
        for (double ang = 0; ang < 360; ang = ang + angInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= w || (int) yy >= h || (int) xx < 0 || (int) yy < 0) {
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

/* ======================= Circle Class ======================= */

    class Circle {
        double x, y, r;

        Circle(double x, double y, double r) {
            this.x = x;
            this.y = y;
            this.r = r;
            //ellipse(x, y, r, r);  // Turn off and run with lines only for variety.
        }
    }

    private void save() throws Exception {
        File op1 = new File(dir + ipFile + "_" + opFile + ".png");
        savePNGFile(obi, op1, dpi);
    }


}
