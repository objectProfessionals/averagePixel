package com.op.epicycles;

import acm.graphics.GOval;
import acm.graphics.GPen;
import acm.program.GraphicsProgram;
import com.op.PathLength;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class FourierDrawing extends GraphicsProgram {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new FourierDrawing().start(args);

    }

    public void init() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
        setBackground(Color.BLACK);

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

    public void run() {
        boolean fromSVG = true;
        ArrayList<ComplexNumber> f = new ArrayList<ComplexNumber>();

        if (fromSVG) {
            Shape shape = parsePathShape("../host/average/images/out/epicycles/drawing3.svg");
            PathLength pathLength = new PathLength(shape);
            float len = pathLength.lengthOfPath();

            double x1 = 0;
            double y1 = 0;
            float pd = 2f;
            double width = shape.getBounds2D().getWidth();
            double height = shape.getBounds2D().getHeight();
            double xxx = shape.getBounds2D().getX();
            double yyy = shape.getBounds2D().getY();

            double d = 5;
            int count = 0;
            for (float p = 0f; p < len; p = p + pd) {
                Point2D point2D = pathLength.pointAtLength(p);
                double xx = point2D.getX() -width;
                double yy = point2D.getY()-height;

                if (count % 1 == 0) {
                    f.add(new ComplexNumber(xx * d, yy * d));
                }

                count++;
            }
        } else {
            infile = new File("../host/average/images/out/epicycles/test.txt");
            try {
                scn = new Scanner(infile);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("bad..");
            }
            int count = 0;

            while (scn.hasNext()) {

                String ln = scn.nextLine();
                String w1, w2;
                w1 = ln.substring(0, ln.indexOf(','));
                w2 = ln.substring(ln.indexOf(',') + 1);
                double rp = Double.parseDouble(w1);
                double ip = Double.parseDouble(w2);
                if (count % 1 == 0)
                    f.add(new ComplexNumber(rp, ip));
                count++;
            }

        }

        ArrayList<ComplexNumber> F = null;

        try {
            F = ComplexNumber.FourierCCoefs(f);


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (int i = 0; i < F.size(); i++) {
            F.set(i, F.get(i).mul(1.0 / F.size()));
        }
        HashMap<ComplexNumber, Integer> cmap = new HashMap<ComplexNumber, Integer>();
        for (int i = 0; i < F.size(); i++) {
            cmap.put(F.get(i), i);
        }
        F = ComplexSort(F, 0, F.size());
        System.out.println("NUMBER OF points,CIRCLES: " + f.size() +","+F.size());
        arrows = new ArrayList<GArrow>();
        circles = new ArrayList<GOval>();
        int s = 1; //F.size();
        int N = F.size();
        ComplexNumber v = new ComplexNumber(getWidth() / 2, getHeight() / 2);
        for (int i = 0; i < N; i = i + s) {
            GArrow arrow = new GArrow(v.getR_part(), v.getI_part(), v.getR_part() + F.get(i).getR_part(), v.getI_part() + F.get(i).getI_part(), 0.5);
            GOval circle = new GOval(v.getR_part() - F.get(i).get_polar_r(), v.getI_part() - F.get(i).get_polar_r(), 2 * F.get(i).get_polar_r(), 2 * F.get(i).get_polar_r());
            arrow.setFilled(true);
            arrow.setFullColor(Color.ORANGE);
            circle.setColor(Color.DARK_GRAY);
            double r = F.get(i).get_polar_r();
            if (r > 0) {
                arrows.add(arrow);
                circles.add(circle);
                v = v.add(F.get(i));
                add(circle);
                add(arrow);
            }
        }
        GPen pen = new GPen();
        pen.setColor(Color.WHITE);
        pen.setSpeed(0.8);
        pen.showPen();
        add(pen, v.getR_part(), v.getI_part());
        pause(1000);
        for (int t = 0; t < N; t++) {
            ComplexNumber vv = new ComplexNumber(getWidth() / 2, getHeight() / 2);
            for (int i = 0; i < N; i++) {
                vv = vv.add(F.get(i).rotate((2 * Math.PI * cmap.get(F.get(i)) * t) / N));
                arrows.get(i).setEndPoint(vv.getR_part(), vv.getI_part());
                if (i < (N - 1)) {
                    double r =  F.get(i + 1).get_polar_r();
                    arrows.get(i + 1).setStartPoint(vv.getR_part(), vv.getI_part());
                    circles.get(i + 1).setLocation(vv.getR_part() - r, vv.getI_part() - r);
                }
            }

            pen.drawLine(vv.getR_part() - v.getR_part(), vv.getI_part() - v.getI_part());
            v = vv;
            pause(100);
        }
        pause(10000);
        for (int i = 0; i < N; i++) {
            remove(arrows.get(i));
            remove(circles.get(i));
        }
        pen.setSpeed(1);
        pen.hidePen();

    }


    public ArrayList<ComplexNumber> ComplexSort(ArrayList<ComplexNumber> f, int start, int end) {
        if ((end - start) <= 1) {
            ArrayList<ComplexNumber> f0 = new ArrayList<ComplexNumber>();
            if ((end - start) == 1) {
                f0.add(f.get(start));
            }
            return f0;
        } else {
            int mid = start + (end - start) / 2;
            ArrayList<ComplexNumber> f1 = ComplexSort(f, start, mid);
            ArrayList<ComplexNumber> f2 = ComplexSort(f, mid, end);
            ArrayList<ComplexNumber> ff = new ArrayList<ComplexNumber>();
            int lp = 0, rp = 0;
            while (lp < f1.size() && rp < f2.size()) {
                if (f1.get(lp).get_polar_r() > f2.get(rp).get_polar_r()) {
                    ff.add(f1.get(lp));
                    lp++;
                } else {
                    ff.add(f2.get(rp));
                    rp++;
                }
            }
            while (lp < f1.size()) {
                ff.add(f1.get(lp));
                lp++;
            }
            while (rp < f2.size()) {
                ff.add(f2.get(rp));
                rp++;
            }
            return ff;
        }

    }

    ArrayList<GArrow> arrows;
    ArrayList<GOval> circles;
    File infile, outfile;
    Scanner scn;
    public static final double PI = 3.14159;
    private static final int APPLICATION_WIDTH = 1920;
    private static final int APPLICATION_HEIGHT = 1080;

}