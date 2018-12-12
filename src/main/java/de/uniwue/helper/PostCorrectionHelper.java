package de.uniwue.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessHandler;

/**
 * Helper class for Post Correction module
 */
public class PostCorrectionHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Post Correction process
     */
    private int progress = -1;

    /**
     * Indicates if a Post Correction process is already running
     */
    private boolean postCorrectionRunning = false;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public PostCorrectionHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
        processHandler = new ProcessHandler();
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        if (postCorrectionRunning == false)
            return progress;
        return 0;
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
     * Import OCR results to Nashi
     * Achieved with the help of nashi-* scripts
     * Always delete and import all data again to ensure integrity
     *
     * @param projectName Name of the current project
     * @throws IOException
     */
    public void execute(String projectName) throws IOException {
        postCorrectionRunning = true;
        progress = 0;

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);

        List<String> command = new ArrayList<String>();
        command.add(projectName);
        processHandler.startProcess("nashi-delete", command, false);
        progress = 50;

        command = new ArrayList<String>();
        command.add(projConf.RESULT_PAGES_DIR);
        command.add(projectName);
        command.add("*.xml");
        processHandler.startProcess("nashi-import", command, false);

        postCorrectionRunning = false;
        progress = 100;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        postCorrectionRunning = false;
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        if (processHandler != null)
            processHandler.stopProcess();
    }
}
