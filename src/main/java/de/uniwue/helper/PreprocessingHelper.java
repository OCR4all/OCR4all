package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessHandler;

/**
 * Helper class for preprocessing pages, which also calls the ocrubus-nlbin program 
 */
public class PreprocessingHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Progress of the process
     */
    private int progress = -1;

    /**
     * Helper object for process handling
     */
    ProcessHandler processHandler = null;

    /** 
     * Number of pages to process
     */
    private int specifiedImageCount = 0;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public PreprocessingHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
        processHandler = new ProcessHandler();

    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        File preprocDir = new File(projConf.PREPROC_DIR);
        File[] binFiles = preprocDir.listFiles((d, name) -> name.endsWith(projConf.BIN_IMG_EXT));
        // Calcuclating the progress of the preprocess process 
        // Maximum progress = 90%, since the preprocessed files still need to be moved
        if (binFiles.length != 0)
            progress =  (int) ((double) binFiles.length / specifiedImageCount * 0.9 * 100);
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
        File origDir = new File(projConf.ORIG_IMG_DIR);
        if (!origDir.exists())
            return;

        File preprocDir = new File(projConf.PREPROC_DIR);
        if (!preprocDir.exists())
            preprocDir.mkdir();

        File binDir = new File(projConf.BINR_IMG_DIR);
        if (!binDir.exists())
            binDir.mkdir();

        File grayDir = new File(projConf.GRAY_IMG_DIR);
        if (!grayDir.exists())
            grayDir.mkdir();

        specifiedImageCount = pageIds.size();
        progress = 0;

        // Add pages with their absolute path to the command list
        List<String> command = new ArrayList<String>();
        for (String pageId : pageIds) {
            command.add(projConf.ORIG_IMG_DIR + pageId + projConf.IMG_EXT);
        }
        command.add("-o");
        command.add(preprocDir.toString());
        command.addAll(cmdArgs);

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        // Process completion status can be fetched with processHandler.isCompleted()
        processHandler.startProcess("ocropus-nlbin", command, false);

        if (progress < 90)
            progress = 90;

        // Move preprocessed pages to projDirConf.PREPROC_DIR
        File[] binFiles = preprocDir.listFiles((d, name) -> name.endsWith(projConf.BIN_IMG_EXT));
        Arrays.sort(binFiles);
        for (File image : binFiles) {
            image.renameTo(new File(projConf.BINR_IMG_DIR + pageIds.get(Integer.parseInt(FilenameUtils.removeExtension(FilenameUtils
                          .removeExtension(image.getName()))) -1) + projConf.IMG_EXT));
              }
        
        File[] nrmFiles = preprocDir.listFiles((d, name) -> name.endsWith(projConf.GRAY_IMG_EXT));
        Arrays.sort(nrmFiles);
        for (File image : nrmFiles) {
            image.renameTo(new File(projConf.GRAY_IMG_DIR + pageIds.get(Integer.parseInt(FilenameUtils.removeExtension(FilenameUtils
                                       .removeExtension(image.getName()))) -1) + projConf.IMG_EXT));
        }

        progress = 100;
    }
}
