package de.uniwue.helper;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SegmentationKrakenHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Image type of the project
     * Possible values: { Binary, Gray }
     */
    private String projectImageType;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Status of the SegmentationLarex progress
     */
    private int progress = -1;

    /**
     * Indicates if the process should be cancelled
     */
    private boolean stop = false;

    /**
     * Indicates if a Training process is already running
     */
    private boolean segmentationRunning = false;

    /**
     * Constructor
     *
     * @param projDir Path to the project directory
     * @param projectImageType Type of the project (binary,gray)
     */
    public SegmentationKrakenHelper(String projDir, String projectImageType) {
        this.projectImageType = projectImageType;
        processHandler = new ProcessHandler();
        projConf = new ProjectConfiguration(projDir);
        procStateCol = new ProcessStateCollector(projConf, projectImageType);
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        if (!segmentationRunning)
            return progress;
        // TODO
        return 0;
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
     * Moves the extracted files of the segmentation process to the OCR project folder
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param segmentationImageType Image type of the segmentation (binary, grayscale)
     * @throws IOException
     * @throws TransformerException 
     * @throws ParserConfigurationException 
     */
    public void execute(List<String> pageIds, String segmentationImageType) throws IOException, ParserConfigurationException, TransformerException {
        segmentationRunning = true;
        progress = 0;

        File ocrDir = new File(projConf.OCR_DIR);
        if (!ocrDir.exists())
            ocrDir.mkdir();

        SegmentationHelper segmentationHelper = new SegmentationHelper(projConf.PROJECT_DIR, this.projectImageType);
        segmentationHelper.deleteOldFiles(pageIds);

        String projectSpecificPreprocExtension = projConf.getImageExtensionByType(projectImageType);

        int processedPages = 0;
        // generates XML files for each page
        File segmentationTypeDir = new File(projConf.getImageDirectoryByType(segmentationImageType));
        if (segmentationTypeDir.exists()){
            List<String> command = new ArrayList<String>();
            command.add("kraken");

            File[] imageFiles = segmentationTypeDir.listFiles((d, name) -> name.endsWith(projectSpecificPreprocExtension));
            for (File file : imageFiles) {
                if (pageIds.contains(file.getName().replace(projectSpecificPreprocExtension, "")) && !stop) {
                    command.add(file.getPath());
                }
            }

            processHandler = new ProcessHandler();
            processHandler.setFetchProcessConsole(true);
            processHandler.startProcess("ocr4all-helper-scripts", command, false);
        }
        segmentationRunning = false;
        progress = 100;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        segmentationRunning = false;
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        if (processHandler != null)
            processHandler.stopProcess();
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses, boolean inProcessFlow) {
        return ProcessConflictDetector.segmentationDummyConflict(currentProcesses, inProcessFlow);
    }

    /**
     * Checks if process depending files already exist
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @return Information if files exist
     */
    public boolean doOldFilesExist(String[] pageIds){
        for(String pageId : pageIds) {
            if (procStateCol.segmentationState(pageId))
                return true;
        }
        return false;
    }
}
