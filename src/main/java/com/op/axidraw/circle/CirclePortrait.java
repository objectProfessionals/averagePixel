package com.op.axidraw.circle;

import com.op.axidraw.AxidrawBaseOLD;

import java.awt.*;
import java.util.Random;

public class CirclePortrait extends AxidrawBaseOLD {
    private static final CirclePortrait linePortrait = new CirclePortrait();
    private String dir = host + "circlePortrait/";
    private String ipFile = "VS4a";
    private String opFile = ipFile + "_CIRC";
    private int wmm = 297;
    private int hmm = 210;
    private double scFact = 4;
    private double step = 2;

    private Random random = new Random(1);
    int count = 0;
    int layerCount = 0;

    public static void main(String[] args) throws Exception {
        linePortrait.run();
    }

    private void run() throws Exception {
        setup(wmm, hmm, dir + ipFile + ".png", dir + opFile + ".svg", scFact);

        drawSVG(Color.WHITE);

        save();
    }

    private void drawSVG(Color col) {
        writer.println("<g style=\"display:inline\" inkscape:label=\"1-Layer\" id=\""+layerCount+"-layer\" inkscape:groupmode=\"layer\">");
        writer.println("<path id=\"path\" d=\"\n");
        double sc = 1 / scaleSVG;
        double bordx = borderF * w;
        double bordy = borderF * h;
        double rad = 0.5;
        //double rndSize = 1 * xStep * thickness / passes;

        opG.setColor(Color.BLACK);
        opG.setStroke(new BasicStroke(1f));
        for (double y = 0; y < h; y = y + step) {
            for (double x = 0; x < w; x = x + (random.nextDouble() * step)) {
                double yy = y + ((random.nextDouble()*step - step/2));
                double val = getValueAsCol(col, x, y);
                if (val > 0.9) {
                    double cx = dp(bordx + x * sc);
                    double cy = dp(bordy + yy * sc);
                    double rr = dp(rad);
                    //writer.println("<circle cx=\"" + cx + "\" cy=\"" + cy + "\" r=\"" + rr + "\"/>");
                    //writer.println(describeArc(cx, cy, rr, 0, 359));
                    writer.println(describeDot(cx, cy));
                    count++;
                }
            }
        }

        String re = Integer.toHexString(col.getRed());
        String gr = Integer.toHexString(col.getGreen());
        String bl = Integer.toHexString(col.getBlue());
        if (re.length() == 1) re = "0" + re;
        if (gr.length() == 1) gr = "0" + gr;
        if (bl.length() == 1) bl = "0" + bl;

        String c = "#" + re + gr + bl;
        if (col.equals(Color.WHITE)) {
            c = "#000000";
        }


        writer.println("\" style=\"fill:none;stroke:"+c+"; stroke-width:1\" />");
        writer.print("</g>");
        System.out.println("count: "+count);
    }

    private double getValueAsCol(Color col, double x, double y) {
        int rgb = ibi.getRGB((int) x, (int) y);
        if (col.equals(Color.WHITE)) {
            Color c = new Color(rgb);
            double R = (double) (c.getRed()) / 255.0;
            double G = (double) (c.getGreen()) / 255.0;
            double B = (double) (c.getBlue()) / 255.0;
            double grey = ((R + G + B) / 3.0);
            return grey;
        } else if (col.equals(Color.RED)) {
            Color c = new Color(rgb);
            return (double) (c.getRed()) / 255.0;
        } else if (col.equals(Color.GREEN)) {
            Color c = new Color(rgb);
            return (double) (c.getGreen()) / 255.0;
        } else if (col.equals(Color.BLUE)) {
            Color c = new Color(rgb);
            return (double) (c.getBlue()) / 255.0;
        } else if (col.equals(Color.YELLOW)) {
            Color c = new Color(rgb);
            double G = (double) (c.getGreen()) / 255.0;
            double B = (double) (c.getBlue()) / 255.0;
            double grey = ((G + B) / 2.0);
            return grey;
        }

        return 0;
    }

    private String describeDot(double cx, double cy) {
        double rad = dp(1);
        return "M " + cx +" "+ cy +" L " + (cx+rad) +" "+ (cy+rad);
    }

    private double [] polarToCartesian(double centerX, double centerY, double radius, double angleInDegrees) {
        double  angleInRadians = (angleInDegrees-90) * Math.PI / 180.0;

        double arr[] = {centerX + (radius * Math.cos(angleInRadians)), centerY + (radius * Math.sin(angleInRadians))};

        return arr;
    }

    private String describeArc(double x, double y, double radius, double startAngle, double endAngle){

        double[] start = polarToCartesian(x, y, radius, endAngle);
        double[] end = polarToCartesian(x, y, radius, startAngle);

        String largeArcFlag = endAngle - startAngle <= 180 ? "0" : "1";

        String d = "M " + start[0] +" "+ start[1] +" A " + radius +" "+ radius +" 0 "+
        largeArcFlag +" 0 " + end[0] +" "+ end[1];

        return d;
    }
    private void save() throws Exception {
        endSVG();
    }


}
