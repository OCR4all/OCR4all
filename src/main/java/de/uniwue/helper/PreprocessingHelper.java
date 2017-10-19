package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.uniwue.config.ProjectDirConfig;
import de.uniwue.feature.ProcessHandler;

/**
 * Helper class for preprocessing pages, which also calls the ocrubus-nlbin function 
 */
public class PreprocessingHelper {
    /**
     * Object to access project directory configuration
     */
    private ProjectDirConfig projDirConf;

    /**
     * Progress of the process
     */
    private int progress = -1;

    /**
     * Status if the process should be cancelled
     */
    private boolean stop = false;

    /**
     * Helper, who is managing the process
     */
    ProcessHandler processHandler = null;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public PreprocessingHelper(String projectDir) {
        projDirConf = new ProjectDirConfig(projectDir);
        processHandler = new ProcessHandler();

    }

    /**
     * Executes image preprocessing of all pages.
     * Achieved with the help of the external python program "ocropus-nlbin".
     * This function also creates the preprocessed directory structure.
     * 
     * @param cmdArgs Command line arguments for "ocropus-nlbin"
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void preprocessPages(List<String> cmdArgs, List<String> pageIds) throws IOException {
        File origDir = new File(projDirConf.ORIG_IMG_DIR);
        if (!origDir.exists())
            return;

        File preprocDir = new File(projDirConf.PREPROC_DIR);
        if (!preprocDir.exists())
            preprocDir.mkdir();

        File binDir = new File(projDirConf.BINR_IMG_DIR);
        if (!binDir.exists())
            binDir.mkdir();

        File grayDir = new File(projDirConf.GRAY_IMG_DIR);
        if (!grayDir.exists())
            grayDir.mkdir();

        //TODO: Implement process handling and logging functionalities
    }

    /**
     * Returns the progress of the job
     *
     * @return progress of preprocessAllPages function
     */
    public int getProgress() {
        return progress;
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
     * Cancels the preprocessAllPages process
     */
    public void cancelPreprocessAllPages() {
        if (processHandler.stop() == true) {
            stop = true;
        }
    }

    /**
     * Gets the the number of logical thread of the system
     *
     * @return Number of logical threads
     */
    public static int getLogicalThreadCount() {
        return Runtime.getRuntime().availableProcessors();
    }
}
