package com.op.axidraw;

import com.op.Base;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.transcoder.TranscoderException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class AxidrawBaseOLD extends Base {
    protected PrintWriter writer;
    protected SVGGraphics2D svgGenerator = null;
    protected boolean generateSVG = false; // true if using svgGenerator, not writer

    protected int w = 0;
    protected int h = 0;
    protected int ww = 0;
    protected int hh = 0;

    protected double scaleOrig = 1;
    protected double scaleSVG = -1;

    protected BufferedImage ibi;
    protected BufferedImage obi;
    protected Graphics2D opG;
    private int border = -1;
    protected double bordermm = -1;
    protected double borderx = -1;
    protected double bordery = -1;
    protected double borderF = 0.1; //0.025;
    protected float stroke = 1f;
    protected ColorSpace instance = null;
    protected String pathToCMYKProfile = host + "sine/UncoatedFOGRA29.icc";

    protected void startSVG(String fileName, int wmm, int hmm, int ww, int hh) throws FileNotFoundException, UnsupportedEncodingException {
        File op = new File(fileName);
        if (generateSVG) {
            // Get a DOMImplementation.
            DOMImplementation domImpl =
                    GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document.
            String svgNS = "http://www.w3.org/2000/svg";
            Document document = domImpl.createDocument(svgNS, "svg", null);

            // Create an instance of the SVG Generator.
            svgGenerator = new SVGGraphics2D(document);
            svgGenerator.setSVGCanvasSize(new Dimension(wmm, hmm));
            writer = new PrintWriter(op, "UTF-8");
        } else {
            writer = new PrintWriter(op, "UTF-8");
            writer.println("<svg width=\"" + wmm + "mm\" height=\"" + hmm + "mm\" xmlns=\"http://www.w3.org/2000/svg\" " +
                    "xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\">" +
                    " viewBox=\"0 0 " + wmm + " " + hmm + "\"");
            writer.println("<g style=\"display:inline\" inkscape:label=\"1-Layer\" id=\"1-layer\" inkscape:groupmode=\"layer\">");
            writer.println("<rect width=\"" + (wmm-1) + "mm\" height=\"" + (hmm-1) + "mm\" style=\"fill:none;stroke-width:3;stroke:rgb(0,0,0)\" />");
            writer.print("</g>");
        }

    }

    protected void startSVGPath(int layer) {
        if (!generateSVG) {
            writer.println("<g style=\"display:inline\" inkscape:label=\"" + layer + "-Layer\" id=\"" + layer + "-layer\" inkscape:groupmode=\"layer\">");
            writer.println("<path id=\"path\" d=\"");
        }

    }

    protected void endSVGPath(Color col) {
        if (!generateSVG) {
            String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
            writer.print("\" style=\"fill:none;stroke:" + hex + "\" />");
            writer.print("</g>");
        }
    }

    protected void endSVGPath(Color col, double width) {
        if (!generateSVG) {
            String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
            writer.print("\" style=\"fill:none;stroke:" + hex + ";stroke-width:"+width+"\" />");
            writer.print("</g>");
        }
    }

    protected void endSVG() throws IOException, TranscoderException {
        if (generateSVG) {
            boolean useCSS = false; // we want to use CSS style attributes
            try {
                svgGenerator.stream(writer, useCSS);
            } catch (SVGGraphics2DIOException e) {
                e.printStackTrace();
            }
        } else {
            writer.println("</svg>");
            writer.close();
        }


    }

    protected double dp(double p) {
        BigDecimal bd = BigDecimal.valueOf(p);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    protected void setup(int wmm, int hmm, String ipFile, String opFile, double scFactor) throws IOException {

        File ip = new File(ipFile);
        ibi = ImageIO.read(ip);
        w = ibi.getWidth();
        h = ibi.getHeight();


        border = (int) (0.5 * ((double) w * scaleOrig) * borderF);
        bordermm = (int) (0.5 * ((double) wmm * scaleOrig) * borderF);
        borderx = (int) (0.5 * ((double) w * scaleOrig) * borderF);
        bordery = (int) (0.5 * ((double) h * scaleOrig) * borderF);

        ww = (int) (2 * border + w * scaleOrig);
        hh = (int) (2 * border + h * scaleOrig);
        scaleSVG = scFactor * ((double) (wmm - 2 * bordermm) / ((double) w));

        obi = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, ww, hh);
        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(stroke));
        opG.drawRect(0, 0, ww, hh);

        if (generateSVG) {
            try {
                instance = new ICC_ColorSpace(ICC_Profile.getInstance(pathToCMYKProfile));
                borderx = borderx / scFactor;
                bordery = bordery / scFactor;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        startSVG(opFile, wmm, hmm, ww, hh);
    }

    public float[] rgbToCmyk(float... rgb) {
        if (rgb.length != 3) {
            throw new IllegalArgumentException();
        }

        float[] fromRGB = instance.fromRGB(rgb);
        return fromRGB;
    }

}
