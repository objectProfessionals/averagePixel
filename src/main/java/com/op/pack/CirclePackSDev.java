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
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CirclePackSDev extends Base {

    private static CirclePackSDev circlePack = new CirclePackSDev();
    //  R.A. Robertson 2012.03 "Circle Packing 3" ~ www.rariora.org ~
    private String dir = host + "";
    private String ipFile = "VirgaColRound";
    private String opFile = "CirclePackSDev";
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
    double minR = 50;
    double maxR = 75;
    private int lowestRad = 25;
    double minRDec = 1;
    double maxRDec = 2;
    double bf = 1;
    double spacer = 0;
    double angInc = 10;
    double strokeF = 0.25;
    double maxCirclesF = 3;// VirgaCol:3=20 75 1 0 10 0.25; 2= 10 30 0
    private FileWriter writer;
    private boolean innerCircleOnly = true;
    private double sdMax = 25;
    private double sdMaxInc = 5;
    private int maxTryCount = 1000; //200000;

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
        int i = 2;//0
        int j = 2;//0
        int k = -1;//0
        int l = -1;//0

        //writeOneFilter();
        writeFilter(0, -0.5, -0.5, 2, 2, 20, bf * 0.02, 17, 1);
        writeFilter(1, -0.5, -0.5, 2, 2, 25, bf * 0.018, 27, 2);
        writeFilter(2, -0.5, -0.5, 2, 2, 30, bf * 0.017, 37, 3);
        writeFilter(3, -0.5, -0.5, 2, 2, 35, bf * 0.0165, 47, 4);
        writeFilter(4, -0.5, -0.5, 2, 2, 40, bf * 0.016, 57, 5);
        writeFilter(5, -0.5, -0.5, 2, 2, 45, bf * 0.0155, 67, 6);
        writeFilter(6, -0.5, -0.5, 2, 2, 50, bf * 0.015, 77, 7);
        writeLine("  </defs>");
        writeLine("  <g id=\"layer1\">");
    }

    private void writeOneFilter() {
        writeLine("    <filter id=\"filterWatercolor0\" style=\"color-interpolation-filters:sRGB;\" x=\"-1\" width=\"2\" y=\"-1\" height=\"2\">");
        writeLine("      <feGaussianBlur id=\"feGaussianBlur4140\" result=\"result8\" stdDeviation=\"50\" />");
        writeLine("      <feTurbulence id=\"feTurbulence4142\" baseFrequency=\"0.015\" numOctaves=\"5\" type=\"fractalNoise\" result=\"result7\" seed=\"27\" />");
        writeLine("      <feComposite id=\"feComposite4144\" in=\"result7\" operator=\"over\" result=\"result6\" in2=\"result8\" />");
        writeLine("      <feColorMatrix id=\"feColorMatrix4146\" values=\"1 0 0 0 0 0 1 0 0 0 0 0 1 0 0 0 0 0 6 -4 \" result=\"result9\" />");
        writeLine("      <feDisplacementMap id=\"feDisplacementMap4148\" result=\"result4\" scale=\"45\" yChannelSelector=\"A\" xChannelSelector=\"A\" in2=\"result9\" in=\"result7\" />");
        writeLine("      <feComposite id=\"feComposite4150\" in=\"result8\" operator=\"in\" result=\"result2\" in2=\"result4\" />");
        writeLine("      <feComposite id=\"feComposite4152\" in2=\"result9\" operator=\"in\" in=\"result2\" result=\"fbSourceGraphic\" />");
        writeLine("      <feComposite id=\"feComposite4154\" result=\"result91\" in2=\"fbSourceGraphic\" in=\"fbSourceGraphic\" k1=\"0.5\" k2=\"1\" operator=\"arithmetic\" />");
        writeLine("      <feBlend id=\"feBlend4156\" in2=\"result91\" mode=\"multiply\" in=\"fbSourceGraphic\" />");
        writeLine("    </filter>");

    }

    private void writeFilter(int filterNum, double x, double y, double w, double h, int std, double baseFreq, int seed, int scale) {
        String str = "    <filter id=\"filterWatercolor%s\" style=\"color-interpolation-filters:sRGB;\" x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\">";
        str = str + "      <feGaussianBlur id=\"feGaussianBlur4140\" result=\"result8\" stdDeviation=\"%s\" />";
        str = str + "      <feTurbulence id=\"feTurbulence4142\" baseFrequency=\"%s\" numOctaves=\"5\" type=\"fractalNoise\" result=\"result7\" seed=\"%s\" />";
        str = str + "      <feComposite id=\"feComposite4144\" in=\"result7\" operator=\"over\" result=\"result6\" in2=\"result8\" />";
        str = str + "      <feColorMatrix id=\"feColorMatrix4146\" values=\"1 0 0 0 0 0 1 0 0 0 0 0 1 0 0 0 0 0 6 -4 \" result=\"result9\" />";
        str = str + "      <feDisplacementMap id=\"feDisplacementMap4148\" result=\"result4\" scale=\"%s\" yChannelSelector=\"A\" xChannelSelector=\"A\" in2=\"result9\" in=\"result7\" />";
        str = str + "      <feComposite id=\"feComposite4150\" in=\"result8\" operator=\"in\" result=\"result2\" in2=\"result4\" />";
        str = str + "      <feComposite id=\"feComposite4152\" in2=\"result9\" operator=\"in\" in=\"result2\" result=\"fbSourceGraphic\" />";
        str = str + "      <feComposite id=\"feComposite4154\" result=\"result91\" in2=\"fbSourceGraphic\" in=\"fbSourceGraphic\" k1=\"0.5\" k2=\"1\" operator=\"arithmetic\" />";
        str = str + "      <feBlend id=\"feBlend4156\" in2=\"result91\" mode=\"multiply\" in=\"fbSourceGraphic\" />";
        str = str + "    </filter>";

        String line = String.format(str, filterNum, x, y, w, h, std, baseFreq, seed, scale);
        writeLine(line);
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

    int draw(int numTry) {
        if(sdMax > 255) {
            return 0;
        }

        int tryCount = 0;
        double ww = (double) w;
        double hh = (double) h;
        boolean hasGroundOnlyColor;
        //double r = minR + Math.random() * maxR;
        double r = maxR;
        double x = Math.random() * ww;
        double y = Math.random() * hh;
        double rr = r + spacer;
        hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);

        double border = (double)(w)*0.2;
        if (innerCircleOnly) {
            Ellipse2D inner = new Ellipse2D.Double(border, border, w-2*border, h-2*border);
            Point2D in = new Point2D.Double(x, y);
            if (!inner.contains(in)) {
                //System.out.println("inside x,y="+x+","+y);
                hasGroundOnlyColor = false;
            }
        }
        while (!hasGroundOnlyColor && rr > minR) {
            rr--;
            hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);
            tryCount++;
            //System.out.println("rr"+rr);
            if (!hasGroundOnlyColor && rr <= minR) {
                //r = minR + Math.random() * maxR;
                r = maxR;
                x = Math.random() * ww;
                y = Math.random() * hh;
                rr = r + spacer;
                //System.out.println("tryAgain");
                hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);
                if (innerCircleOnly) {
                    Ellipse2D inner = new Ellipse2D.Double(border, border, w-2*border, h-2*border);
                    Point2D in = new Point2D.Double(x, y);
                    if (!inner.contains(in)) {
                        //System.out.println("inside2 x,y="+x+","+y);
                        hasGroundOnlyColor = false;
                    }
                }

            }
        }

        if (hasGroundOnlyColor) {
            Color col = getAverageColor(x, y, r);
            circleList.add(new Circle(x, y, r));
            drawOne(x, y, rr, col);
            //drawOneSVG(x, y, rr, col);
        }

        if (tryCount > maxTryCount) {
            minR = Math.max(lowestRad, minR - minRDec);
            maxR = Math.max(minR+1, maxR - maxRDec);
            sdMax = sdMax + sdMaxInc;
        }
        System.out.println("radius:" + rr + " numTry:" + numTry + " sdMax:" + sdMax + " tryCount:" + tryCount + " minRad:" + minR + " maxR:"+maxR);

        return tryCount;
    }

    private void drawOne(double x, double y, double rr, Color col) {
        int rrr = (int) (rr - spacer);

        opG.setColor(col);
        Shape shape = getShape(x, y, rrr);
        opG.fill(shape);

        opG.setColor(col.darker());
        opG.setStroke(new BasicStroke((float) (minR * strokeF)));
        //opG.draw(shape);
    }

    private void drawOneSVG(double x, double y, double rr, Color col) {
        int cx = (int) x;
        int cy = (int) y;
        int r = (int) rr;

        double rf = 1.25;
        int r2 = (int) (rr * rf);
        String color = toHexString(col);

        int wfRnd = (int) (Math.random() * 5);
        int wf = (int) (5 * (rr / maxR));
        //wf = 0;
        writeLine("  	<circle cx=\"" + cx + "\" cy=\"" + cy + "\" r=\"" + (r2) + "\" style=\"fill:" + color + ";filter:url(#filterWatercolor" + wf + ")\" />");
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

    private boolean hasOnlyGroundColor(double x, double y, double r) {
        int rgb = obi.getRGB((int) x, (int) y);
        Color colorTest = new Color(rgb);
        boolean hasGroundColor = (colorTest.equals(ground));
        if (!hasGroundColor) {
            return false;
        }

        for (double ang = 0; ang < 360; ang = ang + angInc) {
            for (double rr = 0; rr < r; rr++) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if ((int) xx >= w || (int) yy >= h || (int) xx < 0 || (int) yy < 0) {
                    continue;
                }
                rgb = obi.getRGB((int) xx, (int) yy);
                colorTest = new Color(rgb);
                hasGroundColor = (colorTest.equals(ground));
                if (!hasGroundColor) {
                    return false;
                }
            }
        }

        double sd = getSDColor(ibi, (int)x, (int)y, (int)r);
        if (sd > sdMax) {
            //System.out.println("SD="+sd);
            return false;
        }

        return true;
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
