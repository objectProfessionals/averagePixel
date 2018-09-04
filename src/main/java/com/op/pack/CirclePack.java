package main.java.com.op.pack;

import main.java.com.op.Base;
import main.java.com.op.HeartShape;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CirclePack extends Base {

    private static CirclePack circlePack = new CirclePack();
    //  R.A. Robertson 2012.03 "Circle Packing 3" ~ www.rariora.org ~
    private String dir = host + "";
    private String ipFile = "VirgaCol";
    private String opFile = "CirclePack";
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
    double spacer = 0;
    double angInc = 10;
    double strokeF = 0.25;
    double maxCirclesF = 3;// 3=20 75 0 10 0.25; 2= 10 30 0
    private FileWriter writer;

    public static void main(String[] args) throws Exception {
        circlePack.run();
    }

    private void run() throws Exception {
        setup();
        setupSVG();

        drawAll();

        save();
        endSVG();
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

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
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
        int ww = (int)(((double)w)*mm2in/dpi);
        int hh = (int)(((double)h)*mm2in/dpi);
        writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        writeLine("<svg");
        writeLine("        xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:cc=\"http://creativecommons.org/ns#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" id=\"svg2\"");
        writeLine("        viewBox=\"0 0 "+w+" "+h+"\"");
        writeLine("        height=\""+hh+"mm\"");
        writeLine("        width=\""+ww+"mm\">");
        writeLine("  <defs id=\"defs4\">");
        int i = 2;//0
        int j = 2;//0
        int k = -1;//0
        int l = -1;//0
        writeFilter(0, k, l, i, j, 8, 0.013, 27);
        writeFilter(1, k*2, l*2, i*2, j*2, 16, 0.012, 30);
        writeFilter(2, k*3, l*3, i*3, j*3, 24, 0.011, 40);
        writeFilter(3, k*4, l*4, i*4, j*4, 28, 0.01, 50);
        writeFilter(4, k*5, l*5, i*5, j*5, 32, 0.009, 60);
        writeFilter(5, k*6, l*6, i*6, j*6, 36, 0.008, 70);
        writeFilter(6, k*7, l*7, i*7, j*7, 40, 0.007, 70);
        writeLine("  </defs>");
        writeLine("  <g id=\"layer1\">");
    }

    private void writeFilter(int filterNum, double x, double y, double w, double h, int std, double baseFreq, int seed) {
        writeLine("    <filter id=\"filterWatercolor"+filterNum+"\" style=\"color-interpolation-filters:sRGB;\" x=\""+x+"\" width=\""+w+"\" y=\""+y+"\" height=\""+h+"\">");
        writeLine("      <feGaussianBlur id=\"feGaussianBlur4140\" result=\"result8"+filterNum+"\" stdDeviation=\""+std+"\" />");
        writeLine("      <feTurbulence id=\"feTurbulence4142\" baseFrequency=\""+baseFreq+"\" numOctaves=\"5\" type=\"fractalNoise\" result=\"result7"+filterNum+"\" seed=\""+seed+"\" />");
        writeLine("      <feComposite id=\"feComposite4144\" in=\"result7"+filterNum+"\" operator=\"over\" result=\"result6"+filterNum+"\" in2=\"result8"+filterNum+"\" />");
        writeLine("      <feColorMatrix id=\"feColorMatrix4146\" values=\"1 0 0 0 0 0 1 0 0 0 0 0 1 0 0 0 0 0 6 -4 \" result=\"result9"+filterNum+"\" />");
        writeLine("      <feDisplacementMap scale=\""+(filterNum*100+100)+"\" id=\"feDisplacementMap4148\" result=\"result4"+filterNum+"\" yChannelSelector=\"A\" xChannelSelector=\"A\" in2=\"result9"+filterNum+"\" in=\"result7"+filterNum+"\" />");
        writeLine("      <feComposite id=\"feComposite4150\" in=\"result8"+filterNum+"\" operator=\"in\" result=\"result2"+filterNum+"\" in2=\"result4"+filterNum+"\" />");
        writeLine("      <feComposite id=\"feComposite4152\" in2=\"result9"+filterNum+"\" operator=\"in\" in=\"result2\" result=\"fbSourceGraphic\" />");
        writeLine("      <feComposite id=\"feComposite4154\" result=\"result91"+filterNum+"\" in2=\"fbSourceGraphic\" in=\"fbSourceGraphic\" k1=\"0.5\" k2=\"1\" operator=\"arithmetic\" />");
        writeLine("      <feBlend id=\"feBlend4156\" in2=\"result91"+filterNum+"\" mode=\"multiply\" in=\"fbSourceGraphic\" />");
        writeLine("    </filter>");
    }

    void endSVG() throws IOException {
        writeLine("  </g>");
        writeLine("  </svg>");

        writer.close();
    }

    int draw(int numTry) {
        int tryCount = 0;
        double ww = (double) w;
        double hh = (double) h;
        boolean hasGroundOnlyColor;
        double r = minR + Math.random() * maxR;
        double x = Math.random() * ww;
        double y = Math.random() * hh;
        double rr = r + spacer;
        hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);

        while (!hasGroundOnlyColor && rr > minR) {
            rr--;
            hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);
            tryCount++;
            //System.out.println("rr"+rr);
            if (!hasGroundOnlyColor && rr <= minR) {
                r = minR + Math.random() * maxR;
                x = Math.random() * ww;
                y = Math.random() * hh;
                rr = r + spacer;
                //System.out.println("tryAgain");
                hasGroundOnlyColor = hasOnlyGroundColor(x, y, rr);
            }
        }

        if (hasGroundOnlyColor) {
            Color col = getAverageColor(x, y, r);
            circleList.add(new Circle(x, y, r));
            drawOne(x, y, rr, col);
            drawOneSVG(x, y, rr, col);
        }
        if (tryCount > 200000) {
            minR = Math.max(2, minR - 1);
        }
        System.out.println("hasOnlyGroundColor:" + hasGroundOnlyColor + " numTry:" + numTry + " circles:" + circleList.size() + " tryCount:" + tryCount + " minRad:" + minR);


        return tryCount;
    }

    private void drawOne(double x, double y, double rr, Color col) {
        int rrr = (int) (rr - spacer);

        opG.setColor(col);
        Shape shape = getShape(x, y, rrr);
        opG.fill(shape);

        opG.setColor(col.darker());
        opG.setStroke(new BasicStroke((float) (minR * strokeF)));
        opG.draw(shape);
    }

    private void drawOneSVG(double x, double y, double rr, Color col) {
        int cx = (int) x;
        int cy = (int) y;
        int r = (int) rr;
        String color = toHexString(col);

        int wfRnd  = (int)(Math.random()*5);
        int wf = (int)(5 * (rr /maxR));
        writeLine("  	<circle cx=\""+cx+"\" cy=\""+cy+"\" r=\""+r+"\" style=\"fill:"+color+";stroke:"+color+";stroke-width:1;filter:url(#filterWatercolor"+wf+")\" />");
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
