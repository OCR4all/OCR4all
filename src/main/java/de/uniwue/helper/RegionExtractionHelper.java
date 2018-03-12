package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.RegionExtractor;

/**
 * Helper class for region extraction module
 */
public class RegionExtractionHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Status of the progress
     */
    private int progress = -1;

    /**
     * Indicates if a region extraction process is already running
     */
    private boolean regionExtractionRunning = false;

    /**
     * Indicates if the process should be cancelled
     */
    private boolean stop = false;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public RegionExtractionHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
    }

    /**
     * TODO: commenting
     *
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @param spacing
     * @param useSpacing
     * @param useAvgBgd
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void executeRegionExtraction(List<String> pageIds, int spacing, boolean useSpacing, boolean useAvgBgd)
            throws ParserConfigurationException, SAXException, IOException {
        regionExtractionRunning = true;
        stop = false;

        File pageDir = new File(projConf.PAGE_DIR);
        if (!pageDir.exists())
            pageDir.mkdir();

        progress = 0;

        double i = 1;
        int totalPages = pageIds.size();
        for (String pageId : pageIds) {
            if (stop == true) 
                break;

            String imagePath = projConf.OCR_DIR + pageId + projConf.IMG_EXT;
            String xmlPath = projConf.OCR_DIR + pageId + ".xml";
            String outputFolder = projConf.PAGE_DIR;
            RegionExtractor.extractSegments(xmlPath, imagePath, useAvgBgd, useSpacing, spacing, outputFolder);

            progress = (int) ((double) i / totalPages * 100);
            i = i + 1;
        }

        progress = 100;
        regionExtractionRunning = false;
    }

    /**
     * Returns the progress of the job
     *
     * @return Progress of preprocessAllPages function
     */
    public int getProgress() {
        if (stop == true)
            return -1;
        return progress;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        regionExtractionRunning = false;
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        stop = true;
    }

    /**
     * Gets the region extraction status
     *
     * @return status if the process is running
     */
    public boolean isRegionExtractionRunning() {
        return regionExtractionRunning;
    }

    /**
     * Returns the ids of the pages, where the segmentation step is already done
     *
     * @return List with page ids
     */
    public ArrayList<String> getIdsforRegionExtraction() {
        ArrayList<String> IdsForImageList = new ArrayList<String>();

        File ocrDir = new File(projConf.OCR_DIR);
        if (!ocrDir.exists()) {
            return IdsForImageList;}

        File[] XMLfiles = ocrDir.listFiles((d, name) -> name.endsWith(".xml"));
        for(File file: XMLfiles) { 
            IdsForImageList.add(FilenameUtils.removeExtension(file.getName()));
        }
        Collections.sort(IdsForImageList);

        return IdsForImageList;
    }
}
