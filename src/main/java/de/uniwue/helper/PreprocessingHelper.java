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
 * Helper class for preprocessing pages, which also calls the ocrubus-nlbin program 
 */
public class PreprocessingHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Image Type of the Project
     */
    private String projectImageType;

    /**
     * Processing structure of the project
     * Possible values: { Directory, Pagexml }
     */
    private String processingMode;
    
    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Preprocessing process
     */
    private int progress = -1;

    /**
     * Indicates if a Preprocessing process is already running
     */
    private boolean preprocessingRunning = false;

    /**
     * Structure to monitor the progress of the process
     * pageId : processedState
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
    public PreprocessingHelper(String projectDir, String projectImageType, String processingMode) {
        projConf = new ProjectConfiguration(projectDir);
        procStateCol = new ProcessStateCollector(projConf, projectImageType, processingMode);
        processHandler = new ProcessHandler();
        this.projectImageType = projectImageType;
        this.processingMode = processingMode;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        // Prevent function from calculation progress if process is not running
        if (preprocessingRunning == false)
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
            File binImage = new File(projConf.BINR_IMG_DIR + pageId + projConf.BINR_IMG_EXT);
            File grayImage = new File(projConf.GRAY_IMG_DIR + pageId + projConf.GRAY_IMG_EXT);
            if (binImage.exists() && grayImage.exists()) {
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
     * Create necessary Preprocessing directories if they do not exist
     */
    private void initializePreprocessingDirectories() {
        File preprocDir = new File(projConf.PREPROC_DIR);
        if (!preprocDir.exists())
            preprocDir.mkdir();

        File binDir = new File(projConf.BINR_IMG_DIR);
        if (!binDir.exists())
            binDir.mkdir();

        File grayDir = new File(projConf.GRAY_IMG_DIR);
        if (!grayDir.exists())
            grayDir.mkdir();
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
     * Executes image Preprocessing of all pages
     * Achieved with the help of the external python program "ocropus-nlbin"
     * This function also creates the preprocessed directory structure
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "ocropus-nlbin"
     * @throws IOException
     */
    public void execute(List<String> pageIds, List<String> cmdArgs) throws IOException {
        preprocessingRunning = true;

        progress = 0;

        File origDir = new File(projConf.ORIG_IMG_DIR);
        if (!origDir.exists())
            return;

        initializePreprocessingDirectories();
        deleteOldFiles(pageIds);
        initializeProcessState(pageIds);

        List<String> command = new ArrayList<String>();
        for (String pageId : pageIds) {
            // Add affected pages with their absolute path to the command list
            command.add(projConf.ORIG_IMG_DIR + pageId + projConf.IMG_EXT);
        }
        command.add("-o");
        command.add(projConf.PREPROC_DIR);
        command.addAll(cmdArgs);

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocropus-nlbin", command, false);

        getProgress();
        preprocessingRunning = false;
        progress = 100;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        preprocessingRunning = false;
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
     * Deletion of old process related files
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException 
     */
    public void deleteOldFiles(List<String> pageIds) throws IOException {
        for (String pageId : pageIds) {
            File binImg = new File(projConf.BINR_IMG_DIR + pageId + projConf.BINR_IMG_EXT);
            if(binImg.exists())
                binImg.delete();

            File grayImg = new File(projConf.GRAY_IMG_DIR + pageId + projConf.GRAY_IMG_EXT);
            if(grayImg.exists())
                grayImg.delete();
        }

        // Delete Despeckled images as well (they are generated from Binary images)
        DespecklingHelper despecklingHelper = new DespecklingHelper(projConf.PROJECT_DIR, projectImageType, processingMode);
        despecklingHelper.deleteOldFiles(pageIds);
    }

    /**
     * Checks if process depending files already exist
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @return Information if files exist
     */
    public boolean doOldFilesExist(String[] pageIds) {
        for(String pageId : pageIds) {
            if (procStateCol.preprocessingState(pageId) == true)
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
        return ProcessConflictDetector.preprocessingConflict(currentProcesses, inProcessFlow);
    }
}
