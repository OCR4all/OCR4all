package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import de.uniwue.config.ProjectConfiguration;

public class SegmentationHelper {

    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Status of the progress
     */
    private int progress = -1;

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
     * @param projectype type of the project (binary, despeckled)
     * @throws IOException
     */
    public void MoveExtractedSegments(String projectype) throws IOException {
        progress = 0;
        File preprocDir = new File(projConf.getImageDirectoryByType(projectype));
        File[] filesToMove = preprocDir.listFiles((d, name) -> name.endsWith(".xml"));
        int count_xml_dir = filesToMove.length;
        int i = 1;
        for (File xml : filesToMove) {
            progress = (int) ((double) i / count_xml_dir * 100);
            Files.copy(Paths.get(xml.getPath()), Paths.get(projConf.getImageDirectoryByType("OCR") + xml.getName()),StandardCopyOption.valueOf("REPLACE_EXISTING"));
            i++;
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
}
