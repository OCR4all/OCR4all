package de.uniwue.helper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessStateCollector;
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
     *         "recognition" : false,
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
     * Processing structure of the project
     * Possible values: { Directory, Pagexml }
     */
    private String processingMode;

    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Structure to monitor the progress of the process
     * pageId : processedState
     *
     * Structure example:
     * {
     *     "0002.png": true,
     *         "backup" : true,
     *         "pngConversion" : false,
     *     ...
     * }
     */
    private TreeMap<String, TreeMap<String, Boolean>> processState;

    /**
     * Progress of the overview process
     */
    private int progress = -1;

    /**
     * Indicates if an overview process is already running
     */
    private boolean overviewRunning = false;

    /**
     * Indicates if the process should be cancelled
     */
    private boolean stopProcess = false;

    /**
     * Constructor
     *
     * @param pathToProject  Absolute path of the project on the filesystem
     * @param imageType  Image type of the project
     * @param processingMode Processing structure of the project (Directory, Pagexml)
     */
    public OverviewHelper(String pathToProject, String imageType, String processingMode) {
        this.imageType = imageType;
        this.projConf = new ProjectConfiguration(pathToProject);
        this.procStateCol = new ProcessStateCollector(this.projConf, imageType, processingMode);
        this.processingMode = processingMode;
    }

    /**
     * Constructor
     *
     * @param projConf  Object to access project configuration
     * @param imageType  Image type of the project
     * @param processingMode  Project processing structure either Directory or PageXML
     */
    public OverviewHelper(ProjectConfiguration projConf, String imageType, String processingMode) {
        this.imageType = imageType;
        this.projConf = projConf;
        this.processingMode = processingMode;
    }

    /**
     * Generates status overview for one page
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void initialize(String pageId) throws IOException {
        File pageImg = new File(projConf.ORIG_IMG_DIR + pageId + projConf.IMG_EXT);
        if (pageImg.exists()) {
            // Create page overview with states of all required processes
            PageOverview pOverview = new PageOverview(pageId);
            pOverview.setPreprocessed(procStateCol.preprocessingState(pageId));
            pOverview.setDespeckled(procStateCol.despecklingState(pageId));
            pOverview.setSegmented(procStateCol.segmentationState(pageId));
            pOverview.setSegmentsExtracted(procStateCol.regionExtractionState(pageId));
            pOverview.setLinesExtracted(procStateCol.lineSegmentationState(pageId));
            pOverview.setRecognition(procStateCol.recognitionState(pageId));

            overview.put(pageImg.getName(), pOverview);
        }
        else {
            throw new IOException("Page does not exist!");
        }
    }

    /**
     * Generates project status overview for all existing pages
     *
     * @throws IOException
     */
    public void initialize() throws IOException {
        File origImgFolder = new File(projConf.ORIG_IMG_DIR);
        if (origImgFolder.exists()) {
            for (final File fileEntry : origImgFolder.listFiles()) {
                String fileName = fileEntry.getName();
                // Only load files with appropriate image extension
                if (!("." + FilenameUtils.getExtension(fileName)).equals(projConf.IMG_EXT))
                    continue;

                String pageId = FilenameUtils.removeExtension(fileEntry.getName());
                initialize(pageId);
            }
        }
        else {
            throw new IOException("Folder does not exist!");
        }
    }

    /**
     * Generates content for one page
     * This includes its segments and their lines
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @return Sorted map of page content
     * @throws IOException 
     */
    public Map<String, String[]> pageContent(String pageId) throws IOException {
        Map<String, String[]> pageContent = new TreeMap<String, String[]>();
        if (!new File(projConf.PAGE_DIR + pageId).exists())
            return pageContent;

        // Get all extracted segments / regions
        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.PAGE_DIR + pageId), 1)
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
        .filter(fileEntry -> !fileEntry.getName().endsWith(projConf.BINR_IMG_EXT))
        .filter(fileEntry -> !fileEntry.getName().endsWith(projConf.GRAY_IMG_EXT))
        .filter(fileEntry -> !fileEntry.getName().endsWith(projConf.PSEG_EXT))
        .sorted()
        .forEach(
            fileEntry -> {
                // Initialize pageContent data structure with all segments/regions
                pageContent.put(FilenameUtils.removeExtension(fileEntry.getName()), new String[0]);
            }
        );

        // Add all extracted line segments to previously initialized data structure
        File[] segmentDirs = new File(projConf.PAGE_DIR + pageId).listFiles(File::isDirectory);
        for (File segmentDir : segmentDirs) {
            List<String> segmentIds = new ArrayList<String>();
            Files.walk(Paths.get(segmentDir.getAbsolutePath()), 1)
            .map(Path::toFile)
            .filter(fileEntry -> fileEntry.isFile())
            .filter(fileEntry -> fileEntry.getName().endsWith(projConf.getImageExtensionByType(imageType)))
            .sorted()
            .forEach(
                fileEntry -> {
                    // Call removeExtension twice, due to "Binary"|"Gray" image extensions (".bin.png"|".nrm.png")
                    segmentIds.add(FilenameUtils.removeExtension(FilenameUtils.removeExtension(fileEntry.getName())));
                }
            );

            pageContent.put(segmentDir.getName(), segmentIds.toArray(new String[segmentIds.size()]));
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

    /**
     * List all filenames matching a pattern
     *
     * @param root  Directory
     * @param regex Regex String for matching filenames
     * @return all files witch match the regex pattern
     */
    public static File[] listFilesMatching(File root, String regex) {
        if(!root.isDirectory()) {
            throw new IllegalArgumentException(root + " is no directory");
        }
        final Pattern p = Pattern.compile(regex);
        return root.listFiles(new FileFilter(){
            @Override
            public boolean accept(File file) {
                return p.matcher(file.getName()).matches();
            }
        });
    }

    /**
     * Checks if the projectDir exits
     *
     * @return status of the projectDir
     */
    public boolean checkProjectDir() {
        if(!new File(projConf.PROJECT_DIR).exists())
            return false;
        return true;

    }

    /**
     * Validates the projectDir
     *
     * @return validation status of the projectDir
     */
    public boolean validateProjectDir() {
        if(!new File(projConf.ORIG_IMG_DIR).exists())
            return false;
        return true;

    }

    /**
     * Checks if all filenames have the correct IMG_EXT and the project file naming e.g (0001, 0002 ... XXXX)
     *
     * @return Project validation status
     * @throws IOException 
     */
    public boolean isProjectValid() throws IOException {
        ArrayList<Predicate<File>> allPredicates = new ArrayList<Predicate<File>>();
        for (String ext : projConf.CONVERT_IMG_EXTS)
            allPredicates.add(fileEntry -> fileEntry.getName().endsWith(ext));

        ArrayList<File> imagesToConvert = new ArrayList<File>();
        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.ORIG_IMG_DIR), 1)
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(allPredicates.stream().reduce(w -> false, Predicate::or))
        .sorted()
        .forEach(
            fileEntry -> { 
                imagesToConvert.add(fileEntry);
            }
        );

        // Check for images with incorrect format
        if (imagesToConvert.size() > 0)
            return false;

        File[] imagesWithCorrectNaming = listFilesMatching(new File(projConf.ORIG_IMG_DIR),"^\\d{4,}" + projConf.IMG_EXT);
        File[] imagesAll = new File(projConf.ORIG_IMG_DIR).listFiles((d, name) -> name.endsWith(projConf.IMG_EXT));
        // Check for images with incorrect naming
        if (imagesWithCorrectNaming.length != imagesAll.length) 
            return false;

        return true;
    }

    /**
     * Lists all available projects from the data directory
     *
     * @return Map of projects (key = projName | value = path)
     */
    public static TreeMap<String, String> listProjects(){
        TreeMap<String, String> projects = new TreeMap<String, String>();

        File projDataDir = new File(ProjectConfiguration.PROJ_DATA_DIR);
        if (!projDataDir.exists())
            return projects;

        File[] projectsDirs = projDataDir.listFiles(File::isDirectory);
        for (File project: projectsDirs) {
             projects.put(project.getName(), project.getAbsolutePath() + File.separator);
        }

        return projects;
    }

    /**
     * Converts all images to PNG Extension
     *
     * @throws IOException 
     */
    public void convertImagesToPNG() throws IOException {
        if (stopProcess == true)
            return;
        ArrayList<Predicate<File>> allPredicates = new ArrayList<Predicate<File>>();
        for (String ext : projConf.CONVERT_IMG_EXTS) 
            allPredicates.add(fileEntry -> fileEntry.getName().endsWith(ext));

        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.ORIG_IMG_DIR), 1)
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(allPredicates.stream().reduce(w -> false, Predicate::or))
        .sorted()
        .forEach(
            fileEntry -> { 
                if (stopProcess == true)
                    return;

                Mat image = Imgcodecs.imread(fileEntry.getAbsolutePath());
                // Convert and save as new image file
                Imgcodecs.imwrite(FilenameUtils.removeExtension(fileEntry.getAbsolutePath()) + projConf.IMG_EXT, image);
                // Remove old image file (project needs to be valid for the loading process)
                try {
                    Files.delete(Paths.get(fileEntry.getAbsolutePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.release();
            }
        );
    }

    /**
     * Renames all files in the 'original' folder to names that consists of an ascending number of digits (e.g 0001, 0002 ...)
     *
     * @throws IOException 
     */
    public void renameFiles() throws IOException {
        if (stopProcess == true)
            return;
        ArrayList<File> imageFiles = new ArrayList<File>();
        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.ORIG_IMG_DIR), 1)
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
        .sorted()
        .forEach(
            fileEntry -> { imageFiles.add(fileEntry); }
        );

        int minimumFormatLength = String.valueOf(imageFiles.size()).length();
        // File names must consist of at least four digits
        if (minimumFormatLength < projConf.minimumNameLength)
            minimumFormatLength = projConf.minimumNameLength;

        // Build formatting possibility
        String format = "";
        for (int i = 1; i <= minimumFormatLength; i++)
            format = format + 0;
        DecimalFormat df = new DecimalFormat(format);

        int formattingCounter = 1;
        for (File file : imageFiles) {
            if (stopProcess == true)
                return;

            if (!file.getName().equals(projConf.ORIG_IMG_DIR + df.format(formattingCounter) + projConf.IMG_EXT)) {
                file.renameTo(new File(projConf.ORIG_IMG_DIR + df.format(formattingCounter) + projConf.IMG_EXT));
            }
            formattingCounter++;
        }
    }

    /**
     * Adjustments to files so that they correspond to the project standard 
     * 
     * @param backupImages Determines if a backup of the image folder is required 
     * @throws IOException
     */
    public void execute(boolean backupImages) throws IOException {
        overviewRunning = true;
        progress = 0;
        initializeProcessState();

        if (backupImages)
            FileUtils.copyDirectory(new File(projConf.ORIG_IMG_DIR), new File(projConf.BACKUP_IMG_DIR));

        convertImagesToPNG();
        renameFiles();

        getProgress();
        overviewRunning = false;
        progress = 100;
    }

    /**
     * Initializes the structure with which the progress of the process can be monitored
     *
     * @param pageIds Identifiers of the pages 
     * @throws IOException
     */
    public void initializeProcessState() throws IOException {
        // Initialize the status structure
        processState = new TreeMap<String, TreeMap<String, Boolean>>();
        ArrayList<Predicate<File>> allPredicates = new ArrayList<Predicate<File>>();
        for (String ext : projConf.CONVERT_IMG_EXTS) 
            allPredicates.add(fileEntry -> fileEntry.getName().endsWith(ext));
        allPredicates.add(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT));
        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.ORIG_IMG_DIR), 1)
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(allPredicates.stream().reduce(w -> false, Predicate::or))
        .sorted()
        .forEach(
            fileEntry -> { 
                TreeMap<String, Boolean> status = new TreeMap<String, Boolean>();
                status.put("backup", false);
                status.put("pngConversion", false);
                processState.put(fileEntry.getName(),status);
            }
        );
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        // Prevent function from calculation progress if process is not running
        if (overviewRunning == false)
            return progress;

        int files = 0;
        int processedFiles = 0;

        for (String fileName : processState.keySet()) {
            for (String processType : processState.get(fileName).keySet()) {
                files += 1;

                if (processState.get(fileName).get(processType) == true) {
                    processedFiles += 1;
                    continue;
                }

                if (processType == "backup") {
                    if ( new File(projConf.BACKUP_IMG_DIR + fileName).exists())
                        processState.get(fileName).put(processType, true);
                }

                if (processType == "pngConversion") {
                    if (new File(projConf.ORIG_IMG_DIR + FilenameUtils.removeExtension(fileName) + projConf.IMG_EXT).exists())
                        processState.get(fileName).put(processType, true);
                }
            }
        }

        // Safe check, in case Files were not adjusted
        return (progress != 100) ? (int) ((double) processedFiles / files * 100) : 100;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        stopProcess = true;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        overviewRunning = false;
        progress = -1;
    }
}
