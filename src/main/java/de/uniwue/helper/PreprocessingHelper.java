package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.uniwue.config.ProjectDirConfig;
import de.uniwue.feature.ProcessHandler;

/**
 * Helper class for preprocessing pages, which also calls the ocrubus-nlbin program 
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
     * Helper object for process handling
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
     * Returns the progress of the process
     *
     * @return Progress percentage
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
     * Gets the the number of logical thread of the system
     *
     * @return Number of logical threads
     */
    public static int getLogicalThreadCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Executes image preprocessing of all pages.
     * Achieved with the help of the external python program "ocropus-nlbin".
     * This function also creates the preprocessed directory structure.
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "ocropus-nlbin"
     * @throws IOException
     * @throws InterruptedException 
     */
    public void preprocessPages(List<String> pageIds, List<String> cmdArgs) throws IOException, InterruptedException {
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

        // TODO: Implement progress handling (currently just dummy handling to ensure correct behavior)
        progress = 1;

        // Add pages with their absolute path to the command list
        List<String> command = new ArrayList<String>();
        for (String pageId : pageIds) {
            command.add(projDirConf.ORIG_IMG_DIR + pageId + projDirConf.IMG_EXT);
        }
        command.addAll(cmdArgs);

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        // TODO: Set last parameter to true (run in background) and start progress handling afterwards
        //       Process completion status can be fetched with processHandler.isCompleted()
        processHandler.startProcess("ocropus-nlbin", command, false);

        // TODO: Move preprocessed pages to projDirConf.PREPROC_DIR

        progress = 100;
    }
}
