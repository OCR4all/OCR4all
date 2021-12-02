package de.uniwue.feature;

import java.awt.Dimension;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Class to resize images
 */
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
     * Resizes an image file
     *
     * @param img Mat of the img
     * @return Mat representation of the scaled image file
     */
    public Mat getScaledImage(final Mat img) {
        Dimension dimension = getDimension(img);
        if (dimension != null) {
            Imgproc.resize(img, img, new Size(dimension.width, dimension.height) );
        }

        return img;
    }

    /**
     * Calculates the dimension of the image if only height or width is handed over
     *
     * @param img The image to be scaled
     * @return Calculated dimension
     */
    private Dimension getDimension(final Mat img) {
        if (resizeHeight != -1 || resizeWidth != -1) {
            if (resizeHeight != -1 && resizeWidth != -1) {
                return new Dimension(resizeWidth,resizeHeight);
            }
            else if (resizeHeight == -1) {
                double factor = (double) img.cols() / resizeWidth;
                return new Dimension(resizeWidth, (int) (img.rows() / factor));
            }
            else {
                double factor = (double) img.rows() / resizeHeight;
                return new Dimension((int) (img.cols() / factor), resizeHeight);
            }
        }
        return null;
    }
}
