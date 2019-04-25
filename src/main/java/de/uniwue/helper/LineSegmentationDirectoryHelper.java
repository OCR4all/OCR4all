package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
 * Helper class for line segmenting pages, which also calls the ocropus-gpageseg program
 */
public class LineSegmentationDirectoryHelper implements LineSegmentationHelper{
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
    public LineSegmentationDirectoryHelper(String projectDir, String projectImageType, String processingMode) {
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
     * Returns the absolute path of all segment images for the pages in the processState
     *
     * @return List of segment images
     * @throws IOException 
     */
    public List<String> getSegmentImagesForCurrentProcess() throws IOException {
        List<String> segmentImages = new ArrayList<String>();
        for (String pageId : processState.keySet()) {
            for (String segment :processState.get(pageId).keySet()) {
                segmentImages.add(projConf.PAGE_DIR + pageId + File.separator + segment + projConf.IMG_EXT);
            }
        }
        return segmentImages;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     * @throws IOException 
     */
    public int getProgress() throws IOException {
        // Prevent function from calculation progress if process is not running
        if (lineSegmentationRunning == false)
            return progress;

        int lineSegmentCount = 0;
        int processedLineSegmentCount = 0;
        // Identify how many segments are already processed
        for (String pageId : processState.keySet()) {
            for (String segmentId : processState.get(pageId).keySet()) {
                lineSegmentCount += 1;

                if (processState.get(pageId).get(segmentId) == true) {
                    processedLineSegmentCount += 1;
                    continue;
                }

                if (new File(projConf.PAGE_DIR + pageId + File.separator + segmentId + projConf.PSEG_EXT).exists() 
                        && new File(projConf.PAGE_DIR + pageId + File.separator + segmentId).exists()
                        && new File(projConf.PAGE_DIR + pageId + File.separator + segmentId).listFiles().length != 0) {
                    processState.get(pageId).put(segmentId, true);
                }
            }
        }

        // Safe check, because ocropus-gpageseg script does not guarantee that all pseg-files are created
        return (progress != 100) ? (int) ((double) processedLineSegmentCount / lineSegmentCount * 100) : 100;
    }

    /**
     * Creates the line segment files of the segments that were skipped by the ocropus-gpageseg script
     * This can occur when the image resolution is too low
     *
     * @throws IOException
     */
    public void createSkippedSegments() throws IOException{
        for(String pageId : processState.keySet()) {
            for(String segmentId :processState.get(pageId).keySet()) {
                if (processState.get(pageId).get(segmentId) == true)
                    continue;

                File segmentDir = new File(projConf.PAGE_DIR + pageId + File.separator + segmentId);
                if (!segmentDir.exists())
                    segmentDir.mkdirs();

                // Copy segment image to own segment directory and create pseg-file to indicate successful processing
                switch(projectImageType) {
                    case "Gray":
                        Files.copy(Paths.get(projConf.PAGE_DIR + pageId + File.separator + segmentId + projConf.GRAY_IMG_EXT),
                            Paths.get(segmentDir.getAbsolutePath() + File.separator + segmentId + "__000" + projConf.GRAY_IMG_EXT),
                            StandardCopyOption.valueOf("REPLACE_EXISTING"));
                    case "Binary":
                        Files.copy(Paths.get(projConf.PAGE_DIR + pageId + File.separator + segmentId + projConf.BINR_IMG_EXT),
                            Paths.get(segmentDir.getAbsolutePath() + File.separator + segmentId + "__000" + projConf.BINR_IMG_EXT),
                            StandardCopyOption.valueOf("REPLACE_EXISTING"));

                        Files.copy(Paths.get(projConf.PAGE_DIR + pageId + File.separator + segmentId + projConf.IMG_EXT),
                            Paths.get(projConf.PAGE_DIR + pageId + File.separator + segmentId + projConf.PSEG_EXT),
                            StandardCopyOption.valueOf("REPLACE_EXISTING"));
                    default: break;
                }
            }
        }
    }

    /**
     * Executes line segmentation of all pages
     * Achieved with the help of the external python program "ocropus-gpageseg"
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "ocropus-gpageseg"
     * @throws IOException
     */
    public void execute(List<String> pageIds, List<String> cmdArgs) throws IOException {
        lineSegmentationRunning = true;

        progress = 0;

        // Reset line segment data
        deleteOldFiles(pageIds);
        initializeProcessState(pageIds);

        List<String> command = new ArrayList<String>();
        List<String> segmentImages = getSegmentImagesForCurrentProcess();
        for (String segmentImage : segmentImages) {
            // Add affected segment images with their absolute path to the command list
            command.add(segmentImage);
        }
        command.addAll(cmdArgs);
        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocropus-gpageseg", command, false);

        // Execute progress update to fill processState data structure with correct values
        getProgress();
        // Process extension to ocropus-gpageseg script
        createSkippedSegments();

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
            if (procStateCol.regionExtractionState(pageId) == true)
                validPageIds.add(pageId);
        }

        Collections.sort(validPageIds);
        return validPageIds;
    }

    /**
     * Deletion of old process related files
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException 
     */
    public void deleteOldFiles(List<String> pageIds) throws IOException {
        for(String pageId : pageIds) {
            File pageDirectory = new File(projConf.PAGE_DIR + pageId);
            if (!pageDirectory.exists())
                return;

            // Delete directories of the line segments
            File[] lineSegmentDirectories = pageDirectory.listFiles(File::isDirectory);
            if (lineSegmentDirectories.length != 0) {
                for(File dir : lineSegmentDirectories)
                    FileUtils.deleteDirectory(dir);
            }

            // Delete files that indicate successful line segementation process
            File[] psegFiles = pageDirectory.listFiles((d, name) -> name.endsWith(projConf.PSEG_EXT));
            for(File pseg : psegFiles) {
                pseg.delete();}
        }
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
