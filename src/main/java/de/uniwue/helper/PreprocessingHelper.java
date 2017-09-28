package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import de.uniwue.config.ProjectDirConfig;

/**
 * Helper class for preprocessing pages, which also calls the ocrubus-nlbin function 
 */
public class PreprocessingHelper {
    /**
     * Object to access project directory configuration
     */
    private ProjectDirConfig projDirConf;
    private int progress = -1;
    private List<InputStream> streams = new ArrayList<InputStream>();
    private DefaultExecutor executor; 
    private ExecuteWatchdog watchdog; 


    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public PreprocessingHelper(String projectDir) {
        projDirConf = new ProjectDirConfig(projectDir);
        executor = new DefaultExecutor();
        watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);
    }

    /**
     * Executes image preprocessing of one page with "ocropus-nlbin".
     * This process creates the preprocessed and moves them to the favored location.
     *
     * @param pageId Identifier of the page (e.g 0002)
     * @throws IOException
     */
    public void preprocessPage(String pageId,List<String> args) throws IOException {
        CommandLine cmdLine = new CommandLine("ocropus-nlbin");
        cmdLine.addArgument(projDirConf.ORIG_IMG_DIR + pageId + projDirConf.IMG_EXT);
        cmdLine.addArgument("-o");
        cmdLine.addArgument(projDirConf.PREPROC_DIR);
        for(String arg : args) {
            cmdLine.addArgument(arg);
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(os));
        executor.execute(cmdLine);
        streams.add(os.toInputStream());

        // Hardcoded 0001 because of Ocropus naming convention. We call "ocropus-nlbin"
        // for each file individually and given images are named with incremented numbers.
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
        progress = 1;
        for(File pageFile : pageFiles) {
            //TODO: Check if nmr_image exists (When not a binary-only project)
            File binImg = new File(projDirConf.BINR_IMG_DIR + pageFile.getName());
            if(!binImg.exists())
                preprocessPage(FilenameUtils.removeExtension(pageFile.getName()),args);
            progress = (int) (i/totalPages*100);
            i = i+1;
        }
        return;
    }

    /**
     * Returns the InputStreams of the commandLine output
     * @return Returns the InputStreams of the commandLine output
     */
    public List<InputStream> getStreams() {
        return streams;
    }

    /**
     * Returns the progress of the job
     * @return progress of preprocessAllPages function
     */
    public int getProgress() {
        return progress;
    }

    public void cancelPreprocessAllPages() {
            if(watchdog.isWatching()) {
                watchdog.destroyProcess();
            }
    }

}
