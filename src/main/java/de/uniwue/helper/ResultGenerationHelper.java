package de.uniwue.helper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;

public class ResultGenerationHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Object to use generic functionalities
     */
    private GenericHelper genericHelper;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the result generation process
     */
    private int progress = -1;

    /**
     * Indicates if the result generation process should be stopped
     */
    private boolean stopProcess = false;

    /**
     * Structure to monitor the progress of the process
     * pageId : segmentId : lineSegmentId : processedState
     * <p>
     * Structure example:
     * {
     * "0002": {
     * "0002__000__paragraph" : {
     * "0002__000__paragraph__000" : true,
     * "0002__000__paragraph__001" : false,
     * ...
     * },
     * ...
     * },
     * ...
     * }
     */
    private TreeMap<String, TreeMap<String, TreeMap<String, Boolean>>> processState =
            new TreeMap<String, TreeMap<String, TreeMap<String, Boolean>>>();

    /**
     * Constructor
     *
     * @param projectDir       Path to the project directory
     * @param projectImageType Type of the project (binary, gray)
     */
    public ResultGenerationHelper(String projectDir, String projectImageType) {
        projConf = new ProjectConfiguration(projectDir);
        processHandler = new ProcessHandler();
        procStateCol = new ProcessStateCollector(projConf, projectImageType);
        genericHelper = new GenericHelper(projConf);
    }

    /**
     * Gets the process handler object
     *
     * @return Returns the process Helper
     */
    public ProcessHandler getProcessHandler() {
        return processHandler;
    }

    /**
     * Initializes the structure with which the progress of the process can be monitored
     *
     * @param pageIds Identifiers of the chosen pages (e.g 0002,0003)
     * @throws IOException
     */
    public void initialize(List<String> pageIds) throws IOException {
        // Initialize the status structure
        processState = new TreeMap<String, TreeMap<String, TreeMap<String, Boolean>>>();

        for (String pageId : pageIds) {
            TreeMap<String, TreeMap<String, Boolean>> segments = new TreeMap<String, TreeMap<String, Boolean>>();
            // File depth of 1 -> no recursive (file)listing
            File[] lineSegmentDirectories = new File(projConf.PAGE_DIR + pageId).listFiles(File::isDirectory);
            if (lineSegmentDirectories.length != 0) {
                for (File dir : lineSegmentDirectories) {
                    TreeMap<String, Boolean> lineSegments = new TreeMap<String, Boolean>();
                    Files.walk(Paths.get(dir.getAbsolutePath()), 1)
                            .map(Path::toFile)
                            .filter(fileEntry -> fileEntry.isFile())
                            .filter(fileEntry -> fileEntry.getName().endsWith(projConf.REC_EXT))
                            .filter(fileEntry -> !fileEntry.getName().endsWith(projConf.GT_EXT))
                            .forEach(
                                    fileEntry -> {
                                        //.pred.txt is removed to get the id of the line segment
                                        if (fileEntry.getName().contains(projConf.REC_EXT)) {
                                            String lineSegmentId = fileEntry.getName().substring(0, fileEntry.getName().indexOf(projConf.REC_EXT));
                                            lineSegments.put(lineSegmentId, false);
                                        }
                                    }
                            );
                    segments.put(dir.getName(), lineSegments);
                }
            }
            processState.put(pageId, segments);
        }
    }

    /**
     * Create necessary Result directories if they do not exist
     */
    private void initializeResultDirectories() {
        File resultDir = new File(projConf.RESULT_DIR);
        if (!resultDir.exists())
            resultDir.mkdir();

        File resultPagesDir = new File(projConf.RESULT_PAGES_DIR);
        if (!resultPagesDir.exists())
            resultPagesDir.mkdir();
    }

    /**
     * Executes result generation process on all specified pages
     *
     * @param pageIds    Identifiers of the pages (e.g 0002,0003)
     * @param resultType specified resultType (txt, xml)
     * @throws IOException
     */
    public void executeProcess(List<String> pageIds, String resultType) throws IOException {
        stopProcess = false;
        progress = 0;

        initializeResultDirectories();

        if (resultType.equals("txt")) {
            executeTextProcess(pageIds);
        } else if (resultType.equals("xml")) {
            executeXmlProcess(pageIds);
        }

        progress = 100;
    }

    /**
     * Executes result XML generation process on all specified pages
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void executeXmlProcess(List<String> pageIds) throws IOException {
        File dir = new File(projConf.OCR_DIR);
        deleteOldFiles(pageIds, "xml");
        if (!dir.exists())
            return;

        File[] xmlFiles = dir.listFiles((d, name) -> name.endsWith(projConf.CONF_EXT));
        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);

        int processedPages = 1;
        for (File xmlFile : xmlFiles) {
            if (stopProcess == true)
                return;
            if (!pageIds.contains(FilenameUtils.removeExtension(xmlFile.getName())))
                continue;
            List<String> command = new ArrayList<String>();
            command.add(xmlFile.getAbsolutePath());
            command.add("--output");
            command.add(projConf.RESULT_PAGES_DIR + xmlFile.getName());
            processHandler.startProcess("pagedir2pagexml", command, false);

            progress = (int) ((double) processedPages / xmlFiles.length * 100);
            processedPages++;
        }
    }

    /**
     * Executes result TXT generation process on all specified pages
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void executeTextProcess(List<String> pageIds) throws IOException {
        initialize(pageIds);
        deleteOldFiles(pageIds, "txt");

        TreeMap<String, String> pageResult = new TreeMap<String, String>();
        int lineSegmentsCount = 0;
        for (String pageId : processState.keySet()) {
            for (String segmentId : processState.get(pageId).keySet()) {
                lineSegmentsCount += processState.get(pageId).get(segmentId).size();
            }
        }

        int processedLineSegments = 1;
        // For each page: Concatenation of the recognition/gt output of the linesegmentation of the page
        //                Saving output to a txt file (located at /Results/Pages/)
        for (String pageId : processState.keySet()) {
            pageResult.put(pageId, new String());

            for (String segmentId : processState.get(pageId).keySet()) {
                for (String lineSegmentId : processState.get(pageId).get(segmentId).keySet()) {
                    if (stopProcess == true)
                        return;

                    String lineSegDir = projConf.PAGE_DIR + pageId + File.separator + segmentId + File.separator + lineSegmentId;
                    // Using the gt output when available otherwise the recognition output
                    Path path = Paths.get(lineSegDir + projConf.GT_EXT);
                    if (!Files.exists(path))
                        path = Paths.get(lineSegDir + projConf.REC_EXT);

                    BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));
                    String currentLine = new String();
                    while ((currentLine = reader.readLine()) != null) {
                        pageResult.put(pageId, pageResult.get(pageId) + currentLine + "\n");
                    }

                    progress = (int) ((double) processedLineSegments / lineSegmentsCount * 100);
                    processedLineSegments++;
                }
            }
            try (OutputStreamWriter writer =
                         new OutputStreamWriter(new FileOutputStream(projConf.RESULT_PAGES_DIR + pageId + ".txt"), StandardCharsets.UTF_8)) {
                writer.write(pageResult.get(pageId));
            }
        }
        // The recognition/gt output of the the specified pages is concatenated
        String completeResult = new String();
        for (String pageId : pageResult.keySet()) {
            completeResult += pageResult.get(pageId) + "\n";
        }
        try (OutputStreamWriter writer =
                     new OutputStreamWriter(new FileOutputStream(projConf.RESULT_DIR + "complete" + ".txt"), StandardCharsets.UTF_8)) {
            writer.write(completeResult);
        }
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        if (processHandler != null)
            processHandler.stopProcess();
        stopProcess = true;
    }

    /**
     * Returns the Ids of the pages, for which recognition process was already executed
     *
     * @return List of valid page Ids
     * @throws IOException
     */
    public ArrayList<String> getValidPageIds() throws IOException {
        // Get all pages and check which ones are already region extracted
        ArrayList<String> validPageIds = new ArrayList<String>();
        ArrayList<String> allPageIds = genericHelper.getPageList("Original");
        for (String pageId : allPageIds) {
            if (procStateCol.recognitionState(pageId) == true)
                validPageIds.add(pageId);
        }

        Collections.sort(validPageIds);
        return validPageIds;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Deletion of old process related files
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     */
    public void deleteOldFiles(List<String> pageIds, String type) {
        // Delete result of each page
        for (String pageId : pageIds) {
            if (type.equals("txt")) {
                File pageTxtResult = new File(projConf.RESULT_PAGES_DIR + pageId + projConf.REC_EXT);
                if (pageTxtResult.exists())
                    pageTxtResult.delete();
            }
            if (type.equals("xml")) {
                File pageXmlResult = new File(projConf.RESULT_PAGES_DIR + pageId + projConf.CONF_EXT);
                if (pageXmlResult.exists())
                    pageXmlResult.delete();
            }
        }
        if (type.equals("txt")) {
            // delete the concatenated result of the pages
            File completeResult = new File(projConf.RESULT_DIR + "complete" + projConf.REC_EXT);
            if (completeResult.exists())
                completeResult.delete();
        }
    }

    /**
     * Checks if process depending files already exist
     *
     * @param pageIds    Identifiers of the pages (e.g 0002,0003)
     * @param resultType Type of the result (xml, txt)
     * @return Information if files exist
     */
    public boolean doOldFilesExist(String[] pageIds, String resultType) {
        for (String pageId : pageIds) {
            if (procStateCol.resultGenerationState(pageId, resultType) == true)
                return true;
        }
        return false;
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses) {
        return ProcessConflictDetector.resultConflict(currentProcesses);
    }
}
