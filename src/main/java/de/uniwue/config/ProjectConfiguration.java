package de.uniwue.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Configuration class for project directory specific parameters
 */
public final class ProjectConfiguration {
    /**
     * Constructor
     *
     * @param projectDir  Absolute path to the project directory
     */
    public ProjectConfiguration(String projectDir) {
        // Put project directory in front of all directory paths
        // This changes all paths from relative to absolute
        Field[] classVars = this.getClass().getDeclaredFields();
        for (Field classVar : classVars) {
            if (!Modifier.isFinal(classVar.getModifiers())) {
                try {
                    // projectDir ends with File.seperator (is stored like this in session)
                    //classVar.set(this, projectDir + File.separator + classVar.get(this));
                    classVar.set(this, projectDir + classVar.get(this));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // Path will be invalid and controller will be responsible for error handling
                }
            }
        }
    }

    /**
     * Constructor (only final variables contain the correct values)
     */
    public ProjectConfiguration() { }

    /**** Project related directories (default paths in Docker container) ****/

    /**
     * Data directory of the project
     */
    public static final String PROJ_DATA_DIR = "/var/ocr4all/data/";
    /**
     * Model directory of the project
     */
    public static final String PROJ_MODEL_DIR = "/var/ocr4all/models/";
    /**
     * Model directory of the project containing the default models
     */
    public static final String PROJ_MODEL_DEFAULT_DIR = PROJ_MODEL_DIR + "default/";
    /**
     * Model directory of the project containing custom models
     */
    public static final String PROJ_MODEL_CUSTOM_DIR = PROJ_MODEL_DIR + "custom/";

    /**** Extensions ****/

    /**
     * Default image extension of the project
     */
    public final String IMG_EXT = ".png";
    /**
     * Allowed image extensions of the project
     */
    public final String[] CONVERT_IMG_EXTS = new String[] {".PNG", ".jpg", ".JPG", ".jpeg", ".JPEG", ".tif", ".TIF", ".bmp", ".BMP", ".ppm", ".PPM"};
    /**
     * Binary image extension of the project
     */
    public final String BINR_IMG_EXT  = ".bin" + IMG_EXT;
    /**
     * Normalized gray image extension of the project
     */
    public final String GRAY_IMG_EXT = ".nrm" + IMG_EXT;
    /**
     * Despeckled image extension of the project
     */
    public final String DESP_IMG_EXT = ".desp" + IMG_EXT;
    /**
     * Default extension of files that indicate successful line segmentation
     */
    public final String PSEG_EXT = ".pseg" + IMG_EXT;
    /**
     * Default configuration extension of the project
     */
    public final String CONF_EXT = ".xml";
    /**
     * Default recognition extension of the project
     */
    public final String REC_EXT = ".pred.txt";
    /**
     * Default ground truth extension of the project
     */
    public final String GT_EXT = ".gt.txt";
    /**
     * Default Model extension
     */
    public static final String MODEL_EXT = ".ckpt.json";
    /**
     * Returns the file extension of the given image type
     *
     * @param imageType Type of the image
     * @return Image file extension
     */
    public String getImageExtensionByType(String imageType) {
        String imageExtension = null;
        switch(imageType) {
            case "Binary": imageExtension = this.BINR_IMG_EXT; break;
            case "Gray":   imageExtension = this.GRAY_IMG_EXT; break;
            case "Despeckled": imageExtension = this.DESP_IMG_EXT; break;
            default:       imageExtension = this.IMG_EXT;      break;
        }
        return imageExtension;
    }
    /**** Filename ****/

    /**
     * Minimum name length of the file
     */
    public final int minimumNameLength = 4;

    /**** Project directory ****/

    /**
     * Absolute path to the project dir (is made absolute in Constructor)
     */
    public String PROJECT_DIR = "";

    /**** Image related directories ****/

    /**
     * Absolute path to original page images (is made absolute in Constructor)
     */
    public String ORIG_IMG_DIR = "input" + File.separator;
    /**
     * Absolute path to original page images (is made absolute in Constructor)
     */
    public String BACKUP_IMG_DIR = "Backup" + File.separator;
    /**
     * Absolute path to preprocessing directory (is made absolute in Constructor)
     */
    public String PREPROC_DIR = "processing" + File.separator;
    /**
     * Absolute path to preprocessed binary images (is made absolute in Constructor)
     */
    public String BINR_IMG_DIR = PREPROC_DIR;
    /**
     * Absolute path to preprocessed gray images (is made absolute in Constructor)
     */
    public String GRAY_IMG_DIR = PREPROC_DIR;
    /**
     * Absolute path to preprocessed despeckled images (is made absolute in Constructor)
     */
    public String DESP_IMG_DIR = PREPROC_DIR;
    /**
     * Returns the filesystem path of the given image type
     *
     * @param imageType Type of the image
     * @return Absolute filesystem path to the image
     */
    public String getImageDirectoryByType(String imageType) {
        String imagePath = null;
        switch(imageType) {
            case "Original":   imagePath = this.ORIG_IMG_DIR; break;
            case "Binary":     imagePath = this.BINR_IMG_DIR; break;
            case "Gray":       imagePath = this.GRAY_IMG_DIR; break;
            case "Despeckled": imagePath = this.DESP_IMG_DIR; break;
            case "OCR":        imagePath = this.OCR_DIR; break;
            default: break;
        }
        return imagePath;
    }

    /**** OCR related directories ****/

    /**
     * Absolute path to OCR directory (is made absolute in Constructor)
     */
    public String OCR_DIR = PREPROC_DIR;
    /**
     * Absolute path to OCR pages directory (is made absolute in Constructor)
     */
    public String PAGE_DIR  = PREPROC_DIR;
    /**
     * Absolute path to OCR model directory (is made absolute in Constructor)
     */
    public String MODEL_DIR = OCR_DIR + "Models" + File.separator;
    /**
     * Absolute path to OCR line directory (is made absolute in Constructor)
     */
    public String LINE_DIR  = OCR_DIR + "Lines" + File.separator;

    /**** Result related directories ****/

    /**
     * Absolute path to Result directory (is made absolute in Constructor)
     */
    public String RESULT_DIR = "results" + File.separator;
    /**
     * Absolute path to Result pages directory (is made absolute in Constructor)
     */
    public String RESULT_PAGES_DIR = RESULT_DIR + "pages" + File.separator;
}
