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
import de.uniwue.feature.ProcessHandler;

public class RecognitionHelper {
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
    private boolean RecognitionRunning = false;

    // Example : 0002 --> segment --> linesegment --> false (processed) 
    private TreeMap<String,TreeMap<String, TreeMap<String, Boolean>>> status= new TreeMap<String, TreeMap<String, TreeMap<String, Boolean>>>();
    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public RecognitionHelper(String projectDir) {
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
    public void initialize(List<String> pageIds) throws IOException {
        for(String pageId :pageIds) {
            TreeMap<String, TreeMap<String, Boolean>> tm = new TreeMap<String, TreeMap<String, Boolean>>();
            // File depth of 1 -> no recursive (file)listing 
            //TODO: At the moment general images are considered --> Make it dependent on project type
            File[] directories = new File(projConf.PAGE_DIR + pageId).listFiles(File::isDirectory);
            if (directories.length != 0) {
                for(File dir : directories) {
                    TreeMap<String, Boolean> filenames = new TreeMap<String, Boolean>();
                    Files.walk(Paths.get(dir.getAbsolutePath()), 1)
                    .map(Path::toFile)
                    .filter(fileEntry -> fileEntry.isFile())
                    .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
                    .forEach(
                        fileEntry -> {
                            filenames.put(fileEntry.getName(),false);
                        }
                    );
                    tm.put(dir.getName(), filenames);
                }
            }
            status.put(pageId, tm);
        }
    }

    /**
     * Returns the lineSegments of a list of pages
     *
     * @param pageIds Id of the pages
     * @return List of regions
     * @throws IOException 
     */
    public List<String> getLineSegmentsOfPages(List<String> pageIds) throws IOException {
        List<String> LineSegmentsOfPage = new ArrayList<String>();
        for(String pageId : status.keySet()) {
            for(String segment :status.get(pageId).keySet()) {
                for(String lineSegment: status.get(pageId).get(segment).keySet()) {
                    LineSegmentsOfPage.add(projConf.PAGE_DIR + pageId + File.separator + segment+ File.separator+ lineSegment);
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
        int NumberOfLineSegments = 0;
        int NumberOfProcessedLineSegments = 0;
        for(String pageId : status.keySet()) {
            for(String segment :status.get(pageId).keySet()) {
                for(String lineSegment: status.get(pageId).get(segment).keySet()) {
                    if(status.get(pageId).get(segment).get(lineSegment)) {
                        NumberOfProcessedLineSegments += 1;
                        NumberOfLineSegments += 1;
                        continue;
                    }
                    else {
                        NumberOfLineSegments += 1;
                        if(new File(projConf.PAGE_DIR + pageId + File.separator + segment + File.separator + FilenameUtils.removeExtension(FilenameUtils.removeExtension(lineSegment))+".txt").exists()) {
                           status.get(pageId).get(segment).put(lineSegment, true);
                        }
                    }
                }
            }
        }
        return (progress != 100) ? (int) ((double)NumberOfProcessedLineSegments / NumberOfLineSegments * 100) : 100;
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
        initialize(pageIds);
        progress = 0;
        List<String> command = new ArrayList<String>();
        List<String> LineSegmentsOfPage = getLineSegmentsOfPages(pageIds);
        for (String region : LineSegmentsOfPage) {
            // Add affected pages with their absolute path to the command list
            command.add(region);
        }
        command.addAll(cmdArgs);
        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocropus-rpred", command, false);

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
     * Returns the ids of the pages, where the region extraction step is already done
     *
     * @return List with page ids
     */
    public ArrayList<String> getIdsforRecognition() {
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

    /**
     * Gets the Recognition status
     *
     * @return status if the process is running
     */
    public boolean isRecongitionRunning() {
        return RecognitionRunning;
    }
}
