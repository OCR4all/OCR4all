package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uniwue.config.ProjectConfiguration;
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
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Evaluation process
     */
    private int progress = -1;

    /**
     * Indicates if a Evaluation process is already running
     */
    private boolean evaluationRunning = false;

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
     * Returns the absolute path of all gt files for the pages in the processState
     *
     * @return List of gt files
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
    public void evaluatePages(List<String> pageIds, List<String> cmdArgs) throws IOException {
        evaluationRunning = true;

        progress = 0;

        List<String> command = new ArrayList<String>();
        List<String> GtFiles = getGtFilesOfPages(pageIds);
        for (String gtFile : GtFiles) {
            // Add affected line segment images with their absolute path to the command list
            command.add(gtFile);
        }
        progress = 20;
        command.addAll(cmdArgs);
        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocropus-econf", command, false);

        evaluationRunning = false;
        progress = 100;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
    	evaluationRunning = false;
        progress = -1;
    }

    /**
     * Gets the Evaluation status
     *
     * @return status if the process is running
     */
    public boolean isEvaluationRunning() {
        return evaluationRunning;
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
        // Prevent function from calculation progress if process is not running
        if (evaluationRunning == false)
            return progress;

        return progress;
    }
}
