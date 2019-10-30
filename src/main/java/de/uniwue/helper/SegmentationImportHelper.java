package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    private boolean stopProcess = false;

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
        renameFiles();
        PageConverter converter = new PageConverter();

        ArrayList<File> xmlFiles = new ArrayList<File>();
        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.ORIG_IMG_DIR), 1)
                .map(Path::toFile)
                .filter(fileEntry -> fileEntry.isFile())
                .filter(fileEntry -> fileEntry.getName().endsWith(projConf.CONF_EXT))
                .filter(fileEntry -> fileEntry.getName().matches("[0-9]{4}"+projConf.CONF_EXT))
                .sorted()
                .forEach(
                        fileEntry -> { xmlFiles.add(fileEntry); }
                );

        for (File xmlFile : xmlFiles) {
            System.out.println(xmlFile.getName());
            System.out.println(xmlFile.getAbsoluteFile());
            converter.run(xmlFile.getAbsolutePath(), projConf.PREPROC_DIR+xmlFile.getName());
        }
        //converter.run(sourceFilePath,projConf.PREPROC_DIR+"0001.xml");
        progress = 100;
    }

    /**
     * Renames all files in the 'original' folder to names that consists of an ascending number of digits (e.g 0001, 0002 ...)
     *
     * @throws IOException
     */
    public void renameFiles() throws IOException {
        if (stopProcess == true)
            return;
        ArrayList<File> xmlFiles = new ArrayList<File>();
        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.ORIG_IMG_DIR), 1)
                .map(Path::toFile)
                .filter(fileEntry -> fileEntry.isFile())
                .filter(fileEntry -> fileEntry.getName().endsWith(projConf.CONF_EXT))
                .sorted()
                .forEach(
                        fileEntry -> { xmlFiles.add(fileEntry); }
                );

        int minimumFormatLength = String.valueOf(xmlFiles.size()).length();
        // File names must consist of at least four digits
        if (minimumFormatLength < projConf.minimumNameLength)
            minimumFormatLength = projConf.minimumNameLength;

        // Build formatting possibility
        String format = "";
        for (int i = 1; i <= minimumFormatLength; i++)
            format = format + 0;
        DecimalFormat df = new DecimalFormat(format);

        int formattingCounter = 1;
        for (File file : xmlFiles) {
            if (stopProcess == true)
                return;

            if (!file.getName().equals(projConf.ORIG_IMG_DIR + df.format(formattingCounter) + projConf.CONF_EXT)) {
                //file.renameTo(new File(projConf.ORIG_IMG_DIR + df.format(formattingCounter) + projConf.CONF_EXT));
                File validFile = file;
                validFile.renameTo(new File(projConf.ORIG_IMG_DIR + df.format(formattingCounter) + projConf.CONF_EXT));
                validFile.createNewFile();
            }
            formattingCounter++;
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
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        stopProcess = true;
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
