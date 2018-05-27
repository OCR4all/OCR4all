package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
     */
    public TrainingHelper(String projectDir, String projectImageType) {
        projConf = new ProjectConfiguration(projectDir);
        processHandler = new ProcessHandler();
        this.projectImageType = projectImageType;
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
        // Add custom models to map
        Files.walk(Paths.get(projConf.PAGE_DIR))
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.getName().endsWith(projConf.GT_EXT))
        .forEach(
            fileEntry->{
                if(new File(fileEntry.getAbsolutePath().replace(projConf.GT_EXT, projConf.getImageExtensionByType(projectImageType))).exists())
                    imagesWithGt.add(fileEntry.getAbsolutePath().replace(projConf.GT_EXT, projConf.getImageExtensionByType(projectImageType)));
        });
        return imagesWithGt;
    }

    /**
     * Executes image training
     * Achieved with the help of the external python program  calamari-cross-fold-train"
     *
     * @param cmdArgs Command line arguments for "calamari-cross-fold-train"
     * @throws IOException
     */
    public void execute(List<String> cmdArgs) throws IOException {
        trainingRunning = true;

        List<String> command = new ArrayList<String>();
        command.add("--files");
        for (String gtImagePath : getImagesWithGt(projectImageType)) {
            command.add(gtImagePath);
        }
        command.addAll(cmdArgs);
        command.add("--best_models_dir");
        command.add(ProjectConfiguration.PROJ_MODEL_CUSTOM_DIR);
        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("calamari-cross-fold-train", command, false);

        getProgress();
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
