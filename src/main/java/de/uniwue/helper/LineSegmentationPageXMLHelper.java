package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;

/**
 * Helper class for line segmenting pages, which also calls the pagexmllineseg program
 */
public class LineSegmentationPageXMLHelper  implements LineSegmentationHelper{
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Image type of the project
     * Possible values: { Binary, Gray }
     */
    private String projectImageType;

    /**
     * Object to use generic functionalities
     */
    private GenericHelper genericHelper;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Line Segmentation process
     */
    private int progress = -1;

    /**
     * Indicates if a Line Segmentation process is already running
     */
    private boolean lineSegmentationRunning = false;

    /**
     * Structure to monitor the progress of the process
     * pageId : segmentId : processedState
     *
     * Structure example:
     * {
     *     "0002": {
     *         "0002__000__paragraph" : true,
     *         "0002__001__heading" : false,
     *         ...
     *     },
     *     ...
     * }
     */
    private TreeMap<String, TreeMap<String, Boolean>> processState = new TreeMap<String, TreeMap<String, Boolean>>();

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (gray, binary)
     * @param processingMode Processing structure of the project (Directory, Pagexml)
     */
    public LineSegmentationPageXMLHelper(String projectDir, String projectImageType, String processingMode) {
        this.projectImageType = projectImageType;
        projConf = new ProjectConfiguration(projectDir);
        genericHelper = new GenericHelper(projConf);
        procStateCol = new ProcessStateCollector(projConf, projectImageType, processingMode);
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
     * Initializes the structure with which the progress of the process can be monitored
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void initializeProcessState(List<String> pageIds) throws IOException {
        // Initialize the status structure
        processState = new TreeMap<String, TreeMap<String, Boolean>>();

        for(String pageId : pageIds) {
            if(!new File(projConf.PAGE_DIR + pageId).exists())
                continue;

            TreeMap<String, Boolean> segments = new TreeMap<String, Boolean>();
            Files.walk(Paths.get(projConf.PAGE_DIR + pageId), 1)
            .map(Path::toFile)
            .filter(fileEntry -> fileEntry.isFile())
            .filter(fileEntry -> fileEntry.getName().endsWith(projConf.getImageExtensionByType(projectImageType)))
            .forEach(
                fileEntry -> { 
                    segments.put(FilenameUtils.removeExtension(FilenameUtils.removeExtension(fileEntry.getName())), false);
                }
            );

            processState.put(pageId, segments);
        }
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     * @throws IOException 
     */
    public int getProgress() throws IOException {
        return getProgress();
    }


    /**
     * Executes line segmentation of all pages
     * Achieved with the help of the external python program "pagelineseg"
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "pagelineseg"
     * @throws IOException
     */
    public void execute(List<String> pageIds, List<String> cmdArgs) throws IOException {
        lineSegmentationRunning = true;

        progress = 0;

        // Reset line segment data
        initializeProcessState(pageIds);

        int processedPages = 0;
        for (String pageId : pageIds) {
			List<String> command = new ArrayList<String>();
            command.add(projConf.getImageDirectoryByType(projectImageType) + pageId + projConf.getImageExtensionByType(projectImageType));
            command.add(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
			command.addAll(cmdArgs);
			processHandler = new ProcessHandler();
			processHandler.setFetchProcessConsole(true);
			System.out.println("pagelineseg "+String.join(" ",command));
			processHandler.startProcess("pagelineseg", command, false);

			progress = (int) ((double) processedPages++ / pageIds.size() * 100);
        }

        // Execute progress update to fill processState data structure with correct values
        getProgress();

        progress = 100;
        lineSegmentationRunning = false;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        lineSegmentationRunning = false;
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        if (processHandler != null)
            processHandler.stopProcess();
        lineSegmentationRunning = false;
    }

    /**
     * Returns the Ids of the pages, for which region extraction was already executed
     *
     * @return List of valid page Ids
     * @throws IOException 
     */
    public ArrayList<String> getValidPageIds() throws IOException {
        // Get all pages and check which ones are already region extracted
        ArrayList<String> validPageIds = new ArrayList<String>();
        ArrayList<String> allPageIds = genericHelper.getPageList("Original");
        for (String pageId : allPageIds) {
            if (procStateCol.segmentationState(pageId) == true)
                validPageIds.add(pageId);
        }

        Collections.sort(validPageIds);
        return validPageIds;
    }

    /**
     * Checks if process depending files already exist
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @return Information if files exist
     */
    public boolean doOldFilesExist(String[] pageIds){
        for(String pageId : pageIds) {
            if (procStateCol.lineSegmentationState(pageId) == true)
                return true;
        }
        return false;
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses, boolean inProcessFlow) {
        return ProcessConflictDetector.lineSegmentationConflict(currentProcesses, inProcessFlow);
    }

}
