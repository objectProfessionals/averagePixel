package com.op;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class Base {
    protected String host = "../host/average/images/out/";

    protected double getSDColor(BufferedImage bi, int x, int y, int rad) {
        int dia = rad * 2;
        if (x + dia >= bi.getWidth() || y + dia >= bi.getHeight() || dia <= 0) {
            return 255;
        }
        BufferedImage sub = bi.getSubimage(x, y, dia, dia);
        double varRed = getVariance(sub, 0);
        double varGreen = getVariance(sub, 1);
        double varBlue = getVariance(sub, 2);
//         System.out.println("x,y=" + x + "," + y + " rgb=" + varRed + ":"
//         + varGreen + ":" + varBlue);
        return (varRed + varGreen + varBlue) / 3;
    }

    protected double getSDColor(BufferedImage bi, int x, int y, Rectangle2D rect) {
        int ww= (int)rect.getWidth();
        int hh= (int)rect.getHeight();
        if (x < 0 || y  < 0 || x + ww >= bi.getWidth() || y + hh >= bi.getHeight()) {
            return 255;
        }
        BufferedImage sub = bi.getSubimage(x, y, ww, hh);
        double varRed = getVariance(sub, 0);
        double varGreen = getVariance(sub, 1);
        double varBlue = getVariance(sub, 2);
//         System.out.println("x,y=" + x + "," + y + " rgb=" + varRed + ":"
//         + varGreen + ":" + varBlue);
        return (varRed + varGreen + varBlue) / 3;
    }

    protected double getSDColor(BufferedImage bi, int x, int y, Rectangle2D rect, int border) {
        int ww= (int)rect.getWidth();
        int hh= (int)rect.getHeight();
        if (x - border < 0 || y - border < 0 || x + ww + border >= bi.getWidth() || y + hh +border >= bi.getHeight()) {
            return 255;
        }
        BufferedImage sub = bi.getSubimage(x-border, y-border, ww+border, hh + border);
        double varRed = getVariance(sub, 0);
        double varGreen = getVariance(sub, 1);
        double varBlue = getVariance(sub, 2);
//         System.out.println("x,y=" + x + "," + y + " rgb=" + varRed + ":"
//         + varGreen + ":" + varBlue);
        return (varRed + varGreen + varBlue) / 3;
    }

    protected double getSDGrey(BufferedImage bi, int x, int y, int rad) {
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

    public double getGrey(BufferedImage image, int x, int y) {
        Color rgb = new Color(image.getRGB(x, y));
        double grey = ((double) (rgb.getRed() + rgb.getGreen() + rgb.getBlue())) / (255.0 * 3.0);
        return grey;
    }

    public boolean isOnImage(BufferedImage image, int x, int y, double bf) {
        int bx = (int) (bf * ((double) image.getWidth()));
        int by = (int) (bf * ((double) image.getHeight()));
        return (x > bx && x < image.getWidth() - bx && y > by && y < image.getHeight() - by);
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


    public static void saveJPGFile(BufferedImage opImage, File outfile, double dpi) {
        try {
            // Find a jpeg writer
            ImageWriter writer = null;
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
            IIOMetadata metadata = null;
            if (iter.hasNext()) {
                writer = iter.next();
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                if (writeParam.canWriteCompressed()) {
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionQuality(1f);
                }
                ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
                        .createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
                metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
                if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                    // continue;
                }
                double dpmm = dpi / 25.4;
                IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
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
            System.out.println("Saved " + outfile);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void saveJPGFile2(BufferedImage opImage, File outfile, double dpi) {
        try {
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(1f);

            jpgWriter.setOutput(ImageIO.createImageOutputStream(outfile));
            IIOImage outputImage = new IIOImage(opImage, null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();
            System.out.println("Saved " + outfile.getPath());
        } catch (Exception e) {
            System.err.println(e);
        }
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