package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;

/**
 * Helper class for segmentation larex module
 */
public class SegmentationLarexHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Image type of the project
     * Possible values: { Binary, Gray }
     */
    private String projectImageType;

    /**
     * Processing structure of the project
     * Possible values: { Directory, Pagexml }
     */
    private String processingMode;

     /**
     * Status of the SegmentationLarex progress
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
     * @param projectImageType Type of the project (binary,gray)
     */
    public SegmentationLarexHelper(String projDir, String projectImageType, String processingMode) {
    	this.processingMode = processingMode;
        this.projectImageType = projectImageType;
        projConf = new ProjectConfiguration(projDir);
    }

    /**
     * Moves the extracted files of the segmentation process to the OCR project folder
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param segmentationImageType Image type of the segmentation (binary, despeckled)
     * @throws IOException
     */
    public void execute(List<String> pageIds, String segmentationImageType) throws IOException {
        stop = false;
        progress = 0;

        File ocrDir = new File(projConf.OCR_DIR);
        if (!ocrDir.exists())
            ocrDir.mkdir();

        SegmentationHelper segmentationHelper = new SegmentationHelper(projConf.PROJECT_DIR, this.projectImageType, this.processingMode);
        segmentationHelper.deleteOldFiles(pageIds);

        // Copy process specific images (based on project image type)
        File projectSpecificPreprocDir = new File(projConf.getImageDirectoryByType(projectImageType));
        if (projectSpecificPreprocDir.exists()){
            File[] filesToMove = projectSpecificPreprocDir.listFiles((d, name) -> name.endsWith(projConf.IMG_EXT));
            for (File file : filesToMove) {
                if (pageIds.contains(FilenameUtils.removeExtension(file.getName()))) {
                    Files.copy(Paths.get(file.getPath()),
                        Paths.get(projConf.getImageDirectoryByType("OCR") + file.getName()),
                        StandardCopyOption.valueOf("REPLACE_EXISTING"));
                }
            }
        }

        // Copy PageXML files (based on segmentation image type)
        File pageXMLPreprocDir = new File(projConf.getImageDirectoryByType(segmentationImageType));
        File[] pageXMLFiles = pageXMLPreprocDir.listFiles((d, name) -> name.endsWith(projConf.CONF_EXT));
        int pageXMLCount = pageXMLFiles.length;
        int copiedPageXMLFiles = 0;
        for (File xml : pageXMLFiles) {
            if (stop == true) 
                break;

            Files.copy(Paths.get(xml.getPath()),
                Paths.get(projConf.getImageDirectoryByType("OCR") + xml.getName()),
                StandardCopyOption.valueOf("REPLACE_EXISTING"));

            copiedPageXMLFiles++;
            progress = (int) ((double) copiedPageXMLFiles / pageXMLCount * 100);
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
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        stop = true;
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses) {
        return ProcessConflictDetector.segmentationLarexConflict(currentProcesses);
    }
}
