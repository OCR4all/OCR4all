package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;

/**
 * Helper class for segmentating pages, which also calls the pixel-classifier program 
 */
public class SegmentationPixelClassifierHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Image Type of the Project
     */
    private String projectImageType;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Segmentation process
     */
    private int progress = -1;

    /**
     * Indicates if a Segmentation process is already running
     */
    private boolean SegmentationRunning = false;

    /**
     * Structure to monitor the progress of the process
     * pageId : SegmentationState
     *
     * Structure example:
     * {
     *     "0002": true,
     *     "0003 : false,
     *     ...
     * }
     */
    private TreeMap<String, Boolean> processState = new TreeMap<String, Boolean>();

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (binary, gray)

     */
    public SegmentationPixelClassifierHelper(String projectDir, String projectImageType) {
        projConf = new ProjectConfiguration(projectDir);
        procStateCol = new ProcessStateCollector(projConf, projectImageType);
        processHandler = new ProcessHandler();
        this.projectImageType = projectImageType;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        // Prevent function from calculation progress if process is not running
        if (SegmentationRunning == false)
            return progress;

        int imageCount = 0;
        int processedImageCount = 0;
        // Identify how many segments are already processed
        for (String pageId : processState.keySet()) {
            imageCount += 1;
            if (processState.get(pageId) == true) {
                processedImageCount += 1;
                continue;
            }
            File XMLFile = new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
            File ImageFile = new File(projConf.OCR_DIR + pageId + projConf.IMG_EXT);
            if (XMLFile.exists() && ImageFile.exists()) {
                processState.put(pageId, true);
            }
        }
        return (progress != 100) ? (int) ((double) processedImageCount / imageCount * 100) : 100;
     }

    /**
     * Gets the process handler object
     *
     * @return Returns the process Helper
     */
    public ProcessHandler getProcessHandler() {
        return processHandler;
    }

    /**
     * Initializes the structure with which the progress of the process can be monitored
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void initializeProcessState(List<String> pageIds) throws IOException {
        // Initialize the status structure
        processState = new TreeMap<String, Boolean>();
        for(String pageId : pageIds) {
            processState.put(pageId, false);
        }
    }

    /**
     * Executes image segmentation of all pages
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "TODO"
     * @throws IOException
     */
    public void execute(List<String> pageIds, List<String> cmdArgs, String segmentationImageType, String modelId) throws IOException {
        SegmentationRunning = true;

        progress = 0;

        initializeProcessState(pageIds);
        // TODO : Run pixel classifier script with passed arguments

        String imageExt = projConf.getImageExtensionByType(segmentationImageType);
        String pagePathFormat = projConf.getImageDirectoryByType(segmentationImageType) + "%s" + imageExt;

        List<String> command = new ArrayList<>();
        command.add("find-segments");
        command.add("--model");
        command.add(listModels().get(modelId));
        command.add("--strip-extension");
        command.add(imageExt);
        command.add("-b");
        pageIds.stream().map(pageId -> String.format(pagePathFormat, pageId)).forEachOrdered(command::add);
        command.addAll(cmdArgs);

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocr4all-pixel-classifier", command, false);

        getProgress();
        SegmentationRunning = false;
        progress = 100;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        SegmentationRunning = false;
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        if (processHandler != null)
            processHandler.stopProcess();
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses, boolean inProcessFlow) {
        return ProcessConflictDetector.segmentationPixelClassifierConflict(currentProcesses, inProcessFlow );
    }

    /**
     * Lists all available Models from the model directory
     *
     * Model location directory:
     * ProjectConfiguration.PROJ_MODEL_PIXELCLASSIFIER_DIR
     *
     * Example: /var/ocr4all/models/pixelclassifier/default.h5
     * Display: default
     * Example: /var/ocr4all/models/pixelclassifier/book/foo.h5
     * Display: book/foo
     *
     * @return Map of models (key = modelName | value = path)
     * @throws IOException
     */
    public static TreeMap<String, String> listModels() throws IOException{
        TreeMap<String, String> models = new TreeMap<>();

        File modelsDir = new File(ProjectConfiguration.PROJ_MODEL_PIXELCLASSIFIER_DIR);
        if (!modelsDir.exists())
            return models;

        // Add all models to map (follow symbolic links on the filesystem due to Docker container)
        Files.walk(Paths.get(ProjectConfiguration.PROJ_MODEL_PIXELCLASSIFIER_DIR), FileVisitOption.FOLLOW_LINKS)
                .filter(path -> path.toString().toLowerCase().endsWith(ProjectConfiguration.TENSORFLOW_MODEL_EXT))
                .forEach(
                        modelPath -> {
                            // Remove OS path and model extension from display string (only display significant information)
                            String modelPathAbs= modelPath.toAbsolutePath().toString();
                            String modelName = modelPathAbs
                                    .replace(ProjectConfiguration.PROJ_MODEL_PIXELCLASSIFIER_DIR, "")
                                    .replace(ProjectConfiguration.TENSORFLOW_MODEL_EXT, "");
                            models.put(modelName, modelPathAbs);
                        });

        return models;
    }
}
