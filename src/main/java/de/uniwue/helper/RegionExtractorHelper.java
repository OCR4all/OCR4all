package de.uniwue.helper;

import de.uniwue.config.ProjectConfiguration;

public class RegionExtractorHelper {

    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public RegionExtractorHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
    }
}
