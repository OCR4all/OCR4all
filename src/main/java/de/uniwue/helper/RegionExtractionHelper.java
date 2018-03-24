package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;
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
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

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
    public RegionExtractionHelper(String projectDir, String projectImageType) {
        projConf = new ProjectConfiguration(projectDir);
        procStateCol = new ProcessStateCollector(projConf, projectImageType);
        processHandler = new ProcessHandler();
    }

    /**
     * Gets the process handler object
     *
     * @return Returns the process Helper
     */
    public ProcessHandler getProcessHandler() {
        return processHandler;
    }

    /**
     * Executes region extraction on a list of pages
     *
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @param spacing Spacing setting for region extractor
     * @param useAvgBgd UseAvgBgd setting for region extractor
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void executeRegionExtraction(List<String> pageIds, int spacing, boolean useAvgBgd, int parallel)
            throws ParserConfigurationException, SAXException, IOException {
        regionExtractionRunning = true;
        stop = false;
        progress = 0;

        File pageDir = new File(projConf.PAGE_DIR);
        if (!pageDir.exists())
            pageDir.mkdir();

        deleteOldFiles(pageIds);

        int pageCount = pageIds.size();
        int regionExtractedPageCount = 0;
        processHandler = new ProcessHandler();
        for (String pageId : pageIds) {
            if (stop == true) 
                break;

            String imagePath = projConf.OCR_DIR + pageId + projConf.IMG_EXT;
            String xmlPath = projConf.OCR_DIR + pageId + projConf.CONF_EXT;
            String outputFolder = projConf.PAGE_DIR;
            List<String> regions = RegionExtractor.extractSegments(xmlPath, imagePath, useAvgBgd, spacing, outputFolder);
            List<String> command = new ArrayList<String>();
            for(String pathToRegion :regions)
                command.add(pathToRegion);
            command.add("--parallel");
            command.add(Integer.toString(parallel));
            command.add("-n");

            processHandler.setFetchProcessConsole(true);
            processHandler.startProcess("ocropus-nlbin", command, false);
            regionExtractedPageCount++;
            progress = (int) ((double) regionExtractedPageCount / pageCount * 100);
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
     * Returns the Ids of the pages, for which region extraction was already executed
     *
     * @return List of valid page Ids
     */
    public ArrayList<String> getValidPageIdsforRegionExtraction() {
        ArrayList<String> validPageIds = new ArrayList<String>();

        File ocrDir = new File(projConf.OCR_DIR);
        if (!ocrDir.exists()) {
            return validPageIds;}

        File[] XMLfiles = ocrDir.listFiles((d, name) -> name.endsWith(projConf.CONF_EXT));
        for(File file: XMLfiles) { 
            validPageIds.add(FilenameUtils.removeExtension(file.getName()));
        }
        Collections.sort(validPageIds);

        return validPageIds;
    }

    /**
     * Deletion of old process related files
     *
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @throws IOException 
     */
    public void deleteOldFiles(List<String> pageIds) throws IOException {
        // Delete folder of the given pages
        // This deletes the data of subsequent processes as well to preserve data integrity
        for (String pageId : pageIds) {
            File page = new File(projConf.PAGE_DIR + pageId);
            if (page.exists())
                FileUtils.deleteDirectory(page);
        }
    }

    /**
     * Checks if process depending files already exist
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @return Information if files exist
     */
    public boolean doOldFilesExist(String[] pageIds) {
        for (String pageId : pageIds) {
            if (procStateCol.regionExtractionState(pageId))
                return true;
        }
        return false;
    }
}
