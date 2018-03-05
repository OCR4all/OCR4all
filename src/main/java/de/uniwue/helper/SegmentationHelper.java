package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
     */
    public SegmentationHelper(String projConf) {
        this.projConf = new ProjectConfiguration(projConf);
    }

    /**
     * Moves the extracted files of the segmentation process to the OCR project folder
     *
     * @param projectype type of the project (binary, despeckled)
     * @throws IOException
     */
    public void MoveExtractedSegments(String projectype) throws IOException {
        segmentationRunning = true;
        stop = false;

        File ocrDir = new File(projConf.OCR_DIR);
        if (!ocrDir.exists())
            ocrDir.mkdir();

        progress = 0;

        File preprocDir = new File(projConf.getImageDirectoryByType(projectype));
        File[] filesToMove = preprocDir.listFiles((d, name) -> name.endsWith(".xml"));
        int count_xml_dir = filesToMove.length;
        int i = 1;
        for (File xml : filesToMove) {
            if (stop == true) 
                break;

            progress = (int) ((double) i / count_xml_dir * 100);
            Files.copy(Paths.get(xml.getPath()), Paths.get(projConf.getImageDirectoryByType("OCR") + xml.getName()),StandardCopyOption.valueOf("REPLACE_EXISTING"));
            i++;
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
}
