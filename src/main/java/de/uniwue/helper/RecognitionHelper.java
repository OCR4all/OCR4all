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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;

/**
 * Helper class for recognition module
 */
public class RecognitionHelper {
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
     * Progress of the Recognition process
     */
    private int progress = -1;

    /**
     * Indicates if a Recognition process is already running
     */
    private boolean RecognitionRunning = false;

    /**
     * Structure to monitor the progress of the process
     * pageId : segmentId : lineSegmentId : processedState
     *
     * Structure example:
     * {
     *     "0002": {
     *         "0002__000__paragraph" : {
     *             "0002__000__paragraph__000" : true,
     *             "0002__000__paragraph__001" : false,
     *             ...
     *         },
     *         ...
     *     },
     *     ...
     * }
     */
    private TreeMap<String,TreeMap<String, TreeMap<String, Boolean>>> processState =
        new TreeMap<String, TreeMap<String, TreeMap<String, Boolean>>>();

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (binary, gray)
     */
    public RecognitionHelper(String projectDir, String projectImageType) {
        this.projectImageType = projectImageType;
        projConf = new ProjectConfiguration(projectDir);
        genericHelper = new GenericHelper(projConf);
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
     * Initializes the structure with which the progress of the process can be monitored
     *
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @throws IOException
     */
    public void initialize(List<String> pageIds) throws IOException {
        // Initialize the status structure
        processState = new TreeMap<String, TreeMap<String, TreeMap<String, Boolean>>>();

        for (String pageId : pageIds) {
            TreeMap<String, TreeMap<String, Boolean>> segments = new TreeMap<String, TreeMap<String, Boolean>>();
            // File depth of 1 -> no recursive (file)listing
            File[] lineSegmentDirectories = new File(projConf.PAGE_DIR + pageId).listFiles(File::isDirectory);
            if (lineSegmentDirectories.length != 0) {
                for (File dir : lineSegmentDirectories) {
                    TreeMap<String, Boolean> lineSegments = new TreeMap<String, Boolean>();
                    Files.walk(Paths.get(dir.getAbsolutePath()), 1)
                    .map(Path::toFile)
                    .filter(fileEntry -> fileEntry.isFile())
                    .filter(fileEntry -> fileEntry.getName().endsWith(projConf.getImageExtensionByType(projectImageType)))
                    .forEach(
                        fileEntry -> {
                            // Line segments have one of the following endings: ".bin.png" | ".nrm.png"
                            // Therefore both extensions need to be removed
                            String lineSegmentId = FilenameUtils.removeExtension(FilenameUtils.removeExtension(fileEntry.getName()));
                            lineSegments.put(lineSegmentId, false);
                        }
                    );
                    segments.put(dir.getName(), lineSegments);
                }
            }

            processState.put(pageId, segments);
        }
    }

    /**
     * Returns the absolute path of all line segment images for the pages in the processState
     *
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @return List of line segment images
     * @throws IOException 
     */
    public List<String> getLineSegmentImagesForCurrentProcess(List<String> pageIds) throws IOException {
        List<String> LineSegmentsOfPage = new ArrayList<String>();
        for (String pageId : processState.keySet()) {
            for (String segmentId : processState.get(pageId).keySet()) {
                for (String lineSegmentId : processState.get(pageId).get(segmentId).keySet()) {
                    LineSegmentsOfPage.add(projConf.PAGE_DIR + pageId + File.separator + segmentId +
                        File.separator + lineSegmentId + projConf.getImageExtensionByType(projectImageType));
                }
            }
        }
        return LineSegmentsOfPage;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     * @throws IOException 
     */
    public int getProgress() throws IOException {
        // Prevent function from calculation progress if process is not running
        if (RecognitionRunning == false)
            return progress;

        int lineSegmentCount = 0;
        int processedLineSegmentCount = 0;
         // Identify how many line segments are already processed
        for (String pageId : processState.keySet()) {
            for (String segmentId : processState.get(pageId).keySet()) {
                for (String lineSegmentId : processState.get(pageId).get(segmentId).keySet()) {
                    lineSegmentCount += 1;

                    if(processState.get(pageId).get(segmentId).get(lineSegmentId)) {
                        processedLineSegmentCount += 1;
                        continue;
                    }

                    if (new File(projConf.PAGE_DIR + pageId + File.separator + segmentId +
                            File.separator + lineSegmentId + projConf.REC_EXT).exists()) {
                        processState.get(pageId).get(segmentId).put(lineSegmentId, true);
                    }
                }
            }
        }
        return (progress != 100) ? (int) ((double)processedLineSegmentCount / lineSegmentCount * 100) : 100;
    }

    /**
     * Executes OCR on a list of pages
     * Achieved with the help of the external python program "ocropus-rpred"
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "ocropus-rpred"
     * @throws IOException
     */
    public void RecognizeImages(List<String> pageIds, List<String> cmdArgs) throws IOException {
        RecognitionRunning = true;
        progress = 0;

        // Reset recognition data
        deleteOldFiles(pageIds);
        initialize(pageIds);

        List<String> command = new ArrayList<String>();
        List<String> lineSegmentImages = getLineSegmentImagesForCurrentProcess(pageIds);
        for (String lineSegmentImage : lineSegmentImages) {
            // Add affected line segment images with their absolute path to the command list
            command.add(lineSegmentImage);
        }
        command.addAll(cmdArgs);

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocropus-rpred", command, false);

        // Execute progress update to fill processState data structure with correct values
        getProgress();
        // Process extension to ocropus-gpageseg script
        createSkippedSegments();

        progress = 100;
        RecognitionRunning = false;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        RecognitionRunning = false;
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        if (processHandler != null)
            processHandler.stopProcess();
        RecognitionRunning = false;
    }

    /**
     * Returns the Ids of the pages, for which line segmentation was already executed
     *
     * @return List with page ids
     * @throws IOException 
     */
    public ArrayList<String> getValidPageIdsforRecognition() throws IOException {
        // Get all pages and check which ones are already line segmented
        ArrayList<String> validPageIds = new ArrayList<String>();
        ArrayList<String> allPageIds = genericHelper.getPageList("Original");
        for (String pageId : allPageIds) {
            if (procStateCol.lineSegmentationState(pageId) == true)
                validPageIds.add(pageId);
        }

        Collections.sort(validPageIds);
        return validPageIds;
    }

    /**
     * Deletion of old process related files
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     */
    public void deleteOldFiles(List<String> pageIds) {
        // Delete all files created by subsequent processes to preserve data integrity
        ResultGenerationHelper resultGenerationHelper = new ResultGenerationHelper(projConf.PROJECT_DIR, projectImageType);
        resultGenerationHelper.deleteOldFiles(pageIds);

        for(String pageId : pageIds) {
            File pageDirectory = new File(projConf.PAGE_DIR + pageId);
            if (!pageDirectory.exists())
                return;

            File[] lineSegmentDirectories = pageDirectory.listFiles(File::isDirectory);
            if (lineSegmentDirectories.length != 0) {
                for (File dir : lineSegmentDirectories) {
                    // Delete .txt files that store the recognized text
                    // Keep .gt.txt files that store already manually corrected text
                    File[] txtFiles = new File(dir.getAbsolutePath()).listFiles(
                        (d, name) -> name.endsWith(projConf.REC_EXT) && !name.endsWith(projConf.GT_EXT)
                    );
                    for (File txtFile : txtFiles) {
                        txtFile.delete();
                    }
                }
            }
        }
    }

    /**
     * Creates the recognition files of the linesegments that were skipped by the ocropus-rpred script
     *
     * @throws IOException
     */
    public void createSkippedSegments() throws IOException{
        for(String pageId : processState.keySet()) {
            for(String segmentId :processState.get(pageId).keySet()) {
                for (String lineSegmentId : processState.get(pageId).get(segmentId).keySet()) {
                    if (processState.get(pageId).get(segmentId).get(lineSegmentId) == true)
                        continue;

                    FileUtils.writeStringToFile(new File(projConf.PAGE_DIR + pageId + File.separator +
                        segmentId + File.separator + lineSegmentId + projConf.REC_EXT), "", "UTF8");
                }
            }
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
            if (procStateCol.recognitionState(pageId) == true)
                return true;
        }
        return false;
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses) {
        return ProcessConflictDetector.recognitionConflict(currentProcesses);
    }
}
