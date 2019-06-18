package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ImageDespeckle;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessStateCollector;

/**
 * Helper class for despeckling module
 */
public class DespecklingHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

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
     * @param projectImageType Type of the project (binary, gray)
     * @param processingMode Processing structure of the project (Directory, Pagexml)
     */
    public DespecklingHelper(String projectDir, String projectImageType, String processingMode) {
        projConf = new ProjectConfiguration(projectDir);
        procStateCol = new ProcessStateCollector(projConf, projectImageType, processingMode);
    }

    /**
     * Despeckles given pages and stores them on the filesystem
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param maxContourRemovalSize Maximum size of the contours to be removed
     * @throws IOException 
     */
    public void execute(List<String> pageIds, double maxContourRemovalSize) throws IOException {
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

            Mat mat = Imgcodecs.imread(projConf.BINR_IMG_DIR + File.separator + pageId + projConf.BINR_IMG_EXT);
            // Only the "standard" parameter is needed when despeckling
            // "marked" is only used to highlight changes for the user
            mat = ImageDespeckle.despeckle(mat, maxContourRemovalSize, "standard");
            Imgcodecs.imwrite(projConf.DESP_IMG_DIR + File.separator + pageId + projConf.DESP_IMG_EXT, mat);

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
        if (stop == true)
            return -1;
        return progress;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelDespecklingProcess() {
        stop = true;
    }

    /**
     * Deletion of old process related files
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException 
     */
    public void deleteOldFiles(List<String> pageIds) throws IOException {
        for(String pageId : pageIds) {
            File despImg = new File(projConf.DESP_IMG_DIR + pageId + projConf.DESP_IMG_EXT);
            if(despImg.exists())
                despImg.delete();
        }
    }

    /**
     * Checks if process depending files already exist
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @return Information if files exist
     */
    public boolean doOldFilesExist(String[] pageIds) {
        for (String pageId : pageIds) {
            if (procStateCol.despecklingState(pageId) == true)
                return true;
        }
        return false;
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses, boolean inProcessFlow) {
        return ProcessConflictDetector.despecklingConflict(currentProcesses, inProcessFlow);
    }
}
