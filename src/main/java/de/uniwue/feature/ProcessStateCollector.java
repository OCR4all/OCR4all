package de.uniwue.feature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

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
     * Processing structure of the project
     * Possible values: { Directory, Pagexml }
     */
    private String processingMode;

    /**
     * Constructor
     *
     * @param projConf Project configuration object
     * @param projectImageType Type of the project (gray, binary)
     * @param imageType Image type of the project
     */
    public ProcessStateCollector(ProjectConfiguration projConf, String imageType, String processingMode) {
        this.projConf = projConf;
        this.imageType = imageType;
        this.processingMode = processingMode;
    }

    /**
     * Determines the "Preprocessing" process state of a given page
     *
     * @param pageID Identifier of the page (e.g 0002,0003)
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
     * @param pageID Identifier of the page (e.g 0002,0003)
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
     * @param pageID Identifier of the page (e.g 0002,0003)
     * @return "Segmentation" state of the page
     */
    public boolean segmentationState(String pageId) {
        if (!new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT).exists()) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether the required image files in "OCR/Pages/{pageId}" directory exist or not
     *
     * @param pageID Identifier of the page (e.g 0002,0003)
     * @param includePseg Determines if the ".pseg.png" file should be checked as well (for line segmentation only)
     * @return Information if the required image files exist
     */
    public boolean existPageDirImageFiles(String pageId, boolean includePseg) {
        File pageDir = new File(projConf.PAGE_DIR + pageId);
        if (!pageDir.exists())
            return false;

        // Identify plain region images (REGION_ID.png)
        File[] plainRegionImgFiles = pageDir.listFiles((d, name) ->
            name.endsWith(projConf.IMG_EXT) &&
            !name.endsWith(projConf.BINR_IMG_EXT) &&
            !name.endsWith(projConf.GRAY_IMG_EXT) &&
            !name.endsWith(projConf.PSEG_EXT)
        );

        if (plainRegionImgFiles.length == 0)
            return false;

        // Check that for every region file a ".bin.png" and ".nrm.png exists (successful region extraction)
        // If required, check that for every region file a ".pseg.png" exists (successful line segmentation)
        for (File imgFile : plainRegionImgFiles) {
            if (!new File(FilenameUtils.removeExtension(imgFile.getAbsolutePath()) + projConf.BINR_IMG_EXT).exists())
                return false;

            if (!new File(FilenameUtils.removeExtension(imgFile.getAbsolutePath()) + projConf.GRAY_IMG_EXT).exists())
                return false;

            if (includePseg == true) {
                if (!new File(FilenameUtils.removeExtension(imgFile.getAbsolutePath()) + projConf.PSEG_EXT).exists())
                    return false;
            }
        }

        return true;
    }

    /**
     * Checks whether the required textlines for the files in "OCR/Pages/{pageId}" exist or not
     *
     * @param pageID Identifier of the page (e.g 0002,0003)
     * @return Information if the required image files exist
     */
    public boolean existLines(String pageId) {
        File pageXML = new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
        if (!pageXML.exists())
            return false;

        // Easy and fast check if an end tag of a TextLine exists 
        // Does not check if the xml is valid
        try {
			FileInputStream fis = new FileInputStream(pageXML);
			byte[] data = new byte[(int) pageXML.length()];
			fis.read(data);
			fis.close();
			String pageXMLContent = new String(data, "UTF-8");
			
			Pattern p = Pattern.compile("\\</TextLine\\>");
			Matcher matcher = p.matcher(pageXMLContent);

			return matcher.find();
		} catch (UnsupportedEncodingException e) {
			return false;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
    }
    
    /**
     * Determines the "RegionExtraction" process state of a given page
     *
     * @param pageID Identifier of the page (e.g 0002,0003)
     * @return "RegionExtraction" state of the page
     */
    public boolean regionExtractionState(String pageId) {
        // Check that for every region file a ".bin.png" and ".nrm.png exists (successful region extraction)
        return existPageDirImageFiles(pageId, false);
    }

    /**
     * Determines the "LineSegmentation" process state of a given page
     *
     * @param pageID Identifier of the page (e.g 0002,0003)
     * @return "LineSegmentation" state of the page
     */
    public boolean lineSegmentationState(String pageId) {
    	if(processingMode.equals("Directory")) {
			// Check that for every region file a ".bin.png" and ".nrm.png exists (successful region extraction)
			// Check that for every region file a ".pseg.png" exists (successful line segmentation)
			return existPageDirImageFiles(pageId, true);
    	} else {
    		// Check whether lines in the PageXML files exist
    		return existLines(pageId);
    	}
    }

    /**
     * Determines the "Recognition" process state of a given page
     *
     * @param pageID Identifier of the page (e.g 0002,0003)
     * @return "Recognition" state of the page
     */
    public boolean recognitionState(String pageId) {
        File pageDir = new File(projConf.PAGE_DIR + pageId);
        if (!pageDir.exists())
            return false;

        File[] lineSegmentDirectories = pageDir.listFiles(File::isDirectory);
        if (lineSegmentDirectories.length == 0) {
            return false;
        }

        for (File dir : lineSegmentDirectories) {
            File[] lineSegmentImgFiles = dir.listFiles((d, name) -> name.endsWith(projConf.getImageExtensionByType(imageType)));
            // Check that for every line segmenation file a ".txt" exists (successful recognition)
            for (File imgFile : lineSegmentImgFiles) {
                // Call removeExtension twice, due to "Binary"|"Gray" image extensions (".bin.png"|".nrm.png")
                if (!new File(FilenameUtils.removeExtension(FilenameUtils.removeExtension(imgFile.getAbsolutePath()))
                         + projConf.REC_EXT).exists()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Determines the "ResultGeneration" process state of a given page
     *
     * @param pageID Identifier of the page (e.g 0002,0003)
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
