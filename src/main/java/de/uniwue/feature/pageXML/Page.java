package de.uniwue.feature.pageXML;

import java.util.ArrayList;
import java.util.Collections;

import de.uniwue.model.ImageRegion;
import de.uniwue.model.RegionRefIndexed;
import de.uniwue.model.TextRegion;

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

        this.imageFilename = imageFilename;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageRegions = imageRegions;
        this.textRegions = textRegions;

        if (readingOrder == null) {
            System.out.println("Reading Order == null! Using naive top-to-bottom RO!");
            readingOrder = calcNaiveTop2BottomReadingOrder(textRegions, true);
        }
        this.readingOrder = readingOrder;
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
            RegionRefIndexed regRefIdx = new RegionRefIndexed(Integer.toString(i),
                    textRegion.getId().toString());
            regionRefIndices.add(regRefIdx);
        }

        ReadingOrder readingOrder = new ReadingOrder("oID", regionRefIndices);

        return readingOrder;
    }

    /**
     * Puts the regions with type marginalia to the end of the list
     * @param textRegions  List of textregions
     * @return List of regions with marginalia last
     */
    private ArrayList<TextRegion> putMarginaliaLast(
            ArrayList<TextRegion> textRegions) {
        ArrayList<TextRegion> marginalia = new ArrayList<TextRegion>();
        ArrayList<TextRegion> notMarginalia = new ArrayList<TextRegion>();

        for (int i = 0; i < textRegions.size(); i++) {
            TextRegion region = textRegions.get(i);

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
     * Gets the with of the image
     * @return image width
     */
    public String getImageWidth() {
        return imageWidth;
    }

    /**
     * Gets the height of the image
     * @return height
     */
    public String getImageHeight() {
        return imageHeight;
    }

    /**
     * Gets the reading Order object of the page
     * @return readingOrder object
     */
    public ReadingOrder getReadingOrder() {
        return readingOrder;
    }

    /**
     * Gets the image regions of the page
     * @return List of image regions
     */
    public ArrayList<ImageRegion> getImageRegions() {
        return imageRegions;
    }

    /**
     * Gets the text regions of the page
     * @return List of text regions of the page
     */
    public ArrayList<TextRegion> getTextRegions() {
        return textRegions;
    }
}
