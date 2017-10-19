package de.uniwue.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectDirConfig;

/**
 * Helper class for generic controller
 */
public class GenericHelper {
    /**
     * Object to access project directory configuration
     */
    private ProjectDirConfig projDirConf;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
    public GenericHelper(String projectDir) {
        projDirConf = new ProjectDirConfig(projectDir);
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

        Files.walk(Paths.get(projDirConf.getImagePathByType(imageType)))
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(fileEntry -> fileEntry.getName().endsWith(projDirConf.IMG_EXT))
        .sorted()
        .forEach(
            fileEntry -> { pageList.add(FilenameUtils.removeExtension(fileEntry.getName())); }
        );

        return pageList;
    }
}
