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
import de.uniwue.feature.ProcessHandler;

/**
 * Helper class for line segmenting pages, which also calls the ocropus-gpageseg program
 */
public class LineSegmentationHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

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
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public LineSegmentationHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
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

            TreeMap<String, Boolean> segmentIds = new TreeMap<String, Boolean>();
            Files.walk(Paths.get(projConf.PAGE_DIR + pageId), 1)
            .map(Path::toFile)
            .filter(fileEntry -> fileEntry.isFile())
            .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
            .forEach(
                fileEntry -> { 
                    if (!fileEntry.getName().endsWith(projConf.PSEG_EXT))
                        segmentIds.put(FilenameUtils.removeExtension(fileEntry.getName()), false);
                }
            );

            processState.put(pageId, segmentIds);
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

                if (new File(projConf.PAGE_DIR + pageId + File.separator + segmentId + projConf.PSEG_EXT).exists()) {
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
                if(!segmentDir.exists())
                    segmentDir.mkdirs();

                // Copy segment image to own segment directory and create pseg-file to indicate successful processing
                File segmentImage = new File(projConf.PAGE_DIR + pageId + File.separator + segmentId + projConf.IMG_EXT);
                Files.copy(Paths.get(segmentImage.getPath()),
                    Paths.get(segmentDir.getAbsolutePath() + File.separator + segmentId + projConf.IMG_EXT),
                    StandardCopyOption.valueOf("REPLACE_EXISTING"));
                Files.copy(Paths.get(segmentImage.getPath()),
                    Paths.get(projConf.PAGE_DIR + pageId + File.separator + segmentId + projConf.PSEG_EXT),
                    StandardCopyOption.valueOf("REPLACE_EXISTING"));
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
    public void lineSegmentPages(List<String> pageIds, List<String> cmdArgs) throws IOException {
        lineSegmentationRunning = true;

        progress = 0;

        // Reset line segment data
        deleteOldFiles(pageIds);
        initializeProcessState(pageIds);

        List<String> command = new ArrayList<String>();
        List<String> segmentImages = getSegmentImagesForCurrentProcess();
        for (String segmentImage : segmentImages) {
            // Add affected line segment images with their absolute path to the command list
            command.add(segmentImage);
        }
        command.addAll(cmdArgs);
        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocropus-gpageseg", command, false);

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
     * Gets the Line Segmentation status
     *
     * @return status if the process is running
     */
    public boolean isLineSegmentationRunning() {
        return lineSegmentationRunning;
    }

    /**
     * Returns the Ids of the pages, for which region extraction was already executed
     *
     * @return List of valid page Ids
     */
    public ArrayList<String> getValidPageIdsforLineSegmentation() {
        ArrayList<String> validPageIds = new ArrayList<String>();
        File pageDir = new File(projConf.PAGE_DIR);
        if (!pageDir.exists())
            return validPageIds;

        File[] directories = pageDir.listFiles(File::isDirectory);
        for(File file: directories) { 
            validPageIds.add(file.getName());
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
            if (!new File(projConf.PAGE_DIR + pageId).exists())
                return;

            // Delete directories of the line segments
            File[] lineSegmentDirectories = new File(projConf.PAGE_DIR + pageId).listFiles(File::isDirectory);
            if (lineSegmentDirectories.length != 0) {
                for(File dir : lineSegmentDirectories)
                    FileUtils.deleteDirectory(dir);
            }

            // Delete files that indicate successful line segementation process
            File[] psegFiles = new File(projConf.PAGE_DIR + pageId).listFiles((d, name) -> name.endsWith(projConf.PSEG_EXT));
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
        for(String page : pageIds) {
            File pageDir = new File(projConf.PAGE_DIR + page);
            if (!pageDir.exists())
                continue;

            // Check for folders and pseg-files
            if (pageDir.listFiles(File::isDirectory).length != 0)
                return true;
            if (pageDir.listFiles((d, name) -> name.endsWith(projConf.PSEG_EXT)).length != 0)
                return true;
        }
        return false;
    }
}
