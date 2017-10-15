package de.uniwue.feature;

import java.awt.Dimension;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import de.uniwue.config.ProjectDirConfig;

public class ImageResize {
    /**
     * Object to access project directory configuration
     */
    private ProjectDirConfig projDirConf;

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
        // Load OpenCV library (!important)
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

        this.projDirConf  = new ProjectDirConfig();
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
        Mat mat = Imgcodecs.imread(path);
        if (mat.empty())
            return null;

        Dimension dimension = getDimension(mat);
        if (dimension != null) {
            Imgproc.resize(mat, mat, new Size(dimension.width, dimension.height) );
        }

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(projDirConf.IMG_EXT, mat, matOfByte); 
        return matOfByte.toArray();
    }

    /**
     * Resizes an image file and stores it into a byte array
     *
     * @param img Mat of the img
     * @return Byte array representation of the scaled image file
     * @throws IOException 
     */
    public byte[] getScaledImage(Mat img) throws IOException {
        Dimension dimension = getDimension(img);
        if (dimension != null) {
            Imgproc.resize(img, img, new Size(dimension.width, dimension.height) );
        }

        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(projDirConf.IMG_EXT, img, matOfByte); 
        return matOfByte.toArray();
    }

    /**
     * Calculates the dimension of the image if only height or width is handed over
     *
     * @param img The image to be scaled
     * @return Calculated dimension
     */
    private Dimension getDimension(Mat img) {
        Dimension dimension = null;
        if (resizeHeight != -1 || resizeWidth != -1) {
            if (resizeHeight != -1 && resizeWidth != -1) {
                return new Dimension(resizeWidth,resizeHeight);
            }
            else if (resizeHeight == -1) {
                double factor = (double) img.cols() / resizeWidth;
                return new Dimension(resizeWidth, (int) (img.rows() / factor));
            }
            else {
                double factor = img.rows() / resizeHeight;
                 return new Dimension((int) (img.cols() / factor), resizeHeight);
            }
        }
        return dimension;
    }
}
