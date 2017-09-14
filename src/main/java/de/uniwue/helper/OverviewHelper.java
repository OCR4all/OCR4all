package de.uniwue.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.NameFileComparator;

import de.uniwue.model.PageOverview;

public class OverviewHelper {
    private Map<String,PageOverview> overview = new HashMap<String, PageOverview>();
    private String pathToProject;
    private String imageType;
    
    public OverviewHelper(String pathToProject, String imageType) {
        this.pathToProject = pathToProject;
        this.setImageType(imageType);
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
            checkSegmentsExtracted();
            checkLinesExtracted();
        }
        else {
            throw new IOException("Folder does not exist!");
        }
    }

    public void initialize(String pageID) throws IOException {
        String path = pathToProject+File.separator+"Original"+File.separator+pageID+".png";
        if (new File(path).exists()) {
             overview.put(new File(path).getName(), new PageOverview(FilenameUtils.removeExtension(new File(path).getName())));
            checkPreprocessed();
            checkSegmented();
            checkSegmentsExtracted();
            checkLinesExtracted();
        }
        else {
            throw new IOException("Folder does not exist!");
        }
    }

    public void checkPreprocessed() {
        for (String key : overview.keySet()) {
            overview.get(key).setPreprocessed(true);
                if(!new File(pathToProject+File.separator+"PreProc"+File.separator+imageType+File.separator+key).exists()) 
                    overview.get(key).setPreprocessed(false);
        }
    }

    public void checkSegmented() {
        for (String key : overview.keySet()) {
            overview.get(key).setSegmented(true);
            if(!new File(pathToProject+File.separator+"OCR"+File.separator+overview.get(key).getPageId()+".xml").exists()) 
                overview.get(key).setSegmented(false);
        }
    }

    public void checkSegmentsExtracted() {
        for (String key: overview.keySet()) {
            overview.get(key).setSegmentsExtracted(true);
            if(!new File(pathToProject+File.separator+"OCR"+File.separator+"Pages"+File.separator+overview.get(key).getPageId()).isDirectory())
                overview.get(key).setSegmentsExtracted(false);
        }
    }

    public void checkLinesExtracted() {
        for (String key: overview.keySet()) {
            overview.get(key).setLinesExtracted(true);
            if (overview.get(key).isSegmentsExtracted()) {
                File[] directories = new File(pathToProject+File.separator+"OCR"+File.separator+"Pages"+File.separator+overview.get(key).getPageId()).listFiles(File::isDirectory);
                if (directories.length != 0 ) {
                    File dir = new File(directories[0].toString());
                    File[] files;
                    if(imageType.equals("Binary"))
                        files = dir.listFiles((d, name) -> name.endsWith("bin.png"));
                    else
                        files = dir.listFiles((d, name) -> name.endsWith("nrm.png"));
                    if (files.length == 0)
                        overview.get(key).setLinesExtracted(false);
                }
                else {
                    overview.get(key).setLinesExtracted(false);
                }
            }
            else
                overview.get(key).setLinesExtracted(false);
        }
    }

    public void checkHasGT() {
    }

    public Map<String,String[]> pageContent(String id){
        Map<String,String[]> pageContent = new TreeMap<String, String[]>();
        if(new File(pathToProject+File.separator+"OCR"+File.separator+"Pages"+File.separator+overview.get(id).getPageId()).exists()) {
            File[] directories = new File(pathToProject+File.separator+"OCR"+File.separator+"Pages"+File.separator+overview.get(id).getPageId()).listFiles(File::isDirectory);

            for (int folder = 0;folder < directories.length;folder++) {
                File dir = new File(directories[folder].toString());
                File[] files = dir.listFiles((d, name) -> name.endsWith(".bin.png"));
                List<String> filenames = new ArrayList<String>();
                for (int file = 0; file < files.length;file++) {
                    filenames.add(FilenameUtils.getBaseName(files[file].toString().substring(0, files[file].toString().length()-8)));
                }
                java.util.Collections.sort(filenames);
                pageContent.put((directories[folder].getName()), filenames.toArray(new String[filenames.size()]));
            }
        }
        return pageContent;
        
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

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
}
