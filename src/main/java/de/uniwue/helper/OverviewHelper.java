package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.model.PageOverview;

public class OverviewHelper {
    /**
     * Stores page overviews of the project
     *
     * Structure example:
     * {
     *     "0001.png" : {
     *         "pageId" : 0001,
     *         "preprocessed" : true,
     *         "segmented" : false,
     *         "segmentsExtracted" : false,
     *         "linesExtracted" : false,
     *         "hasGT" : false,
     *     },
     *     ...
     * }
     */
    private Map<String, PageOverview> overview = new HashMap<String, PageOverview>();

    /**
     * Image type of the project
     * Possible values: { Binary, Gray }
     */
    private String imageType;

    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Constructor
     *
     * @param pathToProject  Absolute path of the project on the filesystem
     * @param imageType  Image type of the project
     */
    public OverviewHelper(String pathToProject, String imageType) {
        this.imageType = imageType;
        this.projConf = new ProjectConfiguration(pathToProject);
    }

    /**
     * Generates project status overview for all existing pages
     *
     * @throws IOException
     */
    public void initialize() throws IOException {
        String path = projConf.ORIG_IMG_DIR;
        if (new File(path).exists()) {
            final File folder = new File(path);
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isFile()) {
                    PageOverview pOverview = new PageOverview(FilenameUtils.removeExtension(fileEntry.getName()));
                    overview.put(fileEntry.getName(), pOverview);
                }
            }
            checkPreprocessed();
            checkDespeckled();
            checkSegmented();
            checkSegmentsExtracted();
            checkLinesExtracted();
            checkHasGT();
        }
        else {
            throw new IOException("Folder does not exist!");
        }
    }

    /**
     * Generates status overview for one page
     *
     * @param pageID  Page identifier for which the overview should be generated
     * @throws IOException
     */
    public void initialize(String pageID) throws IOException {
        String path = projConf.ORIG_IMG_DIR + pageID + projConf.IMG_EXT;
        if (new File(path).exists()) {
            PageOverview pOverview = new PageOverview(FilenameUtils.removeExtension(new File(path).getName()));
            overview.put(new File(path).getName(), pOverview);
            checkPreprocessed();
            checkDespeckled();
            checkSegmented();
            checkSegmentsExtracted();
            checkLinesExtracted();
            checkHasGT();
        }
        else {
            throw new IOException("Folder does not exist!");
        }
    }

    /**
     * Validates preprocessing state and updates project overview
     */
    public void checkPreprocessed() {
        for (String key : overview.keySet()) {
            overview.get(key).setPreprocessed(true);
            if (!new File(projConf.PREPROC_DIR + imageType + File.separator + key).exists()) 
                overview.get(key).setPreprocessed(false);
        }
    }

    /**
     * Validates despeckling state and updates project overview
     */
    public void checkDespeckled() {
        for (String key : overview.keySet()) {
            overview.get(key).setDespeckled(true);
            if (!new File(projConf.DESP_IMG_DIR  + key).exists()) 
                overview.get(key).setDespeckled(false);
        }
    }

    /**
     * Validates segmented state and updates project overview
     */
    public void checkSegmented() {
        for (String key : overview.keySet()) {
            overview.get(key).setSegmented(true);
            if (!new File(projConf.OCR_DIR + overview.get(key).getPageId() + projConf.CONF_EXT).exists()) 
                overview.get(key).setSegmented(false);
        }
    }

    /**
     * Validates segment extracted state and updates project overview
     */
    public void checkSegmentsExtracted() {
        for (String key: overview.keySet()) {
            overview.get(key).setSegmentsExtracted(true);
            if (!new File(projConf.PAGE_DIR + overview.get(key).getPageId()).isDirectory())
                overview.get(key).setSegmentsExtracted(false);
        }
    }

    /**
     * Validates line extracted state and updates project overview
     */
    public void checkLinesExtracted() {
        for (String key: overview.keySet()) {
            overview.get(key).setLinesExtracted(true);

            if (overview.get(key).isSegmentsExtracted()) {
                File[] directories = new File(projConf.PAGE_DIR
                        + overview.get(key).getPageId()).listFiles(File::isDirectory);

                if (directories.length != 0) {
                    File dir = new File(directories[0].toString());
                    File[] files;
                    if (imageType.equals("Gray")) {
                        files = dir.listFiles((d, name) -> name.endsWith(projConf.GRAY_IMG_EXT));
                    }
                    else {
                        files = dir.listFiles((d, name) -> name.endsWith(projConf.BIN_IMG_EXT));
                    }

                    if (files.length == 0)
                        overview.get(key).setLinesExtracted(false);
                }
                else {
                    overview.get(key).setLinesExtracted(false);
                }
            }
            else {
                overview.get(key).setLinesExtracted(false);
            }
        }
    }

    /**
     * Validates ground truth state and updates project overview
     * TODO: Implementation. Currently no requirements specified
     */
    public void checkHasGT() {
    }

    /**
     * Generates content for one page
     * This includes its segments and their lines
     * 
     * @param pageId  Page identifier for which the content should be generated
     * @return Sorted map of page content
     */
    public Map<String, String[]> pageContent(String pageId) {
        Map<String,String[]> pageContent = new TreeMap<String, String[]>();
        if (new File(projConf.PAGE_DIR + overview.get(pageId).getPageId()).exists()) {
            File[] directories = new File(projConf.PAGE_DIR
                    + overview.get(pageId).getPageId()).listFiles(File::isDirectory);

            for (int folder = 0; folder < directories.length; folder++) {
                File dir = new File(directories[folder].toString());
                File[] files;
                int extensionLength = 0;
                if (imageType.equals("Gray")) {
                    files = dir.listFiles((d, name) -> name.endsWith(projConf.GRAY_IMG_EXT));
                    extensionLength = projConf.GRAY_IMG_EXT.length();
                }
                else {
                    files = dir.listFiles((d, name) -> name.endsWith(projConf.BIN_IMG_EXT));
                    extensionLength = projConf.BIN_IMG_EXT.length();
                }

                List<String> fileNames = new ArrayList<String>();
                for (int file = 0; file < files.length; file++) {
                    fileNames.add(FilenameUtils.getBaseName(
                            files[file].toString().substring(0, files[file].toString().length() - extensionLength)));
                }
                Collections.sort(fileNames);
                pageContent.put((directories[folder].getName()), fileNames.toArray(new String[fileNames.size()]));
            }
        }
        return pageContent;
    }

    /**
     * Gets the page overviews of the project
     *
     * @return Map of page overviews
     */
    public Map<String, PageOverview> getOverview() {
        return overview;
    }
}
