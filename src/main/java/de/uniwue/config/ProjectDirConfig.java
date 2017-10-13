package de.uniwue.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Configuration class for project directory specific parameters
 */
public final class ProjectDirConfig {
    /**
     * Constructor
     *
     * @param projectDir  Absolute path to the project directory
     */
    public ProjectDirConfig(String projectDir) {
        // Put project directory in front of all directory paths
        // This changes all paths from relative to absolute
        Field[] classVars = this.getClass().getDeclaredFields();
        for (Field classVar : classVars) {
            if (!Modifier.isFinal(classVar.getModifiers())) {
                try {
                    classVar.set(this, projectDir + File.separator + classVar.get(this));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // Path will be invalid and controller will be responsible for error handling
                }
            }
        }
    }

    /**
     * Constructor (only final variables contain the correct values)
     */
    public ProjectDirConfig() { }

    /**** Extensions ****/

    /**
     * Default image extension of the project
     */
    public final String IMG_EXT = ".png";
    /**
     * Binary image extension of the project
     */
    public final String BIN_IMG_EXT  = ".bin" + IMG_EXT;
    /**
     * Normalized gray image extension of the project
     */
    public final String GRAY_IMG_EXT = ".nrm" + IMG_EXT;
    /**
     * Default configuration extension of the project
     */
    public final String CONF_EXT = ".xml";

    /**** Original directories ****/

    /**
     * Absolute path to original page images (is made absolute in Constructor)
     */
    public String ORIG_IMG_DIR = "Original" + File.separator;
    /**
     * Absolute path to the project dir (is made absolute in Constructor)
     */
    public String PROJECT_DIR ="";

    /**** Preprocessing directories ****/

    /**
     * Absolute path to preprocessing directory (is made absolute in Constructor)
     */
    public String PREPROC_DIR = "PreProc" + File.separator;
    /**
     * Absolute path to preprocessed binary images (is made absolute in Constructor)
     */
    public String BINR_IMG_DIR = PREPROC_DIR + "Binary" + File.separator;
    /**
     * Absolute path to preprocessed gray images (is made absolute in Constructor)
     */
    public String GRAY_IMG_DIR = PREPROC_DIR + "Gray" + File.separator;
    /**
     * Absolute path to preprocessed despeckled images (is made absolute in Constructor)
     */
    public String DESP_IMG_DIR = PREPROC_DIR + "Despeckled" + File.separator;

    /**** OCR directories ****/

    /**
     * Absolute path to OCR directory (is made absolute in Constructor)
     */
    public String OCR_DIR = "OCR" + File.separator;
    /**
     * Absolute path to OCR pages directory (is made absolute in Constructor)
     */
    public String PAGE_DIR  = OCR_DIR + "Pages" + File.separator;
    /**
     * Absolute path to OCR model directory (is made absolute in Constructor)
     */
    public String MODEL_DIR = OCR_DIR + "Models" + File.separator;
    /**
     * Absolute path to OCR line directory (is made absolute in Constructor)
     */
    public String LINE_DIR  = OCR_DIR + "Lines" + File.separator;
}
