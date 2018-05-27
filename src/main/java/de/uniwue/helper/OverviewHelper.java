package de.uniwue.helper;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Constructor
     *
     * @param pathToProject  Absolute path of the project on the filesystem
     * @param imageType  Image type of the project
     */
    public OverviewHelper(String pathToProject, String imageType) {
        this.imageType = imageType;
        this.projConf = new ProjectConfiguration(pathToProject);
        this.procStateCol = new ProcessStateCollector(this.projConf, imageType);
    }

    /**
     * Constructor
     *
     * @param projConf  Object to access project configuration
     * @param imageType  Image type of the project
     */
    public OverviewHelper(ProjectConfiguration projConf, String imageType) {
        this.imageType = imageType;
        this.projConf = projConf;
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
     * Checks if all filenames are using the project file naming e.g (0001, 0002 ... XXXX) and file Extension is IMG_EXT
     *
     * @return true = all files are using project naming, false = files are not using project naming
     * @throws IOException 
     */
    public boolean isProjectValid() throws IOException {
        boolean status = false;
        ArrayList<Predicate<File>> allPredicates = new ArrayList<Predicate<File>>();
        for (String ext : projConf.ALLOWED_IMG_EXTS) {
            if (!ext.equals(projConf.IMG_EXT))
                allPredicates.add(fileEntry -> fileEntry.getName().endsWith(ext));
        }
        ArrayList<File> filesWithAllowedImgExts = new ArrayList<File>();
        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.ORIG_IMG_DIR), 1)
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(allPredicates.stream().reduce(w -> true, Predicate::or))
        .sorted()
        .forEach(
            fileEntry -> { 
                filesWithAllowedImgExts.add(fileEntry);
            }
        );
        if (filesWithAllowedImgExts.size() > 0)
            return false;

        File[] filesFilterd = listFilesMatching(new File(projConf.ORIG_IMG_DIR),"^\\d{4,}" + projConf.IMG_EXT);
        File[] files = new File(projConf.ORIG_IMG_DIR).listFiles((d, name) -> name.endsWith(projConf.IMG_EXT));
        if (filesFilterd.length == files.length) 
            status = true;
        return status;
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
        ArrayList<Predicate<File>> allPredicates = new ArrayList<Predicate<File>>();
        for (String ext : projConf.ALLOWED_IMG_EXTS) 
            allPredicates.add(fileEntry -> fileEntry.getName().endsWith(ext));

        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.ORIG_IMG_DIR), 1)
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(allPredicates.stream().reduce(w -> true, Predicate::or))
        .sorted()
        .forEach(
            fileEntry -> { 
                if (FilenameUtils.getExtension(fileEntry.getAbsolutePath()) != projConf.IMG_EXT) {
                    Mat image = Imgcodecs.imread(fileEntry.getAbsolutePath());
                    Imgcodecs.imwrite(FilenameUtils.removeExtension(fileEntry.getAbsolutePath()) + projConf.IMG_EXT, image);
                }
            }
        );
    }

    /**
     * Adjustments to files so that they correspond to the project standard 
     * 
     * @param backup Setting to backup files
     * @param convertImagesToPNG Setting to convertImages to png
     * @param renameFiles Setting to rename Files 
     * @throws IOException
     */
    public void adjustFiles(boolean backup, boolean convertImagesToPNG, boolean renameFiles ) throws IOException {

        if (backup) {
            File backupDir = new File(projConf.BACKUP_IMG_DIR);
            if(!backupDir.exists()) 
                backupDir.mkdir();
            FileUtils.copyDirectory(new File(projConf.ORIG_IMG_DIR), new File(projConf.BACKUP_IMG_DIR));
        }

        if (convertImagesToPNG)
            convertImagesToPNG();

        if (renameFiles)
            renameFiles();
    }

    /**
     * Renames all files in the 'original' folder to names that consists of an ascending number of digits (e.g 0001, 0002 ...)
     * Changes to Filenames is backuped to a file in the project directory
     *
     * @throws IOException 
     */
    public void renameFiles() throws IOException {
        ArrayList<File> ListofFiles = new ArrayList<File>();
        // File depth of 1 -> no recursive (file)listing
        Files.walk(Paths.get(projConf.ORIG_IMG_DIR), 1)
        .map(Path::toFile)
        .filter(fileEntry -> fileEntry.isFile())
        .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
        .sorted()
        .forEach(
            fileEntry -> { ListofFiles.add(fileEntry);
            }
        );

        int minimumFormatLength = String.valueOf(ListofFiles.size()).length();
        // File names must consist of at least four digits
        if(minimumFormatLength < projConf.minimumNameLength)
            minimumFormatLength = projConf.minimumNameLength;
        String Format = "";
        for (int i = 1; i <= minimumFormatLength; i++)
            Format = Format + 0;
        DecimalFormat df = new DecimalFormat(Format);

        //File which contains the information about the renaming
        File backupFilename = new File(projConf.PROJECT_DIR + new SimpleDateFormat("ssmmHHddMMyyyy'.txt'").format(new Date()));
        //name of files, which will be renamed
        TreeMap<File, File> hm = new TreeMap<File, File>();
        int i = 1;
        String writeFilenamesToFile = "";
        for (File file : ListofFiles) {
            File newname = new File(projConf.ORIG_IMG_DIR + df.format(i) + projConf.IMG_EXT);
            if (!newname.getName().equals(file.getName())) {
                hm.put(file, newname);
                writeFilenamesToFile = writeFilenamesToFile + file.getName() + " renamed to " + newname.getName() + "\n";
            }
            i++;
        }
        // writing backup filenames to file
        FileUtils.writeStringToFile(backupFilename,writeFilenamesToFile,"UTF-8", true);

        // renaming files
        for (File file : hm.keySet()) {
            file.renameTo(hm.get(file));
        }
    }
}
