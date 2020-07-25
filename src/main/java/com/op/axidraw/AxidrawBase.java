package com.op.axidraw;

import com.op.Base;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class AxidrawBase extends Base {
    protected PrintWriter writer;

    protected int w = 0;
    protected int h = 0;

    protected int dpF = 2;
    protected BufferedImage ibi;

    protected void startSVG(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        File op = new File(fileName);
        writer = new PrintWriter(op, "UTF-8");
        writer.println("<svg width=\"" + w + "\" height=\"" + h + "\" xmlns=\"http://www.w3.org/2000/svg\" " +
                "xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\">" +
                " viewBox=\"0 0 " + w + " " + h + "\"");
    }

    protected void startSVGPath(int layer) {
        writer.println("<g style=\"display:inline\" inkscape:label=\"" + layer + "-Layer\" id=\"" + layer + "-layer\" inkscape:groupmode=\"layer\">");
        writer.println("<path id=\"path\" d=\"");
    }

    protected void endSVGPath(Color col) {
        String hex = String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
        writer.print("\" style=\"fill:none;stroke:" + hex + "\" />");
        writer.print("</g>");
    }

    protected void moveTo(double x, double y) {
        writer.println("M" + dp(x) + " " + dp(y) + " ");
    }

    protected void lineTo(double x, double y) {
        writer.println("L" + dp(x) + " " + dp(y) + " ");
    }

    protected void endSVG() {
        writer.println("</svg>");
        writer.close();
    }

    protected double dp(double val) {
        BigDecimal bd = BigDecimal.valueOf(val);
        bd = bd.setScale(dpF, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    protected void setup(String ipFile, String opFile) throws IOException {

        File ip = new File(ipFile);
        ibi = ImageIO.read(ip);
        w = ibi.getWidth();
        h = ibi.getHeight();


        startSVG(opFile);
    }

}
