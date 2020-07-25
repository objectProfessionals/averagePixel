package com.op.axidraw.paint;

import com.op.axidraw.AxidrawBase;

import java.awt.*;
import java.util.Random;

public class AxidrawPaint extends AxidrawBase {
    private static final AxidrawPaint axidrawPaint = new AxidrawPaint();
    private String dir = host + "axidrawPaint/";
    private String ipFile = "VirgaBWSq";
    private String opFile = ipFile + "_PAINT";

    public static void main(String[] args) throws Exception {
        axidrawPaint.run();
    }

    private void run() throws Exception {
        setup(dir + ipFile + ".jpg", dir + opFile + ".svg");

        //draw();
        drawSVG();

        save();
    }

    private void drawSVG() {
        startSVGPath(0);
        moveTo(0, 0);
        lineTo(w, 0);
        lineTo(w, h);
        lineTo(0, h);
        lineTo(0, 0);
        endSVGPath(Color.BLACK);
    }

    private void save() throws Exception {
        endSVG();
    }


}
