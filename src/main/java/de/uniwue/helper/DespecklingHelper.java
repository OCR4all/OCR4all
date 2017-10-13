package de.uniwue.helper;

import java.io.File;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import de.uniwue.config.ProjectDirConfig;


public class DespecklingHelper {

    /**
     * Object to access project directory configuration
     */
    private ProjectDirConfig projDirConf;

    /**
     * Helper, who is managing functionality for images
     */
    private ImageHelper imageHelper;

    /**
     * Status if the process should be cancelled
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
        imageHelper = new ImageHelper(projectDir);
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
        progress = 1;
        File DespDir = new File(projDirConf.DESP_IMG_DIR);

        if (!DespDir.exists())
            DespDir.mkdir();

        for (String pageId : pageIdsAsList) {
            if (stop == true)
                return;
            Mat mat = Imgcodecs.imread(projDirConf.BINR_IMG_DIR + File.separator + pageId + projDirConf.IMG_EXT);
            Mat bwImage = new Mat();
            Imgproc.cvtColor(mat, bwImage, Imgproc.COLOR_RGB2GRAY);
            mat = imageHelper.despeckle(bwImage, maxArea, "standard");
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
