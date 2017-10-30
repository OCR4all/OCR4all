package de.uniwue.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectConfiguration;

/**
 * Helper class for generic controller
 */
public class GenericHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public GenericHelper(String projectDir) {
        projConf = new ProjectConfiguration(projectDir);
    }

    /**
     * Gets all page IDs of the project
     *
     * @param imageType Type of the images
     * @return Array of page IDs
     * @throws IOException
     */
    public ArrayList<String> getPageList(String imageType) throws IOException {
        ArrayList<String> pageList = new ArrayList<String>();
        // File depth of 1 -> no recursive (file)listing 
        Files.walk(Paths.get(projConf.getImageDirectoryByType(imageType)), 1)
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
        .sorted()
        .forEach(
            fileEntry -> { pageList.add(FilenameUtils.removeExtension(fileEntry.getName())); }
        );

        return pageList;
    }
}
