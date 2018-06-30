package main.java.com.op.average;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

public class Base {
    protected String host = "../host/average/images/out/";

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
        } catch (Exception e) {
            throw e;
        }
    }


}