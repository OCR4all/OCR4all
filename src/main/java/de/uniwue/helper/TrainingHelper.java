package de.uniwue.helper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;

/**
 * Helper class for training, which also calls the calamari-cross-fold-train program 
 */
public class TrainingHelper {
    /**
     * Image Type of the Project
     */
    private String projectImageType;

    /**
     * Processing structure of the project
     * Possible values: { Directory, Pagexml }
     */
    private String processingMode;
    
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Training process
     */
    private int progress = -1;

    /**
     * Indicates if a Training process is already running
     */
    private boolean trainingRunning = false;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (binary, gray)
     * @param processingMode Processing structure of the project (Directory, Pagexml)
     */
    public TrainingHelper(String projectDir, String projectImageType, String processingMode) {
        projConf = new ProjectConfiguration(projectDir);
        processHandler = new ProcessHandler();
        this.projectImageType = projectImageType;
        this.processingMode = processingMode;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        if (trainingRunning == false)
            return progress;
        // TODO
        return 0;
     }

    /**
     * Gets the process handler object
     *
     * @return Returns the process Helper
     */
    public ProcessHandler getProcessHandler() {
        return processHandler;
    }

    /** Lists all images that have an corresponding gt file
     * 
     * @param projectImageType
     * @return
     * @throws IOException
     */
    public List<String> getImagesWithGt(String projectImageType) throws IOException {
        ArrayList<String> imagesWithGt = new ArrayList<String>();
        final String gtExt = processingMode.equals("Pagexml") ? projConf.CONF_EXT : projConf.GT_EXT;

        // Add custom models to map
		Files.walk(Paths.get(projConf.PAGE_DIR))
		.map(Path::toFile)
		.filter(fileEntry -> fileEntry.getName().endsWith(gtExt))
		.forEach(
			fileEntry -> {
				final String imageFile = fileEntry.getAbsolutePath().replace(gtExt, projConf.getImageExtensionByType(projectImageType));
				if (new File(imageFile).exists())
					imagesWithGt.add(imageFile);
		});
        return imagesWithGt;
    }

    /**
     * Finds next free training identifier
     * Only necessary if no identifier is specified by the user
     *
     * The directory structure is defined by following structure:
     * PROJ_MODEL_CUSTOM_DIR/PROJECT_NAME/TRAINING_ID
     *
     * The identifier is an incremented Integer starting at 0
     * This function finds the next highest Integer value through directories
     *
     * @param projectModelDir Directory that holds the project specific models
     * @return Next training identifier to use
     */
    public String getNextTrainingId(File projectModelDir) {
        File[] trainingDirectories = projectModelDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        // Find all values of directories with Integer naming
        List<Integer> trainingIds = new ArrayList<Integer>();
        trainingIds.add(-1);
        for (File trainingDir : trainingDirectories) {
			// Filter out empty directories
			if(trainingDir.list().length > 0) {
				try {
					trainingIds.add(Integer.parseInt(trainingDir.getName()));
				} catch (NumberFormatException e) {
					// Ignore directories that have no Integer naming (irrelevant)
				}
			}
        }

		return Integer.toString(Collections.max(trainingIds) + 1);
    }

    /**
     * Executes image training
     * Achieved with the help of the external python program  calamari-cross-fold-train"
     *
     * @param cmdArgs Command line arguments for "calamari-cross-fold-train"
     * @param projectName Name of the project that is currently loaded
     * @param trainingId Custom identifier to name the training directory
     * @throws IOException
     */
    public void execute(List<String> cmdArgs, String projectName, String trainingId) throws IOException {
        trainingRunning = true;
        progress = 0;

        // Create project specific model directory if not exists
        File projectModelDir = new File(ProjectConfiguration.PROJ_MODEL_CUSTOM_DIR + File.separator + projectName);
        if (!projectModelDir.exists())
            projectModelDir.mkdir();

        if (trainingId.isEmpty())
            trainingId = getNextTrainingId(projectModelDir);

        deleteOldFiles(projectName, trainingId);

        // Create training specific directory if not exists
        File trainingDir = new File(projectModelDir.getAbsolutePath() +  File.separator + trainingId);
        if (!trainingDir.exists())
            trainingDir.mkdir();

        List<String> command = new ArrayList<String>();
        command.add("--files");
        // Create temp json file with all segment images (to not overload parameter list)
		// Temp file in a temp folder named "calamari-<random numbers>.json"
        File segmentListFile = File.createTempFile("calamari-",".json");
        segmentListFile.deleteOnExit(); // Delete if OCR4all terminates
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode gtList = mapper.createArrayNode();
        for (String gtFile : getImagesWithGt(projectImageType)) {
            // Add affected line segment images with their absolute path to the json file
        	gtList.add(gtFile);
        }
        ObjectNode segmentObj = mapper.createObjectNode();
        segmentObj.set("files", gtList);
        ObjectWriter writer = mapper.writer();
        writer.writeValue(segmentListFile, segmentObj); 
        command.add(segmentListFile.toString());
        System.out.println(segmentListFile);
        
        
        command.addAll(cmdArgs);
        command.add("--best_models_dir");
        command.add(trainingDir.getAbsolutePath());

        command.add("--no_progress_bars");

        if(processingMode.equals("Pagexml")) {
        	command.add("--dataset");
        	command.add("PAGEXML");
        }

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("calamari-cross-fold-train", command, false);

        trainingRunning = false;
        progress = 100;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        trainingRunning = false;
        progress = -1;
    }

    /** Checks if process related files already exist
     * 
     * @param projectName
     * @param trainingId
     * @return
     */
    public boolean doOldFilesExist(String projectName, String trainingId) {
        File projectModelDir = new File(ProjectConfiguration.PROJ_MODEL_CUSTOM_DIR + File.separator + projectName + File.separator + trainingId);
        if (projectModelDir.exists())
            return true;

        return false;
    }

    /**
     * Deletion of old process related files
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void deleteOldFiles(String projectName, String trainingId) throws IOException {
        File projectModelDir = new File(ProjectConfiguration.PROJ_MODEL_CUSTOM_DIR + File.separator + projectName + File.separator + trainingId);
        if (projectModelDir.exists())
            FileUtils.deleteDirectory(projectModelDir);
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
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses, boolean inProcessFlow) {
        return ProcessConflictDetector.trainingConflict(currentProcesses, inProcessFlow);
    }
}
