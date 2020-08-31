package de.uniwue.feature;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uniwue.config.ProjectConfiguration;

/**
 * Class to collect the current state of processes
 */
public class ProcessStateCollector {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Image type of the project
     * Possible values: { Binary, Gray }
     */
    private String imageType;

    /**
     * Constructor
     *
     * @param projConf Project configuration object
     * @param imageType Image type of the project
     */
    public ProcessStateCollector(ProjectConfiguration projConf, String imageType) {
        this.projConf = projConf;
        this.imageType = imageType;
    }

    /**
     * Determines the "Preprocessing" process state of a given page
     *
     * @param pageId Identifier of the page (e.g 0002,0003)
     * @return "Preprocessing" state of the page
     */
    public boolean preprocessingState(String pageId) {
        if (!new File(projConf.getImageDirectoryByType(imageType) + pageId + projConf.getImageExtensionByType(imageType)).exists())
            return false;
        return true;
    }


    /**
     * Determines the "Despeckling" process state of a given page
     *
     * @param pageId Identifier of the page (e.g 0002,0003)
     * @return "Despeckling" state of the page
     */
    public boolean despecklingState(String pageId) {
        if (!new File(projConf.DESP_IMG_DIR  + pageId + projConf.DESP_IMG_EXT).exists())
            return false;
        return true;
    }

    /**
     * Determines the "Segmentation" process state of a given page
     *
     * @param pageId Identifier of the page (e.g 0002,0003)
     * @return "Segmentation" state of the page
     */
    public boolean segmentationState(String pageId) {
        if (!new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT).exists()) {
            return false;
        }
        return true;
    }


    /**
     * Checks whether the required textlines for the image files exist or not.
     * Also checks for binary/grayscale images to extract the lines from. 
     *
     * @param pageId Identifier of the page (e.g 0002,0003)
     * @return Information if the required image files exist
     */
    public boolean existLines(String pageId) {
        File pageXML = new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
        if (!pageXML.exists())
            return false;

        // Check if the images of the lines exist
		String imageFile = projConf.getImageDirectoryByType(imageType) + pageId + projConf.getImageExtensionByType(imageType);
		if (!new File(imageFile).exists()) {
			return false;
		}

        // Easy and fast check if an end tag of a TextLine exists 
        // Does not check if the xml is valid
        try (FileInputStream fis = new FileInputStream(pageXML)) {
			byte[] data = new byte[(int) pageXML.length()];
			fis.read(data);
			fis.close();
			String pageXMLContent = new String(data, "UTF-8");
			
			Pattern p = Pattern.compile("\\</(.+:)?TextLine(.+)?\\>");
			Matcher matcher = p.matcher(pageXMLContent);

			return matcher.find();
		} catch (IOException e) {
			return false;
		}
    }

    /**
     * Determines the "LineSegmentation" process state of a given page
     *
     * @param pageId Identifier of the page (e.g 0002,0003)
     * @return "LineSegmentation" state of the page
     */
    public boolean lineSegmentationState(String pageId) {
        // Check whether lines in the PageXML files exist
        return existLines(pageId);
    }

    /**
     * Determines the "Recognition" process state of a given page
     *
     * @param pageId Identifier of the page (e.g 0002,0003)
     * @return "Recognition" state of the page
     */
    public boolean recognitionState(String pageId) {
        // Check pagexml for TextEquivs
        File pageXML = new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
        if (!pageXML.exists())
            return false;

        // Easy and fast check if an end tag of a TextLine exists 
        // Does not check if the xml is valid
        try (FileInputStream fis = new FileInputStream(pageXML)){
            byte[] data = new byte[(int) pageXML.length()];
            fis.read(data);
            fis.close();
            String pageXMLContent = new String(data, "UTF-8");
            // Test for TextEquiv with index higher 0 (GT)
            Pattern p = Pattern.compile("\\<(.+:)?TextEquiv[^>]+?index=\"[^0]\"[^>]*?(.+)?\\>");
            Matcher matcher = p.matcher(pageXMLContent);

            return matcher.find();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Determines the "GroundTruth" process state of a given page
     * 
     * @param pageId Identifier of the page (e.g 0002,0003)
     * @return "result" state of the page
     */
    public boolean groundTruthState(String pageId) {
        // Check pagexml for TextEquivs
        File pageXML = new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
        if (!pageXML.exists())
            return false;

        // Easy and fast check if an end tag of a TextLine exists 
        // Does not check if the xml is valid
        try (FileInputStream fis = new FileInputStream(pageXML)){
            byte[] data = new byte[(int) pageXML.length()];
            fis.read(data);
            fis.close();
            String pageXMLContent = new String(data, "UTF-8");
            
            Pattern p = Pattern.compile("\\<(.+:)?TextEquiv[^>]+?index=\"0\"(.+)?\\>");
            Matcher matcher = p.matcher(pageXMLContent);

            if(matcher.find()) {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Determines the "ResultGeneration" process state of a given page
     *
     * @param pageId Identifier of the page (e.g 0002,0003)
     * @param resultType Type of the result, which should be checked (xml, txt) 
     * @return "result" state of the page
     */
    public boolean resultGenerationState(String pageId, String resultType) {
        File pageResult;
        if(resultType.equals("xml")) 
            pageResult = new File(projConf.RESULT_PAGES_DIR + pageId + projConf.CONF_EXT);
        else
            pageResult = new File(projConf.RESULT_PAGES_DIR + pageId + projConf.REC_EXT);
        return pageResult.exists();
    }
}
