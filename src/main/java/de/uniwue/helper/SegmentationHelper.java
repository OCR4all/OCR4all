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
    public SegmentationHelper(String projDir) {
        this.projConf = new ProjectConfiguration(projDir);
    }

    /**
     * Moves the extracted files of the segmentation process to the OCR project folder
     *
     * @param pageIds Ids of specified pages
     * @param segmentationImageType type of the project (binary, despeckled)
     * @param projectImageType (gray, binary)
     * @param replace If true, replaces the existing image files
     * @throws IOException
     */
    public void MoveExtractedSegments(List<String> pageIds, String segmentationImageType, String projectImageType, boolean replace) throws IOException {
        segmentationRunning = true;
        stop = false;
        progress = 0;

        File ocrDir = new File(projConf.OCR_DIR);
        if (!ocrDir.exists())
            ocrDir.mkdir();
        deleteOldFiles( pageIds);

        //copy process specific images
        File ProjectTypePreprocessDir = new File(projConf.getImageDirectoryByType(projectImageType));
        if(ProjectTypePreprocessDir.exists()){
            File[] filesToMove = ProjectTypePreprocessDir.listFiles((d, name) -> name.endsWith(projConf.IMG_EXT));
            for(File file : filesToMove) {
                if(pageIds.contains(FilenameUtils.removeExtension(file.getName()))) {
                    if(replace)
                        Files.copy(Paths.get(file.getPath()), Paths.get(projConf.getImageDirectoryByType("OCR") + file.getName()),StandardCopyOption.valueOf("REPLACE_EXISTING"));
                    else {
                        if(!new File(projConf.getImageDirectoryByType("OCR") + file.getName()).exists())
                            Files.copy(Paths.get(file.getPath()), Paths.get(projConf.getImageDirectoryByType("OCR") + file.getName()));
                    }
                }
            }
        }

        File preprocDir = new File(projConf.getImageDirectoryByType(segmentationImageType));
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
     * @param pageIds
     * @throws IOException 
     */
    public void deleteOldFiles(List<String> pageIds) throws IOException {
        if(!new File(projConf.OCR_DIR).exists())
            return;
        RegionExtractionHelper regionExtracorHelper = new RegionExtractionHelper(projConf.PROJECT_DIR);
        regionExtracorHelper.deleteOldFiles(pageIds);
        for(String pageId : pageIds) {
            File segPng = new File(projConf.OCR_DIR + pageId + projConf.IMG_EXT);
            File segXml = new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
            if(segPng.exists())
                segPng.delete();
            if(segXml.exists())
                segXml.delete();
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
            if(new File(projConf.OCR_DIR + page + projConf.CONF_EXT).exists()) {
                exists = true;
                break;
            }
        }
        return exists;
    }
}
