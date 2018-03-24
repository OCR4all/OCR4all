package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectConfiguration;

/**
 * Helper class for segmentation module
 */
public class SegmentationHelper {
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
     * Status of the Segmentation progress
     */
    private int progress = -1;

    /**
     * Indicates if the process should be cancelled
     */
    private boolean stop = false;

    /**
     * Indicates if a Segmentation process is already running
     */
    private boolean segmentationRunning = false;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (binary,gray)
     */
    public SegmentationHelper(String projDir, String projectImageType) {
        this.projectImageType = projectImageType;
        this.projConf = new ProjectConfiguration(projDir);
    }

    /**
     * Moves the extracted files of the segmentation process to the OCR project folder
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param segmentationImageType Image type of the segmentation (binary, despeckled)
     * @throws IOException
     */
    public void moveExtractedSegments(List<String> pageIds, String segmentationImageType) throws IOException {
        segmentationRunning = true;
        stop = false;
        progress = 0;

        File ocrDir = new File(projConf.OCR_DIR);
        if (!ocrDir.exists())
            ocrDir.mkdir();

        deleteOldFiles(pageIds);

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
        segmentationRunning = false;
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
        segmentationRunning = false;
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        stop = true;
    }

    /**
     * Gets the Segmentation status
     *
     * @return status if the process is running
     */
    public boolean isSegmentationRunning() {
        return segmentationRunning;
    }

    /**
     * Deletion of old process related files
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void deleteOldFiles(List<String> pageIds) throws IOException {
        if (!new File(projConf.OCR_DIR).exists())
            return;

        // Delete all files created by subsequent processes to preserve data integrity
        RegionExtractionHelper regionExtracorHelper = new RegionExtractionHelper(projConf.PROJECT_DIR, this.projectImageType);
        regionExtracorHelper.deleteOldFiles(pageIds);

        // Delete image and PageXML files
        for (String pageId : pageIds) {
            File segPng = new File(projConf.OCR_DIR + pageId + projConf.IMG_EXT);
            if (segPng.exists())
                segPng.delete();

            File segXml = new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
            if (segXml.exists())
                segXml.delete();
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
            // Check for image and PageXML files
            if (new File(projConf.OCR_DIR + pageId + projConf.IMG_EXT).exists())
                return true;
            if (new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT).exists())
                return true;
        }
        return false;
    }
}
