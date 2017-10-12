package feature;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class ImageResize {
    /**
     * Width to which the image should be resized
     */
    private int resizeWidth  = -1;

    /**
     * Height to which the image should be resized
     */
    private int resizeHeight = -1;

    /**
     * Constructor
     */
    public ImageResize(Integer resizeWidth, Integer resizeHeight) {
        this.resizeWidth  = resizeWidth  == null ? -1 : resizeWidth;
        this.resizeHeight = resizeHeight == null ? -1 : resizeHeight;
    }

    /**
     * Resizes an image file and stores it into a byte array
     *
     * @param path Filesystem path to the image
     * @return Byte array representation of the scaled image file
     * @throws IOException 
     */
    public byte[] getScaledImage(String path) throws IOException {
        File imageFile = new File(path);

        BufferedImage img = null;
        img = ImageIO.read(imageFile);
        Dimension dimension = getDimension(img);
        if (dimension != null) {
            img = scaleImage(img,dimension);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, FilenameUtils.getExtension(imageFile.getName()), baos);
        return baos.toByteArray();
    }

    /**
     * Calculates the dimension of the image if only height or width is handed over
     *
     * @param img The image to be scaled
     * @return
     */
    private Dimension getDimension(BufferedImage img) {
        Dimension dimension = null;
        if(resizeHeight != -1 || resizeWidth != -1) {
            if(resizeHeight != -1 && resizeWidth != -1) {
                return new Dimension(resizeWidth,resizeHeight);
            }
            else if (resizeHeight == -1) {
                double factor = img.getWidth()/resizeWidth;
                return new Dimension(resizeWidth,(int) (img.getHeight()/factor));
            }
            else {
                double factor = img.getHeight()/resizeHeight;
                 return new Dimension((int) (img.getWidth()/factor),resizeHeight);
            }
        }
        return dimension;
    }

    /**
     * Downscales image
     * Fastest way to scale pictures is with the Nearest Neighbor algorithm
     * Source code from: http://www.locked.de/2009/06/08/fast-image-scaling-in-java/ 
     *
     * @param img Bufferd image
     * @param d Dimension of the downsized image
     * @return Downsized image
     */
    private BufferedImage scaleImage(BufferedImage img, Dimension d) {
        img = scaleByHalf(img, d);
        img = scaleExact(img, d);
        return img;
    }

    private BufferedImage scaleByHalf(BufferedImage img, Dimension d) {
        int w = img.getWidth();
        int h = img.getHeight();
        float factor = getBinFactor(w, h, d);

        // make new size
        w *= factor;
        h *= factor;
        BufferedImage scaled = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }

    private BufferedImage scaleExact(BufferedImage img, Dimension d) {
        float factor = getFactor(img.getWidth(), img.getHeight(), d);

        // create the image
        int w = (int) (img.getWidth() * factor);
        int h = (int) (img.getHeight() * factor);
        BufferedImage scaled = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }

    private float getBinFactor(int width, int height, Dimension dim) {
        float factor = 1;
        float target = getFactor(width, height, dim);
        if (target <= 1) { while (factor / 2 > target) { factor /= 2; }
        } else { while (factor * 2 < target) { factor *= 2; }         }
        return factor;
    }

    private float getFactor(int width, int height, Dimension dim) {
        float sx = dim.width / (float) width;
        float sy = dim.height / (float) height;
        return Math.min(sx, sy);
    }
}
