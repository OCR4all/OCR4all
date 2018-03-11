package de.uniwue.model;

/** Represents the status of the different process steps of a page.
 *
 */
public class PageOverview {
    /**
     * Page identifier
     */
    private String pageId = null;

    /**
     * Preprocessed state
     */
    private boolean preprocessed = false;

    /**
     * Despeckled state
     */
    private boolean despeckled = false;

    /**
     * Segmented state
     */
    private boolean segmented = false;

    /**
     * Segments extracted state
     */
    private boolean segmentsExtracted = false;

    /**
     * Lines extracted state
     */
    private boolean linesExtracted = false;

    /**
     * Recognition state
     */
    private boolean recognition = false;

    /**
     * Constructor
     *
     * @param pageId  Page identifier
     */
    public PageOverview(String pageId) {
        this.pageId = pageId;
    }

    /**
     * Gets the identifier of the page
     *
     * @return String representation of pageId
     */
    public String getPageId() {
        return pageId;
    }

    /**
     * Sets the identifier of the page
     *
     * @param pageId String representation of identifier
     */
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    /**
     * Gets the preprocessed status of the page
     *
     * @return Boolean representation of the status
     */
    public boolean isPreprocessed() {
        return preprocessed;
    }

    /**
     * Sets the preprocessed status of the page
     *
     * @param preprocessed Boolean representation of the status
     */
    public void setPreprocessed(boolean preprocessed) {
        this.preprocessed = preprocessed;
    }

    /**
     * Gets the segmented status of the page
     *
     * @return Boolean representation of the status
     */
    public boolean isSegmented() {
        return segmented;
    }

    /**
     * Sets the segmented status of the page
     *
     * @param segmented Boolean representation of the status
     */
    public void setSegmented(boolean segmented) {
        this.segmented = segmented;
    }

    /**
     * Gets the segmentsExtracted status of the page
     *
     * @return Boolean representation of the status
     */
    public boolean isSegmentsExtracted() {
        return segmentsExtracted;
    }

    /**
     * Sets the segmentsExtracted status of the page
     *
     * @param segmentsExtracted Boolean representation of the status
     */
    public void setSegmentsExtracted(boolean segmentsExtracted) {
        this.segmentsExtracted = segmentsExtracted;
    }
    /**
     * Gets the linesExtracted status of the page
     *
     * @return Boolean representation of the status
     */
    public boolean isLinesExtracted() {
        return linesExtracted;
    }

    /**
     * Sets the linesExtracted status of the page
     *
     * @param linesExtracted Boolean representation of the status
     */
    public void setLinesExtracted(boolean linesExtracted) {
        this.linesExtracted = linesExtracted;
    }

    /**
     * Gets the recognition status of the page
     *
     * @return Boolean representation of the status
     */
    public boolean getRecognition() {
        return recognition;
    }

    /**
     * Sets the recogntion status of the page
     *
     * @param Recognition Boolean representation of the status
     */
    public void setRecognition(boolean Recognition) {
        this.recognition = Recognition;
    }

    /**
     * Gets the despeckled status of the page
     *
     * @return Boolean representation of the status
     */
    public boolean isDespeckled() {
        return despeckled;
    }

    /**
     * Sets the despeckled status of the page
     *
     * @param despeckled Boolean representation of the status
     */
    public void setDespeckled(boolean despeckled) {
        this.despeckled = despeckled;
    }
}
