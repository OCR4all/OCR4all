package de.uniwue.helper;

import java.util.List;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.RegionExtractor;

public class RegionExtractorHelper {

    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Indicates if a regionExtraction process is already running
     */
    private boolean regionExtraction;

    /**
     * Status of the progress
     */
    private int progress = -1;

    /**
     * Indicates if the process should be cancelled
     */
    private boolean stop = false;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public RegionExtractorHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
    }

    public void executeRegionExtraction(List<String> pageIds, int spacing, boolean useSpacing, boolean useAvgBgd ) {
        regionExtraction = true;
        double i = 1;
        int totalPages = pageIds.size();
        progress = 0;
        for(String pageId : pageIds) {
            if (stop == true) 
                break;
            String imagePath = projConf.OCR_DIR + pageId + projConf.IMG_EXT;
            String xmlPath = projConf.OCR_DIR + pageId + ".xml";
            String outputFolder = projConf.PAGE_DIR;
            RegionExtractor.extractSegments(xmlPath, imagePath, useAvgBgd, useSpacing, spacing, outputFolder);

            progress = (int) (i / totalPages * 100);
            i = i + 1;
            }
        progress = 100;
        regionExtraction = false;
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
     * Cancels the process
     */
    public void cancelProcess() {
        stop = true;
    }
}
