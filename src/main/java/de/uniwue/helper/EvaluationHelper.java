package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;

public class EvaluationHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Object to use generic functionalities
     */
    private GenericHelper genericHelper;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Evaluation process
     */
    private int progress = -1;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (binary, gray)
     */
    public EvaluationHelper(String projectDir, String projectImageType) {
        projConf = new ProjectConfiguration(projectDir);
        processHandler = new ProcessHandler();
        procStateCol = new ProcessStateCollector(projConf, projectImageType);
        genericHelper = new GenericHelper(projConf);
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
     * Executes image Evaluation of all pages
     * Achieved with the help of the external python program "calamari-eval"
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "calamari-eval"
     * @throws IOException
     */
    public void execute(List<String> pageIds, List<String> cmdArgs) throws IOException {
        progress = 0;

        List<String> command = new ArrayList<>();

        for(String pageId : pageIds) {
            if(procStateCol.groundTruthState(pageId)) {
                command.add(new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT).getAbsolutePath());
            }
        }

        progress = 20;
        command.addAll(cmdArgs);

        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("calamari-eval-helper", command, false);

        progress = 100;
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
        if (processHandler != null)
            processHandler.stopProcess();
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Returns the Ids of the pages, for which recognition process was already executed
     *
     * @return List of valid page Ids
     * @throws IOException 
     */
    public ArrayList<String> getValidPageIds() throws IOException {
        // Get all pages and check which one were already recognized
        ArrayList<String> validPageIds = new ArrayList<String>();
        ArrayList<String> allPageIds = genericHelper.getPageList("Original");

		for(String pageId : allPageIds) {
			if(procStateCol.groundTruthState(pageId)) {
				validPageIds.add(pageId);
			}
		}
        Collections.sort(validPageIds);
        return validPageIds;
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses) {
        return ProcessConflictDetector.evaluationConflict(currentProcesses);
    }
}
