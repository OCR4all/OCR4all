package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import de.uniwue.model.PageOverview;

public class OverviewHelper {
    private Map<String,PageOverview> overview = new HashMap<String, PageOverview>();
    private String pathToProject;

    public OverviewHelper(String pathToProject) {
        this.pathToProject = pathToProject;
    }

    public void initialize() throws IOException {
        String path = pathToProject+File.separator+"Original";
        if (new File(path).exists()) {
            final File folder = new File(path);
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isFile()) {
                    overview.put(fileEntry.getName(), new PageOverview(FilenameUtils.removeExtension(fileEntry.getName())));
                }
            }
            checkPreprocessed();
            checkSegmented();
        }
        else {
            throw new IOException("Folder does not exist!");
        }
    }

    public void checkPreprocessed() {
        String [] preprocesSteps = {"Binary","Despeckled","Gray"};
        for (String i: preprocesSteps) {
            for (String key : overview.keySet()) {
                if(!new File(pathToProject+File.separator+"PreProc"+File.separator+i+File.separator+key).exists()) {
                    overview.get(key).setPreprocessed(false);}
            }
        }
    }

    public void checkSegmented() {
        for (String key : overview.keySet()) {
            if(!new File(pathToProject+File.separator+"OCR"+File.separator+overview.get(key).getPageId()+".xml").exists()) {
                overview.get(key).setSegmented(false);}
        }
    }

    public List<String> getFileNames(String path) {
        List<String> results = new ArrayList<String>();
        File[] files = new File(path).listFiles();
        
        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName());
            }
        }
        return results;
    }

    public Map<String, PageOverview> getOverview() {
        return overview;
    }

    public void setOverview(Map<String, PageOverview> overview) {
        this.overview = overview;
    }

    public String getPathToProject() {
        return pathToProject;
    }
}
