package de.uniwue.feature.pageXML;

import java.util.ArrayList;
import java.util.Collections;

/** 
 * Represents the content of an page extracted from a xml file
 */
public class Page {

    /**
     * Filename of the image
     */
    private String imageFilename;

    /**
     * Width of the image
     */
    private String imageWidth;

    /**
     * Height of the image
     */
    private String imageHeight;

    /**
     * ReadingOrder object of the page
     */
    private ReadingOrder readingOrder;

    /**
     * List of ImageRegions of the page
     */
    private ArrayList<ImageRegion> imageRegions;

    /**
     * List of TextRegions of the page
     */
    private ArrayList<TextRegion> textRegions;

/** Constructor for a page
 * 
 * @param imageFilename Name of the image
 * @param imageWidth width of the image
 * @param imageHeight height of the image
 * @param readingOrder ReadingOrder of the page
 * @param imageRegions ImageRegions of the page
 * @param textRegions TextRegions of the page
 */
    public Page(String imageFilename, String imageWidth, String imageHeight,
            ReadingOrder readingOrder, ArrayList<ImageRegion> imageRegions,
            ArrayList<TextRegion> textRegions) {
        setImageFilename(imageFilename);
        setImageWidth(imageWidth);
        setImageHeight(imageHeight);

        setImageRegions(imageRegions);
        setTextRegions(textRegions);

        if (readingOrder == null) {
            System.out.println("Reading Order == null! Using naive top-to-bottom RO!");
            readingOrder = calcNaiveTop2BottomReadingOrder(textRegions, true);
        }

        setReadingOrder(readingOrder);
    }

    /**
     * Calculates an a naive top to bottom readingOrder object
     * @param textRegions List of text regions
     * @param marginaliaLast puts all marginallia to the end of the text regions list, if true
     * @return readingOrder object
     */
    private ReadingOrder calcNaiveTop2BottomReadingOrder(
            ArrayList<TextRegion> textRegions, boolean marginaliaLast) {
        Collections.sort(textRegions);

        if (marginaliaLast) {
            textRegions = putMarginaliaLast(textRegions);
        }

        ArrayList<RegionRefIndexed> regionRefIndices = new ArrayList<RegionRefIndexed>();

        for (int i = 0; i < textRegions.size(); i++) {
            TextRegion textRegion = textRegions.get(i);
            RegionRefIndexed regRefIdx = new RegionRefIndexed(i + "",
                    textRegion.getId().toString());
            regionRefIndices.add(regRefIdx);
        }

        ReadingOrder readingOrder = new ReadingOrder("oID", regionRefIndices);

        return readingOrder;
    }

    /**
     * Puts the regions with type marginalia to the end of the list
     * @param textRegions  List of textregions
     * @return List of regions wiht marginalia last
     */
    private ArrayList<TextRegion> putMarginaliaLast(
            ArrayList<TextRegion> textRegions) {
        ArrayList<TextRegion> marginalia = new ArrayList<TextRegion>();
        ArrayList<TextRegion> notMarginalia = new ArrayList<TextRegion>();

        for (int i = 0; i < textRegions.size(); i++) {
            TextRegion region = textRegions.get(i);

            System.out.println(region.getType());
            
            if (region.getType().equals("marginalia")) {
                marginalia.add(region);
            } else {
                notMarginalia.add(region);
            }
        }
        
        textRegions = new ArrayList<TextRegion>();
        textRegions.addAll(notMarginalia);
        textRegions.addAll(marginalia);

        return textRegions;
    }

    /**
     * Gets the filename of the image
     * @return filename of the image
     */
    public String getImageFilename() {
        return imageFilename;
    }

    /**
     * Sets the filename of the image
     * @param imageFilename New filename of the image
     */
    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    /**
     * Gets the with of the image
     * @return image width
     */
    public String getImageWidth() {
        return imageWidth;
    }

    /**
     * Sets the width of the image
     * @param imageWidth new width of the image
     */
    public void setImageWidth(String imageWidth) {
        this.imageWidth = imageWidth;
    }

    /**
     * Gets the height of the image
     * @return height
     */
    public String getImageHeight() {
        return imageHeight;
    }

    /**
     * Sets the height of the image
     * @param imageHeight new height if the image
     */
    public void setImageHeight(String imageHeight) {
        this.imageHeight = imageHeight;
    }

    /**
     * Gets the reading Order object of the page
     * @return readingOrder object
     */
    public ReadingOrder getReadingOrder() {
        return readingOrder;
    }

    /**
     * Sets the readingOrder of the page
     * @param readingOrder new readingOrder object
     */
    public void setReadingOrder(ReadingOrder readingOrder) {
        this.readingOrder = readingOrder;
    }

    /**
     * Gets the image regions of the page
     * @return List of image regions
     */
    public ArrayList<ImageRegion> getImageRegions() {
        return imageRegions;
    }

    /**
     * Sets the image regions of an image
     * @param imageRegions List of ne image regions
     */
    public void setImageRegions(ArrayList<ImageRegion> imageRegions) {
        this.imageRegions = imageRegions;
    }

    /**
     * Gets the text regions of the page
     * @return List of text regions of the page
     */
    public ArrayList<TextRegion> getTextRegions() {
        return textRegions;
    }

    /**
     * Sets the text regions of the page
     * @param textRegions new text regions
     */
    public void setTextRegions(ArrayList<TextRegion> textRegions) {
        this.textRegions = textRegions;
    }
}
