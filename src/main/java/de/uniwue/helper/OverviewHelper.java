package de.uniwue.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

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
                String[] parts = fileEntry.toString().split(File.separator);
                overview.put(parts[parts.length-1], new PageOverview(fileEntry.toString()));
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
