package com.op;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

public class Base {
    protected String host = "../host/average/images/out/";

    protected double getSD(BufferedImage bi, int x, int y, int rad) {
        // HexShape hex = new HexShape(rad, rad, rad);
        int dia = rad * 2;
        if (x + dia >= bi.getWidth() || y + dia >= bi.getHeight() || dia <= 0) {
            return 255;
        }
        BufferedImage sub = bi.getSubimage(x, y, dia, dia);
        // double varRed = getVariance(sub, 0);
        // double varGreen = getVariance(sub, 1);
        // double varBlue = getVariance(sub, 2);
        // System.out.println("x,y=" + x + "," + y + " rgb=" + varRed + ":"
        // + varGreen + ":" + varBlue);
        double varGrey = getVariance(sub, 4);
        return varGrey;
    }

    protected double getVariance(BufferedImage image, int ind) {
        double mean = meanValue(image, ind);
        double sumOfDiff = 0.0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int[] arr = getRGBAG(image, x, y);
                double colour = arr[ind] - mean;
                sumOfDiff += Math.pow(colour, 2);
            }
        }
        return sumOfDiff / ((image.getWidth() * image.getHeight()) - 1);
    }

    protected double meanValue(BufferedImage image, int ind) {
        double tot = 0;
        double c = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                tot = tot + getRGBAG(image, x, y)[ind];
                c++;
            }
        }
        return tot / c;
    }

    public int[] getRGBAG(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        int aa = (rgb >>> 24) & 0x000000FF;
        int r = (rgb >>> 16) & 0x000000FF;
        int g = (rgb >>> 8) & 0x000000FF;
        int b = (rgb >>> 0) & 0x000000FF;
        int grey = (r + g + b) / 3;

        int[] arr = {r, g, b, aa, grey};
        return arr;
    }

    public static BufferedImage createAlphaBufferedImage(int ww, int hh) {
        BufferedImage opImage = null;
        try {
            opImage = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_ARGB);
        } catch (OutOfMemoryError oem) {
        }

        return opImage;
    }

    public static BufferedImage createBufferedImage(int ww, int hh) {
        BufferedImage opImage = null;
        try {
            opImage = new BufferedImage(ww, hh, BufferedImage.TYPE_INT_RGB);
        } catch (OutOfMemoryError oem) {
        }

        return opImage;
    }


    public static void savePNGFile(BufferedImage opImage, File outfile,
                                   double dpi) throws Exception {
        try {
            // Find a jpeg writer
            ImageWriter writer = null;
            Iterator<ImageWriter> iter = ImageIO
                    .getImageWritersByFormatName("png");
            IIOMetadata metadata = null;
            if (iter.hasNext()) {
                writer = (ImageWriter) iter.next();
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
                        .createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
                metadata = writer.getDefaultImageMetadata(typeSpecifier,
                        writeParam);
                if (metadata.isReadOnly()
                        || !metadata.isStandardMetadataFormatSupported()) {
                    // continue;
                }
                double dpmm = dpi / 25.4;
                IIOMetadataNode horiz = new IIOMetadataNode(
                        "HorizontalPixelSize");
                horiz.setAttribute("value", Double.toString(dpmm));
                IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
                vert.setAttribute("value", Double.toString(dpmm));
                IIOMetadataNode dim = new IIOMetadataNode("Dimension");
                dim.appendChild(horiz);
                dim.appendChild(vert);
                IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
                root.appendChild(dim);
                metadata.mergeTree("javax_imageio_1.0", root);
            }
            // Prepare output file
            ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
            writer.setOutput(ios);
            // Write the image
            writer.write(null, new IIOImage(opImage, null, metadata), null);
            // Cleanup
            ios.flush();
            writer.dispose();
            ios.close();
            opImage = null;
            System.gc();
            System.out.println("Saved " + outfile.getPath());
        } catch (Exception e) {
            throw e;
        }
    }


}