package de.uniwue.helper;

import java.io.File;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ImageDespeckle;

/**
 * Helper class for despeckling module
 */
public class DespecklingHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Status of the progress
     */
    private int progress = -1;

    /**
     * Indicates if the process should be cancelled
     */
    private boolean stop = false;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public DespecklingHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
    }

    /**
     * Despeckles given pages and stores them on the filesystem
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param maxContourRemovalSize Maximum size of the contours to be removed
     */
    public void despeckleGivenPages(List<String> pageIds, double maxContourRemovalSize) {
        File DespDir = new File(projConf.DESP_IMG_DIR);
        if (!DespDir.exists())
            DespDir.mkdir();

        progress = 0;

        double i = 1;
        int totalPages = pageIds.size();
        for (String pageId : pageIds) {
            if (stop == true)
                break;

            Mat mat = Imgcodecs.imread(projConf.BINR_IMG_DIR + File.separator + pageId + projConf.IMG_EXT);
            mat = ImageDespeckle.despeckle(mat, maxContourRemovalSize, "standard");
            Imgcodecs.imwrite(projConf.DESP_IMG_DIR + File.separator + pageId + projConf.IMG_EXT, mat);

            progress = (int) (i / totalPages * 100);
            i = i + 1;
        }

        progress = 100;
    }

    /**
     * Returns the progress of the job
     *
     * @return Progress of preprocessAllPages function
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
