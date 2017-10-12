package de.uniwue.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import de.uniwue.config.ProjectDirConfig;
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
     * Returns the filesystem path of the given image type
     *
     * @param imageType Type of the image
     * @return Absolute filesystem path to the image
     */
    private String getImagePathByType(String imageType) {
        String imagePath = null;
        switch(imageType) {
            case "Original":   imagePath = projDirConf.ORIG_IMG_DIR; break;
            case "Binary":     imagePath = projDirConf.BINR_IMG_DIR; break;
            case "Gray":       imagePath = projDirConf.GRAY_IMG_DIR; break;
            case "Despeckled": imagePath = projDirConf.DESP_IMG_DIR; break;
            default: break;
        }
        return imagePath;
    }

    /**
     * Returns the file extension of the given image type
     *
     * @param imageType Type of the image
     * @return Image file extension
     */
    private String getImageExtensionByType(String imageType) {
        String imageExtension = null;
        switch(imageType) {
            case "Binary": imageExtension = projDirConf.GRAY_IMG_EXT; break;
            case "Gray":   imageExtension = projDirConf.BIN_IMG_EXT;  break;
            default: break;
        }
        return imageExtension;
    }

    /**
     * Reads an image file and stores it into a byte array 
     *
     * @param path Filesystem path to the image
     * @return Byte array representation of the image file
     * @throws IOException
     */
    private byte[] readImageFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStreamReader.read(bytes);
        fileInputStreamReader.close();
        return bytes;
    }

    /**
     * Encodes the given image file to a base64 string
     *
     * @param path Filesystem path to the image
     * @return Returns the image as a base64 string
     * @throws IOException
     */
    private String getImageAsBase64(String path) throws IOException {
        // Resizing is required
        if (imageResize != null)
            return Base64.getEncoder().encodeToString(imageResize.getScaledImage(path));

        return Base64.getEncoder().encodeToString(readImageFile(path));
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
        return getImageAsBase64(getImagePathByType(imageType) + pageID + projDirConf.IMG_EXT);
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
        return getImageAsBase64(projDirConf.PAGE_DIR + pageID + File.separator + segmentID + getImageExtensionByType(imageType));
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
                + File.separator + lineID + getImageExtensionByType(imageType));
    }

    /**
     * Gets all pages of the project and the images of the given type as base64 strings
     *
     * @param imageType Type of the images
     * @return Map of page IDs with their images as base64 string
     * @throws IOException
     */
    public TreeMap<String, String> getImageList(String imageType) throws IOException {
        TreeMap<String, String> imageList = new TreeMap<String, String>();

        final File folder = new File(getImagePathByType(imageType));
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile())
                imageList.put(FilenameUtils.removeExtension(fileEntry.getName()), getImageAsBase64(fileEntry.getAbsolutePath()));
        }
        return imageList;
    }


    public static Mat despeckle(Mat binary, double maxArea) {
        Mat inverted = new Mat();
        Core.bitwise_not(binary, inverted);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(inverted, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat result = binary.clone();

        if (contours.size() > 1) {
            ArrayList<MatOfPoint> toRemove = new ArrayList<MatOfPoint>();

            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);

                if (area < maxArea) {
                    toRemove.add(contour);
                }
            }

            Imgproc.drawContours(result, toRemove, -1, new Scalar(255), -1);
        }

        return result;
    }
}
