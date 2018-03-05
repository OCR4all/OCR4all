package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
     * Segmented pages
     */
    private List<String> SegmentedPages = new ArrayList<String>();

    /** 
     * Pages for which line segmentation should be done
     */
    private List<String> Pages = new ArrayList<String>();

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
     * Returns the regions of a list of pages
     *
     * @param pageIds Id of the pages
     * @return List of regions
     * @throws IOException 
     */
    public List<String> getRegionsOfPage(List<String> pageIds) throws IOException {
        List<String> RegionsOfPage = new ArrayList<String>();
        for(String pageId :pageIds) {
            // File depth of 1 -> no recursive (file)listing 
            //TODO: At the moment general images are considered --> Make it dependent on project type
            Files.walk(Paths.get(projConf.PAGE_DIR + pageId), 1)
            .map(Path::toFile)
            .filter(fileEntry -> fileEntry.isFile())
            .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
            .forEach(
                fileEntry -> { 
                    if(!FilenameUtils.removeExtension(fileEntry.getName()).endsWith(".pseg"));
                    RegionsOfPage.add(projConf.PAGE_DIR + pageId + File.separator +fileEntry.getName());
                }
            );
        }
        return RegionsOfPage;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     * @throws IOException 
     */
    public int getProgress() throws IOException {
        // Prevent progress update at startup
        if (!lineSegmentationRunning && progress != 100)
            return -1;

        for(String pageId : Pages) {
            if(SegmentedPages.contains(pageId))
                continue;

            File[] directories = new File(projConf.PAGE_DIR + pageId).listFiles(File::isDirectory);
            List<String> RegionsOfPage = new ArrayList<String>();
            Files.walk(Paths.get(projConf.PAGE_DIR + pageId), 1)
            .map(Path::toFile)
            .filter(fileEntry -> fileEntry.isFile())
            .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
            .forEach(
                fileEntry -> {
                    if(!FilenameUtils.removeExtension(fileEntry.getName()).endsWith("pseg")) {
                    RegionsOfPage.add(projConf.PAGE_DIR + pageId + File.separator + fileEntry.getName());
                    }
                }
            );

            if(directories.length == RegionsOfPage.size()) {
                SegmentedPages.add(pageId);
            }
            else    
                break;
        }
        return (progress != 100) ? (int) ((double) SegmentedPages.size() / Pages.size() * 100) : 100;
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

        File origDir = new File(projConf.ORIG_IMG_DIR);
        if (!origDir.exists())
            return;

        progress = 0;
        Pages = pageIds;

        List<String> command = new ArrayList<String>();
        List<String> RegionsOfPage = getRegionsOfPage(pageIds);
        for (String region : RegionsOfPage) {
            // Add affected pages with their absolute path to the command list
            command.add(region);
        }
        command.addAll(cmdArgs);
        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocropus-gpageseg", command, false);

        progress = 100;
        lineSegmentationRunning = false;
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
     * @return List with page ids
     */
    public ArrayList<String> getIdsforLineSegmentation(){
        ArrayList<String> IdsForImageList = new ArrayList<String>();
        File[] directories = new File(projConf.PAGE_DIR).listFiles(File::isDirectory);
        for(File file: directories) { 
            IdsForImageList.add(file.getName());}
        return IdsForImageList;
    }
}
