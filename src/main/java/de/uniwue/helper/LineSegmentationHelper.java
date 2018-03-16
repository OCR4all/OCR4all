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
     * Structure with which the progress of the process can be monitored
     * Example of the structure : 0002 --> segment --> false (processed)
     */
    private TreeMap<String,TreeMap<String, Boolean>> status= new TreeMap<String, TreeMap<String, Boolean>>();

    /**
     * Initializes the structure with which the progress of the process can be monitored
     *
     * @param pageIds Ids of the pages
     * @throws IOException
     */
    public void initialize(List<String> pageIds) throws IOException {
        deleteOldFiles(pageIds);
        // Initialize the status structure
        status= new TreeMap<String, TreeMap<String, Boolean>>();

        for(String pageId : pageIds) {
            TreeMap<String, Boolean> segments = new TreeMap<String, Boolean>();
            Files.walk(Paths.get(projConf.PAGE_DIR + pageId), 1)
            .map(Path::toFile)
            .filter(fileEntry -> fileEntry.isFile())
            .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
            .forEach(
                fileEntry -> { 
                    if(!FilenameUtils.removeExtension(fileEntry.getName()).endsWith(".pseg")) {
                        segments.put(fileEntry.getName(), false);}
                }
            );

        status.put(pageId, segments);
        }

    }

    /**
     * Deletion of old process related files
     * @param pageIds
     * @throws IOException 
     */
    public void deleteOldFiles(List<String> pageIds) throws IOException {
        for(String pageId : pageIds) {
            if(!new File(projConf.PAGE_DIR + pageId).exists())
                return;
            File[] directories = new File(projConf.PAGE_DIR + pageId).listFiles(File::isDirectory);
            if (directories.length != 0) {
                for(File dir : directories)
                    FileUtils.deleteDirectory(dir);
            }
            File[] FilesWithPsegExtension = new File(projConf.PAGE_DIR + pageId).listFiles((d, name) -> name.endsWith(".pseg"+ projConf.IMG_EXT));
            for(File pseg : FilesWithPsegExtension) {
                pseg.delete();}
        }
    }

    /**
     * Checks if process depending files already exists
     * @param pageIds
     * @return
     */
    public boolean checkIfExisting(String[] pageIds){
        boolean exists = false;
        for(String page : pageIds) {
            if(new File(projConf.PAGE_DIR + page).exists() && new File(projConf.PAGE_DIR + page).listFiles((d, name) -> name.endsWith(".pseg"+ projConf.IMG_EXT)).length !=0) {
                exists = true;
                break;
            }
        }

    return exists;
    }

    /**
     * Returns the segments of a list of pages
     *
     * @param pageIds Id of the pages
     * @return List of regions
     * @throws IOException 
     */
    public List<String> getSegmentsOfPage() throws IOException {
        List<String> SegmentsOfPage = new ArrayList<String>();
        for(String pageId : status.keySet()) {
            for(String segment :status.get(pageId).keySet()) {
                    SegmentsOfPage.add(projConf.PAGE_DIR + pageId + File.separator + segment);
            }
        }
        return SegmentsOfPage;
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

        int NumberOfLineSegments = 0;
        int NumberOfProcessedLineSegments = 0;
        for(String pageId : status.keySet()) {
            for(String segment :status.get(pageId).keySet()) {
                    if(status.get(pageId).get(segment)) {
                        NumberOfProcessedLineSegments += 1;
                        NumberOfLineSegments += 1;
                        continue;
                    }
                    else {
                        NumberOfLineSegments += 1;
                        if(new File(projConf.PAGE_DIR + pageId + File.separator + FilenameUtils.removeExtension(segment) + ".pseg" + projConf.IMG_EXT).exists()) {
                           status.get(pageId).put(segment, true);
                        }
                    }
                }
            }

        return (progress != 100) ? (int) ((double) NumberOfProcessedLineSegments / NumberOfLineSegments * 100) : 100;
    }

    /**
     * Moves the files that were skipped by the ocrubus gpaseg
     * @throws IOException
     */
    public void copySmallSegments() throws IOException{
        for(String pageId : status.keySet()) {
            for(String segment :status.get(pageId).keySet()) {
                    if(status.get(pageId).get(segment)) {
                        continue;
                    }
                    else {
                        File file = new File(projConf.PAGE_DIR + pageId + File.separator + segment);
                        File segmentDir = new File(projConf.PAGE_DIR + pageId + File.separator + FilenameUtils.removeExtension(segment));
                        if(!segmentDir.exists())
                            segmentDir.mkdirs();
                        Files.copy(Paths.get(file.getPath()), Paths.get(projConf.PAGE_DIR + pageId + File.separator + FilenameUtils.removeExtension(segment) + ".pseg" + projConf.IMG_EXT) ,StandardCopyOption.valueOf("REPLACE_EXISTING"));
                        Files.copy(Paths.get(file.getPath()), Paths.get(segmentDir.getAbsolutePath() + File.separator + segment) ,StandardCopyOption.valueOf("REPLACE_EXISTING"));

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
    public void lineSegmentPages(List<String> pageIds, List<String> cmdArgs) throws IOException {
        lineSegmentationRunning = true;

        progress = 0;

        initialize(pageIds);
        List<String> command = new ArrayList<String>();
        List<String> SegmentsOfPage = getSegmentsOfPage();
        for (String segment : SegmentsOfPage) {
            // Add affected pages with their absolute path to the command list
            command.add(segment);
        }
        command.addAll(cmdArgs);
        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocropus-gpageseg", command, false);

        copySmallSegments();

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
     * Returns the ids of the pages, where the region extraction step is already done
     *
     * @return List with page ids
     */
    public ArrayList<String> getIdsforLineSegmentation() {
        ArrayList<String> IdsForImageList = new ArrayList<String>();

        File pageDir = new File(projConf.PAGE_DIR);
        if (!pageDir.exists())
            return IdsForImageList;

        File[] directories = pageDir.listFiles(File::isDirectory);
        for(File file: directories) { 
            IdsForImageList.add(file.getName());
        }
        Collections.sort(IdsForImageList);

        return IdsForImageList;
    }
}
