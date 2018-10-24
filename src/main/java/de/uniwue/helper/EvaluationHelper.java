package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;

public class EvaluationHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Object to use generic functionalities
     */
    private GenericHelper genericHelper;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Evaluation process
     */
    private int progress = -1;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (binary, gray)
     */
    public EvaluationHelper(String projectDir, String projectImageType) {
        projConf = new ProjectConfiguration(projectDir);
        processHandler = new ProcessHandler();
        procStateCol = new ProcessStateCollector(projConf, projectImageType);
        genericHelper = new GenericHelper(projConf);
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
     * Returns the absolute path of all ".gt.txt" files for the pages in the processState
     *
     * @return List of ".gt.txt" files
     * @throws IOException 
     */
    public List<String> getGtFilesOfPages(List<String> pageIds){
        List<String> GtOfPages = new ArrayList<String>();
        for(String pageId : pageIds) {
            File[] directories = new File(projConf.PAGE_DIR + pageId).listFiles(File::isDirectory);
            if (directories != null && directories.length != 0) {
                for(File dir : directories) {
                    File[] GtFiles = dir.listFiles((d, name) -> name.endsWith(projConf.GT_EXT));
                    for(File gtFile :GtFiles)
                        GtOfPages.add(gtFile.getAbsolutePath());
                }
            }
        }
        return GtOfPages;
    }

    /**
     * Executes image Evaluation of all pages
     * Achieved with the help of the external python program "ocropus-econf"
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "ocropus-econf"
     * @throws IOException
     */
    public void execute(List<String> pageIds, List<String> cmdArgs) throws IOException {
        progress = 0;

        List<String> command = new ArrayList<String>();
        List<String> gtFiles = getGtFilesOfPages(pageIds);
        command.add("--gt");
        for (String gtFile : gtFiles) {
            // Add affected line segment images with their absolute path to the command list
            command.add(gtFile);
        }
        progress = 20;
        command.addAll(cmdArgs);
        command.add("--no_progress_bars");

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("calamari-eval", command, false);

        progress = 100;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
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
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Returns the Ids of the pages, for which recognition process was already executed
     *
     * @return List of valid page Ids
     * @throws IOException 
     */
    public ArrayList<String> getValidPageIds() throws IOException {
        // Get all pages and check which one were already recognized
        ArrayList<String> validPageIds = new ArrayList<String>();
        ArrayList<String> allPageIds = genericHelper.getPageList("Original");
        for (String pageId : allPageIds) {
            if (procStateCol.recognitionState(pageId) == true)
                validPageIds.add(pageId);
        }

        Collections.sort(validPageIds);
        return validPageIds;
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses) {
        return ProcessConflictDetector.evaluationConflict(currentProcesses);
    }
}
