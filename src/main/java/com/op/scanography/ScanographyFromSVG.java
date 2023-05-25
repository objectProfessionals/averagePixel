package com.op.scanography;

import com.op.Base;
import com.op.PathLength;
import com.op.epicycles.ComplexNumber;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ScanographyFromSVG extends Base {

    private String dir = host + "scanographyFromSVG/";
    private String ipFile = dir + "StormTrooper3.svg";
    private String opFile = dir + "StormTrooper_LINE.png";
    private String linePNG = "../../../scanography/op/ESB/ESB_LINE";
    private BufferedImage ibi;
    private BufferedImage obi;
    private Graphics2D opG;
    private double wLine = -1;
    private double hline = -1;
    private int w = 2000;
    private int h = w;


    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        new ScanographyFromSVG().start();

    }

    public void start() throws Exception {
        initPng();
        draw();
        savePNGFile(obi, new File(opFile), 300);
    }

    public void draw() throws IOException {
        ArrayList<ComplexNumber> f = new ArrayList<ComplexNumber>();

        Shape shape = parsePathShape(ipFile);
        PathLength pathLength = new PathLength(shape);
        float len = pathLength.lengthOfPath();
        float pd = (float) (len/(float) wLine);

        double width = shape.getBounds2D().getWidth();
        double height = shape.getBounds2D().getHeight();

        int count = 0;

        double translateSc = 40;
        double scaleLine = 0.1;
        float lastAng = 0;
        boolean doAngles = false;
        float angDif = 0;
        for (float p = 0f; p < len; p = p + pd) {
            float ang = pathLength.angleAtLength(p);
            Point2D point2D = pathLength.pointAtLength(p);
            double xx = point2D.getX();
            double yy = point2D.getY();

            int x = (int)(count);
            if (x> wLine -1) {
                return;
            }

            BufferedImage sub = ibi.getSubimage(x, 0, 1, (int) hline);
            if (count > 0) {
                angDif = ang - lastAng;
                if (Math.abs(angDif) > 0.1) {
                    doAngles = true;
                }
            }
            float angles = 50;
            if (doAngles) {
                for (float ii=0; ii< angles; ii++) {
                    float angii = ang - (ii* angDif / angles);
                    AffineTransform trans1 = AffineTransform.getTranslateInstance(0, -hline /2);
                    AffineTransform sc = AffineTransform.getScaleInstance(1, scaleLine);
                    AffineTransform rot = AffineTransform.getRotateInstance(angii);
                    AffineTransform trans = AffineTransform.getTranslateInstance(translateSc *xx, translateSc*yy);
                    trans.concatenate(rot);
                    trans.concatenate(sc);
                    trans.concatenate(trans1);
                    opG.drawImage(sub, trans, null);
                }
            } else {
                AffineTransform trans1 = AffineTransform.getTranslateInstance(0, -hline /2);
                AffineTransform sc = AffineTransform.getScaleInstance(1, scaleLine);
                AffineTransform rot = AffineTransform.getRotateInstance(ang);
                AffineTransform trans = AffineTransform.getTranslateInstance(translateSc *xx, translateSc*yy);
                trans.concatenate(rot);
                trans.concatenate(sc);
                trans.concatenate(trans1);
                opG.drawImage(sub, trans, null);
            }

            lastAng = ang;
            doAngles = false;
            count++;
        }
    }

    private void initPng() throws IOException {
        File ip = new File(host + linePNG + ".png");
        ibi = ImageIO.read(ip);
        wLine = ibi.getWidth();
        hline = ibi.getHeight();

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);


    }

    private String initSVG(String file) throws SAXException, IOException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        String xpathExpression = "//path/@d";
        //String xpathExpression = "path[contains(@id, 'Selection ')]//path/@d";
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);

        NodeList svgPaths = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        String viewBox = document.getElementsByTagName("svg").item(0).getAttributes().getNamedItem("viewBox")
                .getNodeValue().substring(4);
        System.out.println(viewBox);
//        w = Integer.parseInt(viewBox.substring(0, viewBox.indexOf(" ")));
//        h = Integer.parseInt(viewBox.substring(viewBox.indexOf(" ") + 1));

        return svgPaths.item(0).getNodeValue();
    }

    public Shape parsePathShape(String file) {
        try {
            String svgPathShape = initSVG(file);
            AWTPathProducer pathProducer = new AWTPathProducer();
            PathParser pathParser = new PathParser();
            pathParser.setPathHandler(pathProducer);
            pathParser.parse(svgPathShape);
            return pathProducer.getShape();
        } catch (ParseException | SAXException | IOException | XPathExpressionException | ParserConfigurationException ex) {
            // Fallback to default square shape if shape is incorrect
            System.out.println(ex.getMessage());
            return new Rectangle2D.Float(0, 0, 1, 1);
        }
    }
}