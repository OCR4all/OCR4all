package de.uniwue.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;

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

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public PreprocessingHelper(String projectDir) {
        projDirConf = new ProjectDirConfig(projectDir);
    }

    /**
     * Executes image preprocessing of one page with "ocropus-nlbin".
     * This process creates the preprocessed and moves them to the favored location.
     *
     * @param pageId Identifier of the page (e.g 0002)
     * @param out Output stream of controller (to pass output to JSP)
     * @throws IOException
     */
    public void preprocessPage(String pageId, OutputStream out) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("ocropus-nlbin",projDirConf.ORIG_IMG_DIR + pageId 
                + projDirConf.IMG_EXT, "-o",projDirConf.PREPROC_DIR);
        Process p = builder.start();
        System.out.println(builder.command().toString());
        //System.out.println("here");
        //Process p = Runtime.getRuntime().exec("ocropus-nlbin " +  projDirConf.ORIG_IMG_DIR + pageId + projDirConf.IMG_EXT
        //        + " -o " + projDirConf.PREPROC_DIR);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        // Update output stream of PreprocesingController.executePreprocessing
        String line;
        while ((line = reader.readLine ()) != null) {
            out.write(line.getBytes());
            out.flush();
        }

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
     * @param out Output stream of controller (to pass output to JSP)
     * @throws IOException
     */
    public void preprocessAllPages(OutputStream out) throws IOException {
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
        double i = 0;
        for(File pageFile : pageFiles) {
            //TODO: Check if nmr_image exists (When not a binary-only project)
            File binImg = new File(projDirConf.BINR_IMG_DIR + pageFile.getName());
            if(!binImg.exists())
                preprocessPage(FilenameUtils.removeExtension(pageFile.getName()), out);
            progress = (int) (i/totalPages*100);
            System.out.println(progress);
            i = i+1;
        }
        progress = -1;
        return;
    }
    /**
     * Returns the progress of the job
     * @return progress of preprocessAllPages function
     */
    public int getProgress() {
        return progress;
    }

}
