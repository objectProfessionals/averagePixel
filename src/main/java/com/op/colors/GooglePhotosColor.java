package com.op.colors;

import com.op.Base;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

public class GooglePhotosColor extends Base {

    private static final GooglePhotosColor generate = new GooglePhotosColor();

    private String dir = host + "color/";
    private String png = ".png";
    private String svg = ".svg";
    private String ipFileName = "sv";
    private String svgF = dir + ipFileName + svg;
    private String ipF = dir + ipFileName + png;
    private String opF = dir + ipFileName +"_OUT" + png;

    private DocumentBuilder builder;
    private NodeList svgPaths;
    private double numSVGs = -1;

    private BufferedImage ibi;
    private BufferedImage obi;
    private Graphics2D opG;

    private int ww = -1;
    private int hh =-1;

    private int w = 1200;
    private int h = w;
    private int box = 100;

    private TreeMap<Float, BufferedImage> hsb2Image = new TreeMap<>();
    private TreeMap<Float, Color> hsb2Color = new TreeMap<>();

    public static void main(String[] args) throws Exception {
        generate.draw();
    }

    private void draw() throws Exception {
        initSVG();
        setup();

        drawAll();
        save();
    }

    private void drawAll() {
        for (int i=0; i<svgPaths.getLength(); i++) {
            Shape shape = parsePathShape(svgPaths.item(i).getNodeValue());
            int x = (int)shape.getBounds2D().getX();
            int y = (int)shape.getBounds2D().getY();
            int l = (int)shape.getBounds2D().getWidth();
            int t = (int)shape.getBounds2D().getHeight();
            BufferedImage sub = ibi.getSubimage(x,y,l,t);

            double cx = x + (l/2);
            double cy = y + (t/2);
            double r = (l + t)/4;

            Color ave = getAverageColor(cx, cy, r, shape, sub);
            float[] hsb = {0, 0, 0};
            Color.RGBtoHSB(ave.getRed(), ave.getGreen(), ave.getBlue(), hsb);
            float val = hsb[0]*100 + hsb[1]*100 + hsb[2]*100;
            if (hsb2Image.get(val) != null) {
                System.out.println("****val=" +val);
                val = val + ((float)i)* 0.00001F;
            }
            hsb2Image.put(val, sub);
            hsb2Color.put(val, ave);
        }

        int xx = 0;
        int yy = 0;
        int str = 5;
        int c = 0;
        for (Float key : hsb2Image.keySet()) {
            System.out.println("hsb:" + key);
            BufferedImage sub = hsb2Image.get(key);
            opG.drawImage(sub, xx, yy, box, box, null);
            opG.setColor(hsb2Color.get(key));
            opG.setStroke(new BasicStroke(str));
            opG.drawRect(xx+str/2, yy+str/2, box-(str), box-(str));

            xx = xx + box;
            if (xx >= w) {
                xx = 0;
                yy = yy + box;
            }
            c++;
        }

        System.out.println("count=" + c);
    }

    private Color getAverageColor(double x, double y, double r, Shape shape, BufferedImage image) {
        Color aveCol = Color.BLACK;
        double angInc = 1;
        double i = 0;
        double totR = 0;
        double totG = 0;
        double totB = 0;
        double radStart = r;
        double radEnd = r*0.8;
        double radInc = 1;
        for (double ang = 0; ang < 360; ang = ang + angInc) {
//            for (double rr = radStart; rr < r; rr= rr + radInc) {
            for (double rr = radStart; rr > radEnd; rr= rr - radInc) {
                double xx = x + rr * Math.cos(Math.toRadians(ang));
                double yy = y + rr * Math.sin(Math.toRadians(ang));
                if (!shape.contains(xx, yy)) {
                    continue;
                }
                if ((int) xx >= ww || (int) yy >= hh || (int) xx < 0 || (int) yy < 0) {
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

    public Shape parsePathShape(String svgPathShape) {
        try {
            AWTPathProducer pathProducer = new AWTPathProducer();
            PathParser pathParser = new PathParser();
            pathParser.setPathHandler(pathProducer);
            pathParser.parse(svgPathShape);
            return pathProducer.getShape();
        } catch (ParseException ex) {
            // Fallback to default square shape if shape is incorrect
            return new Rectangle2D.Float(0, 0, 1, 1);
        }
    }

    void setup() throws IOException {

        File ip = new File(ipF);
        ibi = ImageIO.read(ip);

        ww = ibi.getWidth();
        hh = ibi.getHeight();

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);
    }

    private void initSVG() throws SAXException, IOException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        builder = factory.newDocumentBuilder();
        Document document = builder.parse(svgF);
        String xpathExpression = "//path/@d";
        //String xpathExpression = "path[contains(@id, 'Selection ')]//path/@d";
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);

        svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        String viewBox = document.getElementsByTagName("svg").item(0).getAttributes().getNamedItem("viewBox")
                .getNodeValue().substring(4);
        System.out.println(viewBox);
//        w = Integer.parseInt(viewBox.substring(0, viewBox.indexOf(" ")));
//        h = Integer.parseInt(viewBox.substring(viewBox.indexOf(" ") + 1));

        numSVGs = svgPaths.getLength();
        System.out.println(numSVGs);
    }

    private void save() throws Exception {
        File op1 = new File(opF);
        savePNGFile(obi, op1, 300);
    }


}
