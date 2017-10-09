package de.uniwue.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectDirConfig;

/**
 * Helper class for image based functionality
 */
public class ImageHelper {
    /**
     * Object to access project directory configuration
     */
    private ProjectDirConfig projDirConf;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public ImageHelper(String projectDir) {
        projDirConf = new ProjectDirConfig(projectDir);
    }

    /**
     * Encodes the given file to base64 String
     *
     * @param File Passed file
     * @return Returns the image as a base64 string
     */
    public String encodeFileToBase64Binary(File file) throws IOException {
        String encodedfile = null;
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStreamReader.read(bytes);
        encodedfile = Base64.getEncoder().encodeToString(bytes);
        fileInputStreamReader.close();
        return encodedfile;
    }

    /**
     * Gets the specified page image and encodes it to base64
     *
     * @param pageID Identifier of the page (e.g 0002)
     * @param imageID Image identifier (Original, Gray or Despeckled)
     * @return Returns the image as a base64 string
     */
    public String getPageImage(String pageID, String imageID) throws IOException {
        String base64Image = null;
        File f = null;
        if (imageID.equals("Original")) {
            f = new File(projDirConf.ORIG_IMG_DIR + pageID + projDirConf.IMG_EXT);
        }
        else {
            if (imageID.equals("Gray")) {
                f = new File(projDirConf.GRAY_IMG_DIR + File.separator + pageID + projDirConf.IMG_EXT);
            }
            else if (imageID.equals("Despeckled")) {
                f = new File(projDirConf.DESP_IMG_DIR + File.separator + pageID + projDirConf.IMG_EXT);
            }
            else {
                f = new File(projDirConf.BINR_IMG_DIR + File.separator + pageID + projDirConf.IMG_EXT);
            }
        }

        if (f.exists())
            base64Image = encodeFileToBase64Binary(f);
        return base64Image;
    }

    /**
     * Gets the specified page segment image and encodes it to base64
     *
     * @param pageID Identifier of the page (e.g 0002)
     * @param segmentID Identifier of the segment (e.g 0002__000__paragraph)
     * @param imageType Image identifier (Binary or Grey-image)
     * @return Returns the image as a base64 string
     */
    public String getSegmentImage(String pageID, String segmentID, String imageType)
            throws IOException {
        String base64Image = null;
        File f = null;
        if (imageType.equals("Gray")) {
            f = new File(projDirConf.PAGE_DIR + pageID + File.separator + segmentID + projDirConf.GRAY_IMG_EXT);
        }
        else {
            f = new File(projDirConf.PAGE_DIR + pageID + File.separator + segmentID + projDirConf.BIN_IMG_EXT);
        }

        if (f.exists())
            base64Image = encodeFileToBase64Binary(f);
        return base64Image;
    }

    /**
     * Gets the specified page line image of a segment and encodes it to base64
     *
     * @param pageID Identifier of the page (e.g 0002)
     * @param segmentID Identifier of the segment (e.g 0002__000__paragraph)
     * @param lineID Identifier of the line (e.g 0002__000__paragraph__000)
     * @param imageType Image identifier (Binary or Grey-image)
     * @return Returns the image as a base64 string
     */
    public String getLineImage(String pageID, String segmentID, String lineID, String imageType)
            throws IOException {
        String base64Image = null;
        File f = null;
        if (imageType.equals("Gray"))
            f = new File(projDirConf.PAGE_DIR + pageID + File.separator + segmentID
                    + File.separator + lineID + projDirConf.GRAY_IMG_EXT);
        if (imageType.equals("Binary"))
            f = new File(projDirConf.PAGE_DIR + pageID + File.separator + segmentID
                    + File.separator + lineID + projDirConf.BIN_IMG_EXT);

        if (f.exists())
            base64Image = encodeFileToBase64Binary(f);
        return base64Image;
    }

    /**
     * Gets all pages of the project and the images of the given type encoded in base64
     *
     * @param imageType Type of the images in the list
     * @return Map of page IDs with their images as base64 string
     * @throws IOException
     */
    public TreeMap<String, String> getImageList(String imageType) throws IOException {
        TreeMap<String, String> imageList = new TreeMap<String, String>();

        String imagePath = null;
        switch(imageType) {
            case "Original":   imagePath = projDirConf.ORIG_IMG_DIR; break;
            case "Binary":     imagePath = projDirConf.BINR_IMG_DIR; break;
            case "Gray":       imagePath = projDirConf.GRAY_IMG_EXT; break;
            case "Despeckled": imagePath = projDirConf.DESP_IMG_DIR; break;
            default: break;
        }

        final File folder = new File(imagePath);
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile())
                imageList.put(FilenameUtils.removeExtension(fileEntry.getName()), encodeFileToBase64Binary(fileEntry));
        }
        return imageList;
    }
}
