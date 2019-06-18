package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;

/**
 * Helper class for segmentating pages, which also calls the pixel-classifier program 
 */
public class SegmentationPixelClassifierHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Image Type of the Project
     */
    private String projectImageType;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Segmentation process
     */
    private int progress = -1;

    /**
     * Indicates if a Segmentation process is already running
     */
    private boolean SegmentationRunning = false;

    /**
     * Structure to monitor the progress of the process
     * pageId : SegmentationState
     *
     * Structure example:
     * {
     *     "0002": true,
     *     "0003 : false,
     *     ...
     * }
     */
    private TreeMap<String, Boolean> processState = new TreeMap<String, Boolean>();

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (binary, gray)
     * @param processingMode Processing structure of the project (Directory, Pagexml)
     */
    public SegmentationPixelClassifierHelper(String projectDir, String projectImageType, String processingMode) {
        projConf = new ProjectConfiguration(projectDir);
        procStateCol = new ProcessStateCollector(projConf, projectImageType, processingMode);
        processHandler = new ProcessHandler();
        this.projectImageType = projectImageType;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        // Prevent function from calculation progress if process is not running
        if (SegmentationRunning == false)
            return progress;

        int imageCount = 0;
        int processedImageCount = 0;
        // Identify how many segments are already processed
        for (String pageId : processState.keySet()) {
            imageCount += 1;
            if (processState.get(pageId) == true) {
                processedImageCount += 1;
                continue;
            }
            File XMLFile = new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
            File ImageFile = new File(projConf.OCR_DIR + pageId + projConf.IMG_EXT);
            if (XMLFile.exists() && ImageFile.exists()) {
                processState.put(pageId, true);
            }
        }
        return (progress != 100) ? (int) ((double) processedImageCount / imageCount * 100) : 100;
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
     * Initializes the structure with which the progress of the process can be monitored
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void initializeProcessState(List<String> pageIds) throws IOException {
        // Initialize the status structure
        processState = new TreeMap<String, Boolean>();
        for(String pageId : pageIds) {
            processState.put(pageId, false);
        }
    }

    /**
     * Executes image segmentation of all pages
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "TODO"
     * @throws IOException
     */
    public void execute(List<String> pageIds, List<String> cmdArgs, String segmentationImageType) throws IOException {
        SegmentationRunning = true;

        progress = 0;

        initializeProcessState(pageIds);
        // TODO : Run pixel classifier script with passed arguments
        List<String> command = new ArrayList<String>();
        for (String pageId : pageIds) {
            // Add affected pages with their absolute path to the command list
            command.add(projConf.getImageDirectoryByType(segmentationImageType) + pageId + projConf.IMG_EXT);
        }
        command.addAll(cmdArgs);

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("TODO", command, false);

        getProgress();
        SegmentationRunning = false;
        progress = 100;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        SegmentationRunning = false;
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
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses, boolean inProcessFlow) {
        return ProcessConflictDetector.segmentationPixelClassifierConflict(currentProcesses, inProcessFlow );
    }
}
