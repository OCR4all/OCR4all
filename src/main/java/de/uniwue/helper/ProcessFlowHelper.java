package de.uniwue.helper;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import de.uniwue.model.PageOverview;

/**
 * Helper class for processflow pages
 */
public class ProcessFlowHelper {
    /**
     * Absolute path of the project on the filesystem
     */
    private String projectDir;

    /**
     * Image type of the project
     * Possible values: { Binary, Gray }
     */
    private String imageType;

    /**
     * Constructor
     *
     * @param projectDir Absolute path to the project
     * @param imageType Image type of the project
     */
    public ProcessFlowHelper(String projectDir, String imageType) {
        this.projectDir = projectDir;
        this.imageType  = imageType;
    }

    /**
     * Identifies pages for which the given status was successfully processed
     *
     * @param initialPageIds Page identifiers that should be checked
     * @param checkProcess Process status that should be checked for given page identifiers
     * @return Page identifiers that were successfully processed
     */
    public String[] getValidPageIds(String[] initialPageIds, String checkProcess) {
        // Needed to determine process results
        OverviewHelper overviewHelper = new OverviewHelper(projectDir, imageType);

        // Get overview of pages including the status that needs to be checked
        try {
            for (String pageId : initialPageIds)
                overviewHelper.initialize(pageId, false);

            switch(checkProcess) {
                case "preprocessing":    overviewHelper.checkPreprocessed(); break;
                case "despeckling":      overviewHelper.checkDespeckled(); break;
                case "segmentation":     overviewHelper.checkSegmented(); break;
                case "regionextraction": overviewHelper.checkSegmentsExtracted(); break;
                case "linesegmentation": overviewHelper.checkLinesExtracted(); break;
                case "recognition":      overviewHelper.checkRecognition(); break;
                default: return new String[0];
            }
        } catch (IOException e) {
            return new String[0];
        }

        // Find correctly processed pages
        Set<String> processedPages = new TreeSet<String>();
        Map<String, PageOverview> overview = overviewHelper.getOverview();
        for(Entry<String, PageOverview> pageInfo: overview.entrySet()) {
            if (pageInfo.getValue().isPreprocessed())
                processedPages.add(pageInfo.getValue().getPageId());
        }

        // Convert to required data type
        String[] processedPagesArr = processedPages.toArray(new String[processedPages.size()]);
        return processedPagesArr;
    }
}
