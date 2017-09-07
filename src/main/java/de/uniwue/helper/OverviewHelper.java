package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.uniwue.model.PageOverview;

public class OverviewHelper {
    private Map<String,PageOverview> overview = new HashMap<String, PageOverview>();
    private String pathToProject;

    public OverviewHelper(String pathToProject) {
        this.pathToProject = pathToProject;
    }

    public void initialize() throws IOException {
        final File folder = new File(pathToProject+File.separator+"Original");
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                overview.put(fileEntry.getName(), new PageOverview(fileEntry.getName()));
            }
        }
    }

    public void checkPreprocessed() {
        
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
