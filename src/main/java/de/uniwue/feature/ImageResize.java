package de.uniwue.feature;

import java.awt.Dimension;
import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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
        Mat mat = Imgcodecs.imread(path);
        Dimension dimension = getDimension(mat);
        if (dimension != null) {
            Imgproc.resize( mat, mat, new Size(dimension.width, dimension.height) );
        }
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", mat, matOfByte); 
        byte[] return_buff = matOfByte.toArray();
        return return_buff;
    }

    public byte[] getScaledImage(Mat img) throws IOException {

        Dimension dimension = getDimension(img);
        if (dimension != null) {
            Imgproc.resize( img, img, new Size(dimension.width, dimension.height) );
        }
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", img, matOfByte); 
        byte[] return_buff = matOfByte.toArray();
        return return_buff;
    }

    /**
     * Calculates the dimension of the image if only height or width is handed over
     *
     * @param img The image to be scaled
     * @return
     */
    private Dimension getDimension(Mat img) {
        Dimension dimension = null;
        if (resizeHeight != -1 || resizeWidth != -1) {
            if (resizeHeight != -1 && resizeWidth != -1) {
                return new Dimension(resizeWidth,resizeHeight);
            }
            else if (resizeHeight == -1) {
                double factor = (double) img.cols() / resizeWidth;
                System.out.println(factor+ "<-- Facotr img.cols "+ img.cols() + " " + resizeWidth );
                return new Dimension(resizeWidth,(int) (img.rows() / factor));
            }
            else {
                double factor = img.rows() / resizeHeight;
                 return new Dimension((int) (img.cols() / factor),resizeHeight);
            }
        }
        return dimension;
    }

}
