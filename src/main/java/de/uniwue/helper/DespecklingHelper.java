package de.uniwue.helper;

import java.io.File;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import de.uniwue.config.ProjectDirConfig;
import de.uniwue.feature.ImageDespeckle;

/**
 * Helper class for despeckling module
 */
public class DespecklingHelper {

    /**
     * Object to access project directory configuration
     */
    private ProjectDirConfig projDirConf;

    /**
     * Image despeckle object to access despeckling functionality
     */
    private ImageDespeckle imageDespeckle;

    /**
     * Status of the progress
     */
    private int progress = -1;

    /**
     * Status if the process should be cancelled
     */
    private boolean stop = false;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public DespecklingHelper(String projectDir) {
        // Load OpenCV library (!important)
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

        imageDespeckle = new ImageDespeckle();
        projDirConf = new ProjectDirConfig(projectDir);
    }

    /**
     * Despeckles pages, that have been passed and saves them
     * @param pageIdsAsList
     * @param maxArea
     */
    public void despeckleGivenPages(List<String> pageIdsAsList, double maxArea) {
        int totalPages = pageIdsAsList.size();
        double i = 1;
        progress = 0;
        File DespDir = new File(projDirConf.DESP_IMG_DIR);

        if (!DespDir.exists())
            DespDir.mkdir();

        for (String pageId : pageIdsAsList) {
            if (stop == true)
                return;
            Mat mat = Imgcodecs.imread(projDirConf.BINR_IMG_DIR + File.separator + pageId + projDirConf.IMG_EXT);
            mat = imageDespeckle.despeckle(mat, maxArea, "standard");
            Imgcodecs.imwrite(projDirConf.DESP_IMG_DIR + File.separator + pageId + projDirConf.IMG_EXT, mat);
            progress = (int) (i / totalPages * 100);
            i = i + 1;
        }
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
     * Cancels the DespecklingProcess
     */
    public void cancelDespecklingProcess() {
        stop = true;
        
    }

}
