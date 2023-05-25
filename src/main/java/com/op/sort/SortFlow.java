package com.op.sort;

import com.op.Base;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SortFlow extends Base {

    private static SortFlow sortFlow = new SortFlow();
    private String dir = host + "sort/";
    private String opFile = "SORT";
    private BufferedImage obi;
    private Graphics2D opG;
    private int w = 1000;
    private int h = 1000;
    private double dpi = 300;
    private ArrayList<SortRow> sortRows = new ArrayList<>();
    private ArrayList<SortPositions> positionsList = new ArrayList();

    public static void main(String[] args) throws Exception {
        sortFlow.run();
    }

    private void run() throws Exception {
        setup();

        setupValues();

        setupPlot();

        plotAll();

        save();
    }

    private void plotAll() {
        int sep = 50;
        float c = 0;
        int i = 0;
        Path2D[] path2Ds = new Path2D[6];
        for (int x = 0; x< 6; x++) {
            Path2D path2D = new Path2D.Double();
            path2D.moveTo(sep, sep * positionsList.get(0).positions.get(x));
            path2Ds[x] = path2D;
        }
        i = 0;
        for (SortPositions sp : positionsList) {
            if (i == 0) {
                i++;
                continue;
            }
            int ii = 0;
            for (Path2D path2D : path2Ds) {
                path2D.lineTo(sep + sep * i, sep * sp.positions.get(ii));
                ii++;
            }
            i++;
        }

        i = 0;
        for (Path2D p : path2Ds) {
            SortRow sr = sortRows.get(i);
            Color col = new Color(c, c, c, 1);
            opG.setColor(col);
            opG.drawString(sr.label, 10, sep* sr.value);
            SortPositions lsp = positionsList.get(0);
            int ii = 1 + lsp.positions.indexOf(i+1);
            opG.drawString(sr.label, 900, sep* ii);
            opG.draw(p);
            c = c + 0.1f;
            i++;
        }

    }

    private void setupPlot() {
        SortRow[] sortRowsArr = new SortRow[sortRows.size()];
        int i = 0;
        for (SortRow sr : sortRows) {
            sortRowsArr[i] = sr;
            i++;
        }

        bubbleSort(sortRowsArr);
        sortRows = new ArrayList();
        for (SortRow sr : sortRowsArr) {
            sortRows.add(sr);
        }
    }

    public void bubbleSort(SortRow[] sr) {
        boolean sorted = false;
        SortRow temp;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < sr.length - 1; i++) {
                if (sr[i].value > sr[i + 1].value) {
                    temp = sr[i];
                    sr[i] = sr[i + 1];
                    swapPositions(i, i + 1);
                    sr[i + 1] = temp;
                    sorted = false;
                }
            }
        }
    }

    private void swapPositions(int i1, int i2) {
        if (positionsList.isEmpty()) {
            SortPositions sp = new SortPositions();
            for (SortRow r : sortRows) {
                sp.positions.add(r.value);
            }
            positionsList.add(sp);
        }
        SortPositions sp = new SortPositions();
        int i = 0;
        for (SortRow r : sortRows) {
            if (i == i1) {
                sp.positions.add(positionsList.get(positionsList.size()-1).positions.get(i2));
            } else if (i == i2) {
                sp.positions.add(positionsList.get(positionsList.size()-1).positions.get(i1));
            } else {
                sp.positions.add(positionsList.get(positionsList.size()-1).positions.get(i));
            }
            i++;
        }
        positionsList.add(sp);

    }

    private void setupValues() {

        SortRow sr1 = new SortRow(6, "S");
        SortRow sr2 = new SortRow(1, "A");
        SortRow sr3 = new SortRow(3, "N");
        SortRow sr4 = new SortRow(5, "J");
        SortRow sr5 = new SortRow(4, "A");
        SortRow sr6 = new SortRow(2, "Y");

        sortRows.add(sr1);
        sortRows.add(sr2);
        sortRows.add(sr3);
        sortRows.add(sr4);
        sortRows.add(sr5);
        sortRows.add(sr6);
    }


    void setup() throws IOException {

        obi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        opG = (Graphics2D) obi.getGraphics();
        opG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_OFF);
        opG.setColor(Color.WHITE);
        opG.fillRect(0, 0, w, h);

        opG.setStroke(new BasicStroke(3));
    }

    private void save() throws Exception {
        File op1 = new File(dir + opFile + ".png");
        savePNGFile(obi, op1, dpi);
    }
}
