package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

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
     * Executes image preprocessing of one page with "ocropus-nlbin".
     * This process creates the preprocessed and moves them to the favored location.
     *
     * @param pageId Identifier of the page (e.g 0002)
     * @throws IOException
     */
    public void preprocessPage(String pageId,List<String> args) throws IOException {
        List<String> cpArgs = new ArrayList<String>(); 
        cpArgs.add(projDirConf.ORIG_IMG_DIR + pageId + projDirConf.IMG_EXT);
        cpArgs.add("-o");
        cpArgs.add(projDirConf.PREPROC_DIR);
        for(String arg : args) {
            cpArgs.add(arg);
        }
        processHandler.setCommandLine("ocropus-nlbin", cpArgs);
        processHandler.setConsoleOutput(true);
        processHandler.start();

        File binImg = new File(projDirConf.PREPROC_DIR + "0001" + projDirConf.BIN_IMG_EXT);
        if (binImg.exists())
            binImg.renameTo(new File(projDirConf.BINR_IMG_DIR + pageId + projDirConf.IMG_EXT));

        File grayImg = new File(projDirConf.PREPROC_DIR + "0001" + projDirConf.GRAY_IMG_EXT);
        if (grayImg.exists())
            grayImg.renameTo(new File(projDirConf.GRAY_IMG_DIR + pageId + projDirConf.IMG_EXT));

        return;
    }

    /**
     * Executes image preprocessing of all pages.
     * This process creates the preprocessed directory structure.
     *
     * @throws IOException
     */
    public void preprocessAllPages(List<String> args) throws IOException {
        stop = false;
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

        File[] pageFiles = origDir.listFiles((d, name) -> name.endsWith(projDirConf.IMG_EXT));
        Arrays.sort(pageFiles);
        int totalPages = pageFiles.length;
        double i = 1;
        progress = 0;
        for(File pageFile : pageFiles) {
            if (stop == true) {
                progress = -1;
                return;
            }

            //TODO: Check if nmr_image exists (When not a binary-only project)
            File binImg = new File(projDirConf.BINR_IMG_DIR + pageFile.getName());
            if(!binImg.exists())
                preprocessPage(FilenameUtils.removeExtension(pageFile.getName()),args);
            progress = (int) (i / totalPages * 100);
            i = i+1;
        }
        return;
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
     * Handles the process
     * @return Returns the process Helper
     */
    public ProcessHandler getProcessHelper() {
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
     *  Get the the number of logical thread of the system
     * @return number of logical threads
     */
    public static int getLogicalThreadCount() {
        return Runtime.getRuntime().availableProcessors();
    }
}
