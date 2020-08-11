package de.uniwue.helper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import de.uniwue.feature.ProcessHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessStateCollector;
import de.uniwue.model.PageOverview;
import de.uniwue.feature.ProcessHandler;

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
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

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
     * Default value to be used for PDF Rendering
     */
    private int pdfdpi = 300;

    /**
     * Pages to be converted, used in calculation of conversion progression
     */
    private int pagesToConvert = -1;

    /**
     * Pages already converted,used in calculation of conversion progression
     */
    private int pagesConverted = 0;

    /**
     * Flag to determine which process the Progress bar is currently showing
     */

    private boolean pdfConversionFlag = false;

    /**
     * name of the current zip file
     */
    private String zipName;

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
     * @param pageId Identifiers of the pages (e.g 0002,0003)
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
            pOverview.setLinesExtracted(procStateCol.lineSegmentationState(pageId));
            pOverview.setRecognition(procStateCol.recognitionState(pageId));
            pOverview.setGroundtruth(procStateCol.groundTruthState(pageId));

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
     * @param pageId Identifiers of the pages (e.g 0002,0003)
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
     * Checks if the project structure of this project is a legacy project (Directory)
     *
     * @return Project validation status
     * @throws IOException
     */
    public boolean isLegacy() {
		File project = Paths.get(projConf.OCR_DIR).toFile();
		// Check for every folder inside the inputs folder that consists of 
		// numbers and may therefore be legacy data
		if(project.exists()) {
			long numberDirectories = Arrays.stream(project.listFiles())
					.filter(f -> f.isDirectory())
					.filter(f -> f.getName().matches("\\d{4}"))
					.count();
			return numberDirectories > 0;
		} else {
			return false;
		}
    }

    /**
     * Backups the whole processing directory of a legacy project before converting the project to latest
     *
     * @throws IOException
     */
    public void backupLegacy() throws IOException {
        File source = new File(projConf.PREPROC_DIR);
        File target = new File(projConf.PROJECT_DIR + File.separator + "legacy_backup");

        FileUtils.copyDirectory(source, target);
    }

    /**
     * Converts a legacy project to latest
     *
     * @throws IOException
     */
    public void convertLegacyToLatest() throws IOException {
        String dir = projConf.PREPROC_DIR;

        List<String> command = new ArrayList<String>();

        command.add("-p");
        command.add(dir);

        try {
            processHandler = new ProcessHandler();
            processHandler.startProcess("legacy_convert", command, false);

            cleanupLegacyFiles();
        }catch (Exception e){
            // Restore state before conversion in case of conversion error
            File legacy_dir = new File(projConf.PROJECT_DIR + File.separator + "legacy_backup");
            File proc_dir = new File(projConf.PREPROC_DIR);

            if(legacy_dir.isDirectory()){
                FileUtils.deleteDirectory(proc_dir);
                FileUtils.copyDirectory(legacy_dir, proc_dir);
                FileUtils.deleteDirectory(legacy_dir);
            }
        }
    }

    /**
     * Removes directories from successfully converted legacy project which are no longer needed in latest
     * @throws IOException
     */
    public void cleanupLegacyFiles() throws IOException {
        File proj_dir = new File(projConf.PREPROC_DIR);
        File[] legacy_dirs = proj_dir.listFiles(File::isDirectory);

        assert legacy_dirs != null;
        for(File dir: legacy_dirs){
            FileUtils.deleteDirectory(dir);
        }
    }

    /**
     * Removes legacy backup project in case it exists
     *
     */
    public void cleanupLegacyBackup() throws IOException {
        File backup_dir = new File(projConf.PROJECT_DIR + File.separator + "legacy_backup");
        if(backup_dir.isDirectory()) {
            FileUtils.deleteDirectory(backup_dir);
        }
    };

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

                final Mat image = Imgcodecs.imread(fileEntry.getAbsolutePath());
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
     * @param backupDelete Determines if a backup of the image folder is required when
     *                     TRUE => backup folder containing old images will be created
     *                     FALSE => no backup of old images will be created
     *             OR determines if blank pages will not be saved on PDF conversion
     *                     TRUE => blank pages in PDF will not be saved as PNG
     *                     FALSE => All pages of PDF will be saved as PNG
     * @param convert sets pdfConversionFlag
     * @throws IOException
     */
    public void execute(boolean backupDelete, boolean convert) throws IOException {
        overviewRunning = true;
        progress = 0;
        pdfConversionFlag = convert;
        initializeProcessState();
        if(convert) {
            try {
                convertPDF(projConf.ORIG_IMG_DIR,backupDelete);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            if (backupDelete)
                FileUtils.copyDirectory(new File(projConf.ORIG_IMG_DIR), new File(projConf.BACKUP_IMG_DIR));

            convertImagesToPNG();
            renameFiles();
        }
        getProgress();
        overviewRunning = false;
        progress = 100;
    }

    /**
     * Initializes the structure with which the progress of the process can be monitored
     *
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
        if(pdfConversionFlag) {
            if (pagesToConvert < 0) {
                return 0;
            } else {
                return (int) ((double) pagesConverted / pagesToConvert * 100);

            }
        } else {
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
                        if (new File(projConf.BACKUP_IMG_DIR + fileName).exists())
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

    /**
     * Checks if the project dir contains no images and a PDF
     * @return TRUE if there are no images and directory contains PDF
     * @throws IOException
     */
    public boolean checkPdfConvertable() throws IOException {
        File dir = new File(projConf.ORIG_IMG_DIR);
        File[] pngInDir = dir.listFiles((d, name) -> name.endsWith("png"));

        if (pngInDir.length == 0) {

            File[] pdfInDir = dir.listFiles((d, name) -> name.endsWith("pdf"));
            if(pdfInDir.length > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts all PDF in folder to several PNG files
     * @param sourceDir data input directory
     * @param deleteBlank Determines if blank pages will not be rendered
     * @throws FileNotFoundException
     */
    public void convertPDF(String sourceDir, boolean deleteBlank) throws FileNotFoundException {
        File dir = new File(sourceDir);
        //Listing all .pdf-Files in Folder
        File[] pdfInDir = dir.listFiles((d, name) -> name.endsWith("pdf"));
        List<File> sortedPDFs = Arrays.stream(pdfInDir)
                .sorted((f1,f2) -> f1.getName().compareTo(f2.getName())).collect(Collectors.toList());

        if (dir.exists()) {
            Splitter splitter = new Splitter();
            splitter.setMemoryUsageSetting(MemoryUsageSetting.setupTempFileOnly());
            List<PDDocument> docs= new ArrayList<PDDocument>();
            List<PDDocument> pages = new ArrayList<PDDocument>();
            try {
                for (File pdf : sortedPDFs) {
                    //using temp files to conserve memory usage, at the cost of increasing processing time
                    docs.add(PDDocument.load(pdf, MemoryUsageSetting.setupTempFileOnly()));
                }
                //splitting every pdf in single page pdf to conserve memory usage
                for(PDDocument doc : docs) {
                    pages.addAll(splitter.split(doc));
                }
                pagesToConvert = pages.size();
                int pageCounter = 0;

                //rendering every page to png
                for (PDDocument page : pages) {
                    try {
                        PDFRenderer renderer = new PDFRenderer(page);
                        //page number parameter is zero based
                        BufferedImage img = renderer.renderImageWithDPI(0, pdfdpi, ImageType.RGB);

                        if (deleteBlank) {
                            //check if image is blank page
                            if (!isBlank(bufferedImageToMat(img), 0.99, 0.99)) {
                                //suffix in filename will be used as file format
                                ImageIOUtil.writeImage(img, dir.getPath() + File.separator + String.format("%04d", ++pageCounter) + ".png", pdfdpi);
                            }
                        } else {
                            ImageIOUtil.writeImage(img, dir.getPath() + File.separator + String.format("%04d", ++pageCounter) + ".png", pdfdpi);
                        }
                        page.close();
                        pagesConverted = pageCounter;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        page.close();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                for (PDDocument doc : docs) {
                    try {
                        doc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            throw new FileNotFoundException(dir.getName() + " Folder does not exist");
        }
        sortedPDFs.clear();
    }

    /**
     * Checks if rendered Image is blank white or light-gray
     * @param img rendered Image from .pdf
     * @param areaFactor Percent of the area that is allowed to be blank [0,1]
     * @param whiteFactor Percent brightness a pixel has to have to be considered bland [0,1]
     * @return TRUE if Page is blank
     */
    private boolean isBlank(final Mat img, double areaFactor, double whiteFactor) {
        if (!(0 <= areaFactor && areaFactor <= 1) || !(0 <= whiteFactor && whiteFactor <= 1)) {
            throw new IllegalArgumentException("Percent factors are not in range of 0% and 100%");
        }
        // Convert image to grayscale
        final Mat gray = new Mat(img.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        /* Create a binary mask with all pixels that are considered blank as 1
           and everything else as 0 */
        final Mat blankMat = new Mat(img.size(), CvType.CV_8UC1);
        Imgproc.threshold(gray, blankMat, 255 * whiteFactor, 1, Imgproc.THRESH_BINARY);
        gray.release(); //Clear RAM

        boolean blank = (img.size().height * img.size().width * areaFactor) <= Core.countNonZero(blankMat);
        blankMat.release(); //Clear RAM

        return blank;
    }

    /**
     * Setter for changing the default DPI value of 300
     * @param newDPI new DPI value
     */
    public void setDPI(int newDPI) {
        pdfdpi = newDPI;
    }

    /**
     * Zips processing Directory
     * @param binary    determines if binary image will be zipped
     * @param gray      determines if grayscale image will be zipped
     */
    public void zipDir(Boolean binary, Boolean gray) {
        try {
            if(new File(projConf.PREPROC_DIR).exists()) {
                LocalDateTime localTime = LocalDateTime.now();
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                        .withLocale( Locale.getDefault() )
                        .withZone( ZoneId.systemDefault());
                zipName = projConf.PROJECT_DIR + "GTC_" + localTime.format(timeFormatter) + ".zip";
                FileOutputStream fos = new FileOutputStream(zipName);
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                FilenameFilter nameFilter = (file, s) -> true;
                File fileToZip = new File(projConf.PREPROC_DIR);
                File[] pageFiles = fileToZip.listFiles(nameFilter);
                for (File pageFile : pageFiles) {
                    zipFile(pageFile,pageFile.getName(),zipOut, binary, gray);
                }
                zipOut.close();
                fos.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Zips specified pages from processing directory
     * @param pages pages to zip
     * @param binary    determines if binary image will be zipped
     * @param gray      determines if grayscale image will be zipped
     */
    public void zipPages(String pages, Boolean binary, Boolean gray) {

        List<String> pageIdSegments = new ArrayList<String>();
        //splits page input on commas and semi-colons
        try {
            Scanner scanner = new Scanner(pages);
            scanner.useDelimiter(",|;|\n");
            while(scanner.hasNext()){
                pageIdSegments.add(scanner.next());
            }
            scanner.close();
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

        //splits every segment at hyphen and fills this range up with corresponding numbers
        List<Integer> pageIds = new ArrayList<Integer>();
        try {
            if(!pageIdSegments.isEmpty()) {
                for (String segment : pageIdSegments) {
                    if(segment.contains("-")) {
                        Scanner scanner = new Scanner(segment);
                        scanner.useDelimiter("-");
                        List<String> pageRange = new ArrayList<String>();
                        while(scanner.hasNext()){
                            pageRange.add(scanner.next());
                        }
                        if(pageRange.size() == 2) {
                            for(int i = Integer.parseInt(pageRange.get(0)); i <= Integer.parseInt(pageRange.get(1));i++) {
                                pageIds.add(i);
                            }
                        } else {
                            throw new IndexOutOfBoundsException("page range is negative or had more than one range");
                        }
                    } else {
                        pageIds.add(Integer.parseInt(segment));
                    }
                }
            } else {
                throw new IllegalArgumentException("No pages selected");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        //now zips every page selected
        try {
            if(!pageIds.isEmpty()) {
                if(new File(projConf.PREPROC_DIR).exists()) {
                    LocalDateTime localTime = LocalDateTime.now();
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                            .withLocale( Locale.getDefault() )
                            .withZone( ZoneId.systemDefault());
                    zipName = projConf.PROJECT_DIR + "GTC_" + localTime.format(timeFormatter) + ".zip";
                    FileOutputStream fos = new FileOutputStream(zipName);
                    ZipOutputStream zipOut = new ZipOutputStream(fos);
                    for (int pageId : pageIds) {

                        FilenameFilter nameFilter = (dir, s) -> s.startsWith(String.format("%04d", pageId));

                        File fileToZip = new File(projConf.PREPROC_DIR);
                        File[] pageFiles = fileToZip.listFiles(nameFilter);
                        for (File pageFile : pageFiles) {
                            zipFile(pageFile,pageFile.getName(),zipOut, binary, gray);
                        }
                    }

                    zipOut.close();
                    fos.close();
                }
            } else{
                throw new IllegalArgumentException("page list is empty");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**Recursive function that zips files that are either GTC files or folders
     *
     * @param fileToZip absolute path to file
     * @param fileName  name of file
     * @param zipOut    ZipOutputStream
     * @param binary    determines if binary image will be zipped
     * @param gray      determines if grayscale image will be zipped
     * @throws IOException
     */
    public void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, Boolean binary, Boolean gray) throws IOException{
        //do not zip hidden file
        if (fileToZip.isHidden()) {
            return;
        }
        //if file is directory list all files in directory and check for GTC data
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith(File.separator)) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + File.separator));
                zipOut.closeEntry();
            }
            FilenameFilter nameFilter = (dir, s) -> {
                try {
                    return checkGTC(dir.toString() + File.separator + s, binary, gray);
                } catch(IOException e) {
                    return false;
                }
            };

            File[] children = fileToZip.listFiles(nameFilter);
            for (File childFile : children) {
                zipFile(childFile, fileName + File.separator + childFile.getName(), zipOut, binary, gray);
            }
            return;
        }
        //additional check necessary for root folder
        if(checkGTC(fileName,binary,gray)) {
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
    }


    /**
     * Checks if file belongs to Ground Truth Data
     * @param pathToFile path to file
     * @param binary determines if binary images will be checked positive
     * @param gray determines is grayscale images will be checke positive
     * @return TRUE if there are no images and directory contains PDF
     * @throws IOException
     */
    public boolean checkGTC(String pathToFile, Boolean binary, Boolean gray) throws IOException {
        File file = new File(pathToFile);
        if(((binary && pathToFile.endsWith(projConf.BINR_IMG_EXT))
                || (gray && pathToFile.endsWith(projConf.GRAY_IMG_EXT))
                || pathToFile.endsWith(projConf.GT_EXT)
                || pathToFile.endsWith("xml")
                || file.isDirectory())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Converts BufferedImage to OpenCV.Mat
     * @param bufferedimage buffered image
     * @return matrix of the buffered image
     */
    public Mat bufferedImageToMat(BufferedImage bufferedimage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedimage, "png", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        final MatOfByte bytes = new MatOfByte(byteArrayOutputStream.toByteArray());
        final Mat image = Imgcodecs.imdecode(bytes, Imgcodecs.IMREAD_UNCHANGED);
        bytes.release();
        return image;
    }

    /**
     * Checks if there is any exportable Ground Truth Data in Project
     * @return true if GT data exist
     */
    public boolean checkGtcExportable() {
        try {
            File procDir = new File(projConf.PREPROC_DIR);
            if(procDir.isDirectory()) {
                for (File file : procDir.listFiles()) {
                    if(checkGTC(file.getAbsolutePath(),true,true)) {
                        return true;
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns Project dir to Controller
     * @return String Project dir
     */
    public String getProjDir() {
        String[] dirs = zipName.split(File.separator);
        String relZipName = dirs[dirs.length-3]+ File.separator +
                            dirs[dirs.length-2]+ File.separator +
                            dirs[dirs.length -1];
        return relZipName;
    }

}
