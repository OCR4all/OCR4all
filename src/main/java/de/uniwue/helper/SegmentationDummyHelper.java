package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.w3c.dom.Document;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessStateCollector;
import de.uniwue.feature.pageXML.PageXMLWriter;

public class SegmentationDummyHelper {
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
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Status of the SegmentationLarex progress
     */
    private int progress = -1;

    /**
     * Indicates if the process should be cancelled
     */
    private boolean stop = false;

    /**
     * Indicates if a SegmentationLarex process is already running
     */
    private boolean segmentationRunning = false;


    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (binary,gray)
     */
    public SegmentationDummyHelper(String projDir, String projectImageType) {
        this.projectImageType = projectImageType;
        projConf = new ProjectConfiguration(projDir);
        procStateCol = new ProcessStateCollector(projConf, projectImageType);
    }

    /**
     * Moves the extracted files of the segmentation process to the OCR project folder
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param segmentationImageType Image type of the segmentation (binary, despeckled)
     * @throws IOException
     */
    public void extractXmlFiles(List<String> pageIds, String segmentationImageType) throws IOException {
        segmentationRunning = true;
        stop = false;
        progress = 0;
        
        File ocrDir = new File(projConf.OCR_DIR);
        if (!ocrDir.exists())
            ocrDir.mkdir();

        SegmentationHelper segmentationHelper = new SegmentationHelper(projConf.PROJECT_DIR, this.projectImageType);
        segmentationHelper.deleteOldFiles(pageIds);

        // Copy process specific images (based on project image type)
        File projectSpecificPreprocDir = new File(projConf.getImageDirectoryByType(projectImageType));
        if (projectSpecificPreprocDir.exists()){
            File[] filesToMove = projectSpecificPreprocDir.listFiles((d, name) -> name.endsWith(projConf.IMG_EXT));
            for (File file : filesToMove) {
                if (pageIds.contains(FilenameUtils.removeExtension(file.getName())) && stop == false) {
                    Files.copy(Paths.get(file.getPath()),
                        Paths.get(projConf.getImageDirectoryByType("OCR") + file.getName()),
                        StandardCopyOption.valueOf("REPLACE_EXISTING"));
                }
            }
        }
        int processedPages = 0;
        // generates XML files for each page
        File segmentationTypeDir = new File(projConf.getImageDirectoryByType(segmentationImageType));
        if (segmentationTypeDir.exists()){
            File[] imageFiles = segmentationTypeDir.listFiles((d, name) -> name.endsWith(projConf.IMG_EXT));
            for (File file : imageFiles) {
                if (pageIds.contains(FilenameUtils.removeExtension(file.getName())) && stop == false) {
                    extractXML(file,projConf.OCR_DIR);
                    progress = (int) ((double) processedPages / pageIds.size() * 100);
                }
            }
        }
        progress = 100;
        segmentationRunning = false;
    }

    public void extractXML(File file, String outputFolder) {
        String imagePath = file.getAbsolutePath();
        String imageFilename = imagePath.substring(imagePath.lastIndexOf(File.separator) + 1);
        Mat image = Imgcodecs.imread(imagePath);
        if(image.width() == 0)
            return;
        Document xml = PageXMLWriter.getPageXML(image, imageFilename, "");
        PageXMLWriter.saveDocument(xml, imageFilename, outputFolder);
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
     * Gets the SegmentationLarex status
     *
     * @return status if the process is running
     */
    public boolean isSegmentationRunning() {
        return segmentationRunning;
    }
}
