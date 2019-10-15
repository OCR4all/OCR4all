package de.uniwue.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

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
			processState.put(pageId, segments);
		}
    }

    /**
     * Create necessary Result directories if they do not exist
     */
    private String initializeResultDirectories(String resultType) {
        LocalDateTime localTime = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                .withLocale( Locale.getDefault() )
                .withZone( ZoneId.systemDefault());
        String time = localTime.format(timeFormatter);
        File resultDir = new File(projConf.RESULT_DIR + time + "_" + resultType + File.separator);
        if (!resultDir.exists())
            resultDir.mkdir();

        File resultPagesDir = new File(resultDir.getPath() + File.separator + "pages" + File.separator);
        if (!resultPagesDir.exists())
            resultPagesDir.mkdir();
        return time;
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

        String initTime = initializeResultDirectories(resultType);

        if (resultType.equals("txt")) {
            executeTextProcess(pageIds, initTime);
        } else if (resultType.equals("xml")) {
            executeXmlProcess(pageIds, initTime);
        }

        progress = 100;
    }

    /**
     * Executes result XML generation process on all specified pages
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void executeXmlProcess(List<String> pageIds, String time) throws IOException {
		File dir = new File(projConf.OCR_DIR);
		if (!dir.exists())
			return;
		
		File[] xmlFiles = dir.listFiles((d, name) -> name.endsWith(projConf.CONF_EXT));
        // Copy all xml files into output
        int processedPages = 0;
        for(File xmlFile : xmlFiles) {
            File xmlOutFile = new File(projConf.RESULT_DIR + time + "_xml" + File.separator + "pages" + File.separator +  xmlFile.getName());
            Files.copy(xmlFile.toPath(),xmlOutFile.toPath());
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
    public void executeTextProcess(List<String> pageIds, String time) throws IOException {
        initialize(pageIds);

		TreeMap<String, String> pageResult = new TreeMap<String, String>();
		int processElementCount = pageIds.size();
		int processedElements = 1;
		// For each page: Concatenation of the recognition/gt output of the linesegmentation of the page
		//                Saving output to a txt file (located at /Results/Pages/)
		for (String pageId : processState.keySet()) {
			pageResult.put(pageId, new String());

            // Retrieve every ground truth or recognition line in the page xmls and group them per page
            Path path =  Paths.get(projConf.PAGE_DIR + pageId + projConf.CONF_EXT);
            BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));
            StringBuilder contents = new StringBuilder();
            while(reader.ready()) {
                contents.append(reader.readLine());
            }
            final String xmlContent = contents.toString();
            
            // Find all textlines inside the file

            Pattern textlinePattern = Pattern.compile("\\<TextLine[^>]+?\\>(.*?)\\<\\/TextLine\\>");
            Pattern gtPattern = Pattern.compile("\\<TextEquiv[^>]+?index=\"0\"[^>]*?\\>(.*?)\\<\\/TextEquiv\\>");
            Pattern recPattern = Pattern.compile("\\<TextEquiv[^>]+?index=\"[^0]\"[^>]*?\\>(.*?)\\<\\/TextEquiv\\>");
            Matcher matcher = textlinePattern.matcher(xmlContent);

            while(matcher.find()) {
                if (stopProcess == true)
                    return;
                String textlineContent = matcher.group(1);

                Matcher gtMatcher = gtPattern.matcher(textlineContent);
                // Check for ground truth text
                if(gtMatcher.find()) {
                    String currentLine = gtMatcher.group(1).replaceAll("\\<[^>]*?\\>", "");
                    pageResult.put(pageId, pageResult.get(pageId) + currentLine + "\n");
                } else {
                    // Check for recognition text if gt text does not exist
                    Matcher recMatcher = recPattern.matcher(textlineContent);
                    if(recMatcher.find()) {
                        String currentLine = recMatcher.group(1).replaceAll("\\<[^>]*?\\>", "");
                        pageResult.put(pageId, pageResult.get(pageId) + currentLine + "\n");
                    }
                }
            }
            processedElements++;
            progress = (int) ((double) processedElements / processElementCount * 100);
			
			
			try (OutputStreamWriter writer =
						 new OutputStreamWriter(new FileOutputStream(projConf.RESULT_DIR + time + "_txt" + File.separator
                                 + "pages" + File.separator + pageId + ".txt"),
                                 StandardCharsets.UTF_8)) {
				writer.write(pageResult.get(pageId));
			}
		}
		// The recognition/gt output of the the specified pages is concatenated
		String completeResult = new String();
		for (String pageId : pageResult.keySet()) {
			completeResult += pageResult.get(pageId) + "\n";
		}
		try (OutputStreamWriter writer =
					 new OutputStreamWriter(new FileOutputStream(projConf.RESULT_DIR + time + "_txt" + File.separator + "complete" + ".txt"), StandardCharsets.UTF_8)) {
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
