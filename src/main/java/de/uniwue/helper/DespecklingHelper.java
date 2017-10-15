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
        projDirConf = new ProjectDirConfig(projectDir);
    }

    /**
     * Despeckles given pages and stores them on the filesystem
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param maxContourRemovalSize Maximum size of the contours to be removed
     */
    public void despeckleGivenPages(List<String> pageIds, double maxContourRemovalSize) {
        File DespDir = new File(projDirConf.DESP_IMG_DIR);
        if (!DespDir.exists())
            DespDir.mkdir();

        double i = 1;
        progress = 0;
        int totalPages = pageIds.size();
        for (String pageId : pageIds) {
            if (stop == true)
                return;

            Mat mat = Imgcodecs.imread(projDirConf.BINR_IMG_DIR + File.separator + pageId + projDirConf.IMG_EXT);
            mat = ImageDespeckle.despeckle(mat, maxContourRemovalSize, "standard");
            Imgcodecs.imwrite(projDirConf.DESP_IMG_DIR + File.separator + pageId + projDirConf.IMG_EXT, mat);

            progress = (int) (i / totalPages * 100);
            i = i + 1;
        }
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
