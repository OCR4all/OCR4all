package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import de.uniwue.config.ProjectDirConfig;
import de.uniwue.feature.ImageDespeckle;
import de.uniwue.feature.ImageResize;


/**
 * Helper class for image based functionality
 */
public class ImageHelper {
    /**
     * Object to access project directory configuration
     */
    private ProjectDirConfig projDirConf;

    /**
     * Image resizing object to access resizing functionality
     */
    private ImageResize imageResize = null;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public ImageHelper(String projectDir) {
        projDirConf = new ProjectDirConfig(projectDir);
    }

    /**
     * Sets image resizing object
     *
     * @param imageResize
     */
    public void setImageResize(ImageResize imageResize) {
        this.imageResize = imageResize;
    }

    /**
     * Converts the given Mat of an image to a byte array
     *
     * @param img Mat of the image
     * @return Byte array of the image
     */
    private byte[] convertImageMatToByte(Mat img) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(projDirConf.IMG_EXT, img, matOfByte); 
        return matOfByte.toArray();
    }

    /**
     * Encodes the image of the given Mat to a base64 string
     *
     * @param img Mat of the image
     * @return Returns the image as a base64 string
     * @throws IOException
     */
    private String getImageAsBase64(Mat img) throws IOException {
        if (imageResize != null) {
            img = imageResize.getScaledImage(img);
        }

        byte[] returnBuff = convertImageMatToByte(img);
        if (returnBuff == null)
            return "";

        return Base64.getEncoder().encodeToString(returnBuff);
    }

    /**
     * Encodes the image file in the given path to a base64 string
     *
     * @param path Filesystem path to the image
     * @return Returns the image as a base64 string
     * @throws IOException
     */
    private String getImageAsBase64(String path) throws IOException {
        Mat img = Imgcodecs.imread(path);
        if (img.empty())
            return "";

        return getImageAsBase64(img);
    }

    /**
     * Gets the specified page image as base64 string
     *
     * @param pageID Identifier of the page (e.g 0002)
     * @param imageType Type of the image
     * @return Returns the image as a base64 string
     * @throws IOException
     */
    public String getPageImage(String pageID, String imageType) throws IOException {
        return getImageAsBase64(projDirConf.getImagePathByType(imageType) + pageID + projDirConf.IMG_EXT);
    }

    /**
     * Gets the specified page segment image as base64 string
     *
     * @param pageID Identifier of the page (e.g 0002)
     * @param segmentID Identifier of the segment (e.g 0002__000__paragraph)
     * @param imageType Type of the image
     * @return Returns the image as a base64 string
     * @throws IOException
     */
    public String getSegmentImage(String pageID, String segmentID, String imageType) throws IOException {
        return getImageAsBase64(projDirConf.PAGE_DIR + pageID + File.separator + segmentID + projDirConf.getImageExtensionByType(imageType));
    }

    /**
     * Gets the specified page line image of a segment as base64 string
     *
     * @param pageID Identifier of the page (e.g 0002)
     * @param segmentID Identifier of the segment (e.g 0002__000__paragraph)
     * @param lineID Identifier of the line (e.g 0002__000__paragraph__000)
     * @param imageType Type of the image
     * @return Returns the image as a base64 string
     * @throws IOException
     */
    public String getLineImage(String pageID, String segmentID, String lineID, String imageType) throws IOException {
        return getImageAsBase64(projDirConf.PAGE_DIR + pageID + File.separator + segmentID
                + File.separator + lineID + projDirConf.getImageExtensionByType(imageType));
    }

    /**
     * Binary despeckling and base64 encoding
     *
     * @param pageId Identifier of the page (e.g 0002)
     * @param maxContourRemovalSize Maximum size of the contours to be removed
     * @param illustrationType standard: the result image shows the resulting binary image | 
     *                          marked:  the result image shows the resulting binary image and additionally represents the removed contours
     * @return Resulting binary image as base64 string
     * @throws IOException 
     */
    public String getPreviewDespeckleAsBase64(String pageId, double maxContourRemovalSize, String illustrationType) throws IOException {
        Mat binImage = Imgcodecs.imread(projDirConf.BINR_IMG_DIR + File.separator + pageId + projDirConf.IMG_EXT);
        Mat despImage = ImageDespeckle.despeckle(binImage, maxContourRemovalSize, illustrationType);
        return getImageAsBase64(despImage);
    }
}
