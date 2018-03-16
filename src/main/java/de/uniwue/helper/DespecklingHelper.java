package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
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
    * Indicates if a depeckling process is running
    */
    private boolean despecklingRunning = false;

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
     * @throws IOException 
     */
    public void despeckleGivenPages(List<String> pageIds, double maxContourRemovalSize) throws IOException {
        despecklingRunning = true;
        stop = false;

        progress = 0;

        File DespDir = new File(projConf.DESP_IMG_DIR);
        if (!DespDir.exists())
            DespDir.mkdir();
        deleteOldFiles(pageIds);


        double i = 1;
        int totalPages = pageIds.size();
        for (String pageId : pageIds) {
            if (stop == true) 
                break;

            Mat mat = Imgcodecs.imread(projConf.BINR_IMG_DIR + File.separator + pageId + projConf.IMG_EXT);
            // Only the "standard" parameter is needed when despeckling
            mat = ImageDespeckle.despeckle(mat, maxContourRemovalSize, "standard");
            Imgcodecs.imwrite(projConf.DESP_IMG_DIR + File.separator + pageId + projConf.IMG_EXT, mat);

            progress = (int) (i / totalPages * 100);
            i = i + 1;
        }

        progress = 100;
        despecklingRunning = false;
    }

    /**
     * Returns the progress of the job
     *
     * @return Progress of preprocessAllPages function
     */
    public int getProgress() {
        if (stop == true)
            return -1;
        return progress;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        despecklingRunning = false;
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelDespecklingProcess() {
        stop = true;
    }

    /**
    * Gets the despeckling status
    *
    * @return status if the process is running
    */
    public boolean isDespecklingRunning() {
        return despecklingRunning;
    }

    /**
     * Deletion of old process related files
     * @param pageIds
     * @throws IOException 
     */
    public void deleteOldFiles(List<String> pageIds) throws IOException {
        for(String pageId : pageIds) {
            File despImg = new File(projConf.DESP_IMG_DIR + pageId + projConf.IMG_EXT);
            if(despImg.exists())
                despImg.delete();
        }
    }
    /**
     * Checks if process depending files already exists
     * @param pageIds
     * @return
     */
    public boolean checkIfExisting(String[] pageIds){
        boolean exists = false;
        for(String page : pageIds) {
            if(new File(projConf.DESP_IMG_DIR + page + projConf.IMG_EXT).exists() ) {
                exists = true;
                break;
            }
        }
        return exists;
    }
}
