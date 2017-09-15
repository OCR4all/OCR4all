package de.uniwue.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
/** Helper class for image based functionality
*/
public class ImageHelper {

    public ImageHelper() {
    }
    /** Encodes the given file to base64 String
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
    /** Gets the specified page image and encodes it to base64 
     * @param projectDir Path to the project directory
     * @param pageID Identifier of the page (e.g 0002)
     * @param imageID Image identifier (Original, Gray or Despeckled)
     * @return Returns the image as a base64 string
    */
    public String getPageImage(String projectDir, String pageID, String imageID) throws IOException {
        String base64Image = null;
        File f = null;
        if (imageID.equals("Original")) {
            f = new File(projectDir + File.separator + "Original" + File.separator + pageID + ".png");
        } else {
            if (imageID.equals("Gray")) {
                f = new File(projectDir + File.separator + "PreProc" + File.separator + "Gray" + File.separator + pageID
                        + ".png");
            }
            if (imageID.equals("Binary")) {
                f = new File(projectDir + File.separator + "PreProc" + File.separator + "Binary" + File.separator
                        + pageID + ".png");
            }
            if (imageID.equals("Despeckled")) {
                f = new File(projectDir + File.separator + "PreProc" + File.separator + "Despeckled" + File.separator
                        + pageID + ".png");
            }
        }
        if (f.exists())
            base64Image = encodeFileToBase64Binary(f);

        return base64Image;
    }
    /** Gets the specified page segment image and encodes it to base64
     * @param projectDir Path to the project directory
     * @param pageID Identifier of the page (e.g 0002)
     * @param segmentID Identifier of the segment (e.g 0002__000__paragraph)
     * @param imageType Image identifier (Binary or Grey-image)
     * @return Returns the image as a base64 string
    */
    public String getSegmentImage(String projectDir, String pageID, String segmentID, String imageType)
            throws IOException {
        String base64Image = null;
        File f = null;
        if (imageType.equals("Gray"))
            f = new File(projectDir + File.separator + "OCR" + File.separator + "Pages" + File.separator + pageID
                    + File.separator + segmentID + ".nrm.png");
        if (imageType.equals("Binary"))
            f = new File(projectDir + File.separator + "OCR" + File.separator + "Pages" + File.separator + pageID
                    + File.separator + segmentID + ".bin.png");
        if (f.exists())
            base64Image = encodeFileToBase64Binary(f);

        return base64Image;
    }
    /** Gets the specified page line image of a segment and encodes it to base64
     * @param projectDir Path to the project directory
     * @param pageID Identifier of the page (e.g 0002)
     * @param segmentID Identifier of the segment (e.g 0002__000__paragraph)
     * @param lineID Identifier of the line (e.g 0002__000__paragraph__000)
     * @param imageType Image identifier (Binary or Grey-image)
     * @return Returns the image as a base64 string
    */
    public String getLineImage(String projectDir, String pageID, String segmentID, String lineID, String imageType)
            throws IOException {
        String base64Image = null;
        File f = null;
        if (imageType.equals("Gray"))
            f = new File(projectDir + File.separator + "OCR" + File.separator + "Pages" + File.separator + pageID
                    + File.separator + segmentID + File.separator + lineID + ".nrm.png");
        if (imageType.equals("Binary"))
            f = new File(projectDir + File.separator + "OCR" + File.separator + "Pages" + File.separator + pageID
                    + File.separator + segmentID + File.separator + lineID + ".bin.png");
        if (f.exists())
            base64Image = encodeFileToBase64Binary(f);
        return base64Image;
    }

}
