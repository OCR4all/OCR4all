package de.uniwue.helper;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessStateCollector;

/**
 * Helper class for processflow pages
 */
public class ProcessFlowHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Image type of the project
     * Possible values: { Binary, Gray }
     */
    private String imageType;

    /**
     * Constructor
     *
     * @param projectDir Absolute path to the project
     * @param imageType Image type of the project
     */
    public ProcessFlowHelper(String projectDir, String imageType) {
        this.projConf = new ProjectConfiguration(projectDir);
        this.imageType  = imageType;
    }

    /**
     * Identifies pages for which the given status was successfully processed
     *
     * @param initialPageIds Page identifiers that should be checked
     * @param checkProcess Process status that should be checked for given page identifiers
     * @return Page identifiers that were successfully processed
     */
    public String[] getValidPageIds(String[] initialPageIds, String checkProcess) {
        // Needed to determine process states
        ProcessStateCollector procStateCol = new ProcessStateCollector(projConf, imageType);

        // Verify state of each page for the given process and store the successful ones
        Set<String> processedPages = new TreeSet<String>();
        for (String pageId : initialPageIds) {
            switch(checkProcess) {
                case "preprocessing":     if (procStateCol.preprocessingState(pageId)) processedPages.add(pageId); break;
                case "despeckling":       if (procStateCol.despecklingState(pageId)) processedPages.add(pageId); break;
                case "segmentation":      if (procStateCol.segmentationState(pageId)) processedPages.add(pageId); break;
                case "lineSegmentation":  if (procStateCol.lineSegmentationState(pageId)) processedPages.add(pageId); break;
                case "recognition":       if (procStateCol.recognitionState(pageId)) processedPages.add(pageId); break;
                default: break;
            }
        }

        // Convert to required data type
        return processedPages.toArray(new String[processedPages.size()]);
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses) {
        return ProcessConflictDetector.processFlowConflict(currentProcesses);
    }
}
