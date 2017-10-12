package de.uniwue.helper;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;

import org.apache.commons.io.output.ByteArrayOutputStream;

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
     * Resizing image to this dimension
     */
    private Dimension dimension = null;

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
     * @throws IM4JavaException 
     * @throws InterruptedException 
     * @throws MagickException 
     */
    public String getPageImage(String pageID, String imageID) throws IOException, InterruptedException {

        if (imageID.equals("Original")) {
            return scaldedBase64Image(projDirConf.ORIG_IMG_DIR + pageID + projDirConf.IMG_EXT);
        }
        else {
            if (imageID.equals("Gray")) {
                return scaldedBase64Image(projDirConf.GRAY_IMG_DIR + File.separator + pageID + projDirConf.IMG_EXT);
            }
            else if (imageID.equals("Despeckled")) {
                return scaldedBase64Image(projDirConf.DESP_IMG_DIR + File.separator + pageID + projDirConf.IMG_EXT);
            }
            else {
                return scaldedBase64Image(projDirConf.BINR_IMG_DIR + File.separator + pageID + projDirConf.IMG_EXT);
            }
        }
    }
    /**
     * Sets the dimension
     * @param dimension
     */
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
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

        if (imageType.equals("Gray")) {
            return scaldedBase64Image(projDirConf.PAGE_DIR + pageID + File.separator + segmentID + projDirConf.GRAY_IMG_EXT);
        }
        else {
            return scaldedBase64Image(projDirConf.PAGE_DIR + pageID + File.separator + segmentID + projDirConf.BIN_IMG_EXT);
        }
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
        if (imageType.equals("Gray"))
            return scaldedBase64Image(projDirConf.PAGE_DIR + pageID + File.separator + segmentID
                    + File.separator + lineID + projDirConf.GRAY_IMG_EXT);
        if (imageType.equals("Binary"))
            return scaldedBase64Image(projDirConf.PAGE_DIR + pageID + File.separator + segmentID
                    + File.separator + lineID + projDirConf.BIN_IMG_EXT);
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
    /**
     * Downscales given image and encodes it to base64
     * @param path path to the image
     * @return Base 64 String of the resized Image
     * @throws IOException
     */
    public String scaldedBase64Image(String path) throws IOException {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
        }
        if (dimension != null) {
            img = scaleImage(img,dimension);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        
        byte[] b= baos.toByteArray();
        String resultBase64Encoded = Base64.getEncoder().encodeToString(b);
        return resultBase64Encoded;
    }

    /**
     * Downscales images
     * Fastest way to scale pictures is with the Nearest Neighbor algorithm
     * Source code from: http://www.locked.de/2009/06/08/fast-image-scaling-in-java/ 
     * @param img Bufferd image
     * @param d Dimension of the downsized image
     * @return Downsized image
     */
    public BufferedImage scaleImage(BufferedImage img, Dimension d) {
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

    float getBinFactor(int width, int height, Dimension dim) {
        float factor = 1;
        float target = getFactor(width, height, dim);
        if (target <= 1) { while (factor / 2 > target) { factor /= 2; }
        } else { while (factor * 2 < target) { factor *= 2; }         }
        return factor;
    }

    float getFactor(int width, int height, Dimension dim) {
        float sx = dim.width / (float) width;
        float sy = dim.height / (float) height;
        return Math.min(sx, sy);
    }
}
