package de.uniwue.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessConflictDetector;
import de.uniwue.feature.ProcessHandler;
import de.uniwue.feature.ProcessStateCollector;

/**
 * Helper class for line segmenting pages, which also calls the pagexmllineseg program
 */
public class LineSegmentationPageXMLHelper  implements LineSegmentationHelper{
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Image type of the project
     * Possible values: { Binary, Gray }
     */
    private String projectImageType;

    /**
     * Object to use generic functionalities
     */
    private GenericHelper genericHelper;

    /**
     * Object to determine process states
     */
    private ProcessStateCollector procStateCol;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Line Segmentation process
     */
    private int progress = -1;

    /**
     * Last time the images/pagexml are modified
     */
    private Map<String,Long> imagesLastModified;

    /**
     * Indicates if a Line Segmentation process is already running
     */
    private boolean lineSegmentationRunning = false;

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     * @param projectImageType Type of the project (gray, binary)
     * @param processingMode Processing structure of the project (Directory, Pagexml)
     */
    public LineSegmentationPageXMLHelper(String projectDir, String projectImageType, String processingMode) {
        this.projectImageType = projectImageType;
        projConf = new ProjectConfiguration(projectDir);
        genericHelper = new GenericHelper(projConf);
        procStateCol = new ProcessStateCollector(projConf, projectImageType, processingMode);
        processHandler = new ProcessHandler();
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
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException
     */
    public void initializeProcessState(List<String> pageIds) throws IOException {
        // Init the listener for image modification
        imagesLastModified = new HashMap<>();
        for(String pageId: pageIds) {
			final String pageXML = projConf.OCR_DIR + pageId + projConf.CONF_EXT;
			imagesLastModified.put(pageXML,new File(pageXML).lastModified());
		}
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     * @throws IOException 
     */
    public int getProgress() throws IOException {
    	int modifiedCount = 0;
    	if(imagesLastModified != null) {
    		for(String pagexml : imagesLastModified.keySet()) {
				if(imagesLastModified.get(pagexml) < new File(pagexml).lastModified()) {
					modifiedCount++;
				}
    		}
    		progress = (modifiedCount*100) / imagesLastModified.size();
    	} else {
    		progress = -1;
    	}
    	
        return progress;
    }


    /**
     * Executes line segmentation of all pages
     * Achieved with the help of the external python program "pagelineseg"
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @param cmdArgs Command line arguments for "pagelineseg"
     * @throws IOException
     */
    public void execute(List<String> pageIds, List<String> cmdArgs) throws IOException {
        lineSegmentationRunning = true;

        progress = 0;

        // Reset line segment data
        deleteOldFiles(pageIds);
        initializeProcessState(pageIds);
        
        List<String> command = new ArrayList<String>();
        // Create temp json file with all segment images (to not overload parameter list)
		// Temp file in a temp folder named "lineseg-<random numbers>.json"
        File segmentListFile = File.createTempFile("lineseg-",".json");
        segmentListFile.deleteOnExit(); // Delete if OCR4all terminates
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode dataList = mapper.createArrayNode();
        for (String pageId : pageIds) {
            ArrayNode pageList = mapper.createArrayNode();
        	pageList.add(projConf.getImageDirectoryByType(projectImageType) + pageId + projConf.getImageExtensionByType(projectImageType));
        	final String pageXML = projConf.OCR_DIR + pageId + projConf.CONF_EXT;
            pageList.add(pageXML);

            // Add affected line segment images with their absolute path to the json file
        	dataList.add(pageList);
        }
        ObjectWriter writer = mapper.writer();
        writer.writeValue(segmentListFile, dataList); 
        
        command.add(segmentListFile.toString());
		command.addAll(cmdArgs);
		processHandler.startProcess("pagelineseg", command, false);
        

        // Execute progress update to fill data structure with correct values
        getProgress();

        progress = 100;
        lineSegmentationRunning = false;
    }

    /**
     * Resets the progress (use if an error occurs)
     */
    public void resetProgress() {
        lineSegmentationRunning = false;
        progress = -1;
    }

    /**
     * Cancels the process
     */
    public void cancelProcess() {
        if (processHandler != null)
            processHandler.stopProcess();
        lineSegmentationRunning = false;
    }

    /**
     * Returns the Ids of the pages, for which region extraction was already executed
     *
     * @return List of valid page Ids
     * @throws IOException 
     */
    public ArrayList<String> getValidPageIds() throws IOException {
        // Get all pages and check which ones are already region extracted
        ArrayList<String> validPageIds = new ArrayList<String>();
        ArrayList<String> allPageIds = genericHelper.getPageList("Original");
        for (String pageId : allPageIds) {
            if (procStateCol.segmentationState(pageId) == true)
                validPageIds.add(pageId);
        }

        Collections.sort(validPageIds);
        return validPageIds;
    }

    /**
     * Deletion of old process related data
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @throws IOException 
     */
    public void deleteOldFiles(List<String> pageIds) throws IOException {
        for(String pageId : pageIds) {
            File pageXML = new File(projConf.OCR_DIR + pageId + projConf.CONF_EXT);
            if (!pageXML.exists())
                return;
           
            // Load pageXML and replace/delete all TextLines
			String pageXMLContent = new String(Files.readAllBytes(pageXML.toPath()));
			pageXMLContent = pageXMLContent.replaceAll("<TextLine[^>]*>.*?<\\/TextLine>", "");
			
			// Save new pageXML
			try (FileWriter fileWriter = new FileWriter(pageXML)) {
				fileWriter.write(pageXMLContent);
				fileWriter.flush();
				fileWriter.close();
			}
        }
    }

    /**
     * Checks if process depending files already exist
     *
     * @param pageIds Identifiers of the pages (e.g 0002,0003)
     * @return Information if files exist
     */
    public boolean doOldFilesExist(String[] pageIds){
        for(String pageId : pageIds) {
            if (procStateCol.lineSegmentationState(pageId) == true)
                return true;
        }
        return false;
    }

    /**
     * Determines conflicts with the process
     *
     * @param currentProcesses Processes that are currently running
     * @param inProcessFlow Indicates if the process is executed within the ProcessFlow
     * @return Type of process conflict
     */
    public int getConflictType(List<String> currentProcesses, boolean inProcessFlow) {
        return ProcessConflictDetector.lineSegmentationConflict(currentProcesses, inProcessFlow);
    }

}
