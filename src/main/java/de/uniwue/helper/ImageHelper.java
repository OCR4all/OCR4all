package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
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
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
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
     * Encodes the given image file to a base64 string
     *
     * @param path Filesystem path to the image
     * @return Returns the image as a base64 string
     * @throws IOException
     */
    private String getImageAsBase64(String path) throws IOException {
            byte[] return_buff = imageResize.getScaledImage(path);
            if (return_buff != null)
                return Base64.getEncoder().encodeToString(imageResize.getScaledImage(path));
            return "";
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
     * Gets specified pages of the project and the images of the given type as base64 strings
     *
     * @param imageType Type of the images
     * @param skip Amount of images to skip
     * @param limit Amount of images to fetch
     * @return Map of page IDs with their images as base64 string
     * @throws IOException
     */
    public TreeMap<String, String> getImageList(String imageType, long skip, long limit) throws IOException {
        TreeMap<String, String> imageList = new TreeMap<String, String>();

        Files.walk(Paths.get(getImagePathByType(imageType)))
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .sorted()
        .skip(skip)
        .limit(limit)
        .forEach(
            fileEntry -> {try {
                imageList.put(FilenameUtils.removeExtension(fileEntry.getName()), getImageAsBase64(fileEntry.getAbsolutePath()));
            } catch (IOException e) { 
                // Ignore occurring errors (files will not show in list)
            }
        });

        return imageList;
    }

    /** Despeckling of a Binary image
     * 
     * @param binary Binary image
     * @param maxArea maximum size of the contours to be removed
     * @param modus standard: the result image shows the resulting binary image | 
     *              marked: the result image shows the resulting binary image and additionally represents the removed contours
     * @return Resulting binary image
     */
    public Mat despeckle(Mat binary, double maxArea, String modus) {
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
            if(modus.equals("marked")) {
                Imgproc.cvtColor(result, result, Imgproc.COLOR_GRAY2BGR);
                Imgproc.drawContours(result, toRemove, -1, new Scalar(0, 0, 255), -1);
            }
            else
                Imgproc.drawContours(result, toRemove, -1, new Scalar(255), -1);

        }

        return result;
    }

    /** Binary despeckling and base64 encoding
     * 
     * @param pageID Identifier of the page (e.g 0002)
     * @param maxArea Maximum size of the contours to be removed
     * @param modus Standard: the result image shows the resulting binary image | 
     *              Marked: the result image shows the resulting binary image and additionally represents the removed contours
     * @return Resulting binary image as base64 string
     * @throws IOException 
     */
    public String getPreviewDespeckleAsBase64(String pageID, double maxArea, String modus) throws IOException {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        Mat mat = Imgcodecs.imread(projDirConf.BINR_IMG_DIR + File.separator + pageID + projDirConf.IMG_EXT);
        Mat bwImage = new Mat();
        Imgproc.cvtColor(mat, bwImage, Imgproc.COLOR_RGB2GRAY);
        mat = despeckle(bwImage, maxArea, modus);
        if (imageResize != null)
            return Base64.getEncoder().encodeToString(imageResize.getScaledImage(mat));
        return "";
    }
}
