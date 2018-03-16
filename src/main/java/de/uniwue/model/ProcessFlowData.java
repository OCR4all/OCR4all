package de.uniwue.model;

import java.util.List;
import java.util.Map;

/**
 * Represents the necessary data for Process Flow execution
 */
public class ProcessFlowData {
    /**
     * Identifiers of the pages (e.g 0002,0003)
     */
    private String[] pageIds;

    /**
     * Names of the processes that should be executed
     */
    private List<String> processesToExecute;

    /**
     * Settings for each process
     */
    private Map<String, Map<String, Object>> processSettings;

    /**
     * Gets the page identifiers
     *
     * @return Array of pageIds
     */
    public String[] getPageIds() {
        return pageIds;
    }

    /**
     * Sets the page identifiers
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     */
    public void setPageIds(String[] pageIds) {
        this.pageIds = pageIds;
    }

    /**
     * Gets the processes that should be executed
     *
     * @return List of process names
     */
    public List<String> getProcessesToExecute() {
        return processesToExecute;
    }

    /**
     * Sets the processes that should be executed
     *
     * @param processesToExecute Names of the processes that should be executed
     */
    public void setProcessesToExecute(List<String> processesToExecute) {
        this.processesToExecute = processesToExecute;
    }

    /**
     * Gets the process settings as Map. Example:
     * {
     *     "preprocessing" : { "cmdArgs" : ["--nocheck", "--parallel", "8"] },
     *     "despeckling"   : { "maxContourRemovalSize" : 100 }
     * }
     *
     * @return Map of process settings
     */
    public Map<String, Map<String, Object>> getProcessSettings() {
        return processSettings;
    }

    /**
     * Sets the process settings
     *
     * @param processSettings Settings for each process
     */
    public void setProcessSettings(Map<String, Map<String, Object>> processSettings) {
        this.processSettings = processSettings;
    }
}
