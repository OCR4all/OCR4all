package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.config.ProjectConfiguration;
import de.uniwue.feature.ProcessHandler;

public class LineSegmentationHelper {
    /**
     * Object to access project configuration
     */
    private ProjectConfiguration projConf;

    /**
     * Helper object for process handling
     */
    private ProcessHandler processHandler;

    /**
     * Progress of the Preprocessing process
     */
    private int progress = -1;
    
    /**
     * Segmented pages
     */
    private List<String> SegmentedPages = new ArrayList<String>();

    /** 
     * Pages to preprocess
     */
    private List<String> Pages = new ArrayList<String>();

    /**
     * Constructor
     *
     * @param projectDir Path to the project directory
     */
	public LineSegmentationHelper(String projectDir) {
		
		projConf = new ProjectConfiguration(projectDir);
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
     * Returns the regions of a list of pages
     * @param pageIds Id of the pages
     * @return List of regions
     * @throws IOException 
     */
    public List<String> getRegionsOfPage(List<String> pageIds) throws IOException {
    	List<String> RegionsOfPage = new ArrayList<String>();
    	for(String pageId :pageIds) {
            // File depth of 1 -> no recursive (file)listing 
    		//Todo: At the moment general images are considered --> Make it dependent on project type 
            Files.walk(Paths.get(projConf.PAGE_DIR + pageId), 1)
            .map(Path::toFile)
            .filter(fileEntry -> fileEntry.isFile())
            .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
            .forEach(
                fileEntry -> { 
                	if(!FilenameUtils.removeExtension(fileEntry.getName()).endsWith(".pseg"));
                	RegionsOfPage.add(projConf.PAGE_DIR + pageId + File.separator +fileEntry.getName()); }
            );
    	}
    	return RegionsOfPage;
    }

    /**
     * Returns the progress of the process
     *
     * @return Progress percentage
     * @throws IOException 
     */
    public int getProgress() throws IOException {
        for(String pageId : Pages) {
        	if(SegmentedPages.contains(pageId))	
        		continue;
        	File[] directories = new File(projConf.PAGE_DIR + pageId).listFiles(File::isDirectory);
        	List<String> RegionsOfPage = new ArrayList<String>();
            Files.walk(Paths.get(projConf.PAGE_DIR + pageId), 1)
            .map(Path::toFile)
            .filter(fileEntry -> fileEntry.isFile())
            .filter(fileEntry -> fileEntry.getName().endsWith(projConf.IMG_EXT))
            .forEach(
                fileEntry -> { 
                	if(!FilenameUtils.removeExtension(fileEntry.getName()).endsWith("pseg")){
                	RegionsOfPage.add(projConf.PAGE_DIR + pageId + File.separator + fileEntry.getName());
                	}}
            );
        	if(directories.length == RegionsOfPage.size())
            	SegmentedPages.add(pageId);
        	else	
        		break;
        }
        return (progress != 100) ? (int) ((double) SegmentedPages.size() / Pages.size() * 100) : 100;
    }

    public void LineSegmentPages(List<String> pageIds, List<String> cmdArgs) throws IOException {
        File origDir = new File(projConf.ORIG_IMG_DIR);
        if (!origDir.exists())
            return;

        progress = 0;
        Pages = pageIds;
        
        List<String> command = new ArrayList<String>();
        List<String> RegionsOfPage = getRegionsOfPage(pageIds);
        for (String region : RegionsOfPage) {
            // Add affected pages with their absolute path to the command list
            command.add(region);
        }
        command.addAll(cmdArgs);
        System.out.println(command);
        processHandler = new ProcessHandler();
        processHandler.setFetchProcessConsole(true);
        processHandler.startProcess("ocropus-gpageseg", command, false);

        progress = 100;
    }
}
