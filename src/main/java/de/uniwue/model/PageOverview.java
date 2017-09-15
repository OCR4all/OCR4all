package de.uniwue.model;
/** Represents the status of the different process steps of a page.
 *
 */
public class PageOverview {
    private String pageId = null;
    private boolean preprocessed = false;
    private boolean segmented = false;
    private boolean segmentsExtracted = false;
    private boolean linesExtracted = false;
    private boolean hasGT = false;
    
    @Override
    public String toString() {
        return "PageOverview [id=" + pageId + ", preprocessed=" + preprocessed + ", segmented=" + segmented
                + ", segmentsExtracted=" + segmentsExtracted + ", linesExtracted=" + linesExtracted + ", hasGT=" + hasGT
                + "]";
    }

    /**
     * @param pageId
     */
    public PageOverview(String pageId) {
        this.pageId = pageId;
    }

    /**Gets the identifier of the page
     * @return String representation of pageId
     */
    public String getPageId() {
        return pageId;
    }

    /** Sets the identifier of the page
     * @param pageId String representation of identifier
     */
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    /** Gets the preprocessed status of the page  
     * @return Boolean representation of the status
     */
    public boolean isPreprocessed() {
        return preprocessed;
    }

    /** Sets the preprocessed status of the page
     * @param preprocessed Boolean representation of the status
     */
    public void setPreprocessed(boolean preprocessed) {
        this.preprocessed = preprocessed;
    }

    /** Gets the segmented status of the page  
     * @return Boolean representation of the status
     */
    public boolean isSegmented() {
        return segmented;
    }

    /** Sets the segmented status of the page
     * @param segmented Boolean representation of the status
     */
    public void setSegmented(boolean segmented) {
        this.segmented = segmented;
    }

    /** Gets the segmentsExtracted status of the page  
     * @return Boolean representation of the status
     */
    public boolean isSegmentsExtracted() {
        return segmentsExtracted;
    }

    /** Sets the segmentsExtracted status of the page
     * @param segmentsExtracted Boolean representation of the status
     */
    public void setSegmentsExtracted(boolean segmentsExtracted) {
        this.segmentsExtracted = segmentsExtracted;
    }
    /** Gets the linesExtracted status of the page  
     * @return Boolean representation of the status
     */
    public boolean isLinesExtracted() {
        return linesExtracted;
    }

    /** Sets the linesExtracted status of the page
     * @param linesExtracted Boolean representation of the status
     */
    public void setLinesExtracted(boolean linesExtracted) {
        this.linesExtracted = linesExtracted;
    }

    /** Gets the hasGT status of the page  
     * @return Boolean representation of the status
     */
    public boolean isHasGT() {
        return hasGT;
    }

    /** Sets the hasGT status of the page
     * @param hasGT Boolean representation of the status
     */
    public void setHasGT(boolean hasGT) {
        this.hasGT = hasGT;
    }
}
