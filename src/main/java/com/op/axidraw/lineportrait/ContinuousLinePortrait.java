package com.op.axidraw.lineportrait;

import com.op.axidraw.AxidrawBase;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static java.awt.Color.*;

public class ContinuousLinePortrait extends AxidrawBase {
    private static final ContinuousLinePortrait linePortrait = new ContinuousLinePortrait();
    private String dir = host + "linePortrait/";
    private String ipFile = "Virga2";
    private String opFile = ipFile + "_CLINE";
    private int imgWmm = 200;
    private int imgHmm = 200;
    private int paperWmm = 200;
    private int paperHmm = 200;
    private double strmm = 0.25;
    private double lineBorderF = 0.025;
    private double lineStepMinmm = 1;
    private double lineStepMaxmm = 2;
    private double maxPathStarts = 1000;
    private double maxPathLines = maxPathStarts * 0.1;
    private double minLinesPerPath = maxPathLines * 0.1;

    private Random random = new Random(1);

    public static void main(String[] args) throws Exception {
        linePortrait.run();
    }

    private void run() throws Exception {
        setup(host + ipFile + ".png", dir + opFile + ".svg", imgWmm, imgHmm, paperWmm, paperHmm);

        //draw();
//        int tot = drawSVG(BLACK, 0.0, 0.5);

        int tot = drawSVG("1", YELLOW, 0.2, 0.5);
        tot = tot + drawSVG("2", CYAN, 0.2, 0.5);
        tot = tot + drawSVG("3", MAGENTA, 0.2, 0.5);
        tot = tot + drawSVG("4", BLACK, 0.2, 0.7);

        System.out.println("total=" + tot);
        save();
    }

    private int drawSVG(String layer, Color col, double greyMin, double greyMax) {
        startSVGPath("1-" + getColorName(col), 0, 0);
        double str = strmm * scale;
        int c = 0;
        ArrayList<Path2D.Double> paths = new ArrayList<Path2D.Double>();
        for (int r = 0; r < maxPathStarts; r++) {
            double x = imageW * lineBorderF + (random.nextDouble() * imageW * (1 - (2 * lineBorderF)));
            double y = imageH * lineBorderF + (random.nextDouble() * imageH * (1 - (2 * lineBorderF)));
            double off = random.nextDouble() * 360;
            double radmm = lineStepMinmm + random.nextDouble() * lineStepMaxmm;
            Path2D.Double path = drawLinePart(col, x, y, radmm, 360, off, greyMin, greyMax);
            if (path != null) {
                paths.add(path);
                c++;
            }
        }
        addPaths(paths);
        endSVGPath(col, str);
        return c;
    }

    private void addPaths(ArrayList<Path2D.Double> paths) {
        Collections.sort(paths, new Comparator<Path2D.Double>() {
            @Override
            public int compare(Path2D.Double o1, Path2D.Double o2) {
                PathIterator pi1 = o1.getPathIterator(null);
                double[] coords1 = {0, 0};
                pi1.currentSegment(coords1);
                int x1 = (int) coords1[0];
                int y1 = (int) coords1[1];

                PathIterator pi2 = o2.getPathIterator(null);
                double[] coords2 = {0, 0};
                pi2.currentSegment(coords2);
                int x2 = (int) coords2[0];
                int y2 = (int) coords2[1];

                int z1 = x1 + y1 * imageW;
                int z2 = x2 + y2 * imageW;

                return z1 - z2;
            }
        });
        for (Path2D.Double path : paths) {
            PathIterator pi = path.getPathIterator(null);
            double[] coords = {0, 0};
            pi.currentSegment(coords);
            moveTo(coords[0], coords[1]);
            pi.next();
            while (!pi.isDone()) {
                double[] coords2 = {0, 0};
                pi.currentSegment(coords2);
                lineTo(coords2[0], coords2[1]);
                pi.next();
            }
        }
    }

    private Path2D.Double drawLinePart(Color col, double x, double y, double radmm, double max, double offset, double greyStart, double greyEnd) {
        double rad = radmm * scale;
        double radColmm = 10;
        double radCol = radColmm * scale;

        double grey = getAverageColor(x, y, radCol, col);
        if (grey < greyStart) {
            return null;
        }
        double ang = getAngFromGrey(grey, max, offset);
        double xx = x + rad * Math.cos(ang);
        double yy = y + rad * Math.sin(ang);
        grey = getAverageColor(xx, yy, radCol, col);
        if (grey < greyStart) {
            return null;
        }
        Path2D.Double p = new Path2D.Double();
        p.moveTo(x, y);
        p.lineTo(xx, yy);
        int count = 0;
        double lastGrey = grey;
        while (insideImage(xx, yy) && count < maxPathLines) {
            grey = getAverageColor(xx, yy, radCol, col);
            if (grey > greyEnd) {
                break;
            }
            if (Math.abs(lastGrey - grey) > 0.25) {
                break;
            }
            ang = getAngFromGrey(grey, max, offset);
            double xxx = xx + rad * Math.cos(ang);
            double yyy = yy + rad * Math.sin(ang);
            p.lineTo(xxx, yyy);
            xx = xxx;
            yy = yyy;
            count++;
            lastGrey = grey;
        }

        if (count < minLinesPerPath) {
            return null;
        }
        return p;
    }

    private double getAngFromGrey(double grey, double max, double offset) {
        return Math.toRadians(offset + max * grey);
    }

    private boolean insideImage(double xx, double yy) {
        return (xx > imageW * lineBorderF && xx < imageW * (1 - lineBorderF) && yy > imageH * lineBorderF && yy < imageH * (1 - lineBorderF));
    }

    private boolean sameGrey(double grey, double xx, double yy) {
        double g2 = getGrey(xx, yy);
        return (Math.abs(g2 - grey) < 0.1);
    }

    private void save() throws Exception {
        endSVG();
    }


}
