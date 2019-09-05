package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.w3c.dom.Document;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.pageXML.PageXMLWriter;
import de.uniwue.feature.pageXML.PageConverter;

public class SegmentationImportHelper {
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
     */
    public SegmentationImportHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
    }

    /**
     * Moves the extracted files of the segmentation process to the OCR project folder
     *
     * @param sourceFilePath path to source file
     * @param destFilePath path to output file
     * @throws IOException
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public void execute(String sourceFilePath, String destFilePath) throws IOException, ParserConfigurationException, TransformerException {
        progress = 0;

        PageConverter converter = new PageConverter();
        converter.run(projConf.ORIG_IMG_DIR+"DemoImage1_ABBYY_Basic.xml",projConf.PREPROC_DIR+"0001.xml");
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
     * NOTE: uses the same basic function from the Dummy Segmentation, because conflicts would be the same
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses, boolean inProcessFlow) {
        return ProcessConflictDetector.segmentationDummyConflict(currentProcesses, inProcessFlow);
    }

    public int countPages(String filepath) {
        int pageCounter = 0;
        try {
            // Execute command
            String command = "grep '/page' " + filepath + " | wc -l";
            Process process = Runtime.getRuntime().exec(command);

            // Get the input stream and read from it
            InputStream in = process.getInputStream();
            pageCounter = (int) in.read();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pageCounter;
    }
}
