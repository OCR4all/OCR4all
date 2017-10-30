package de.uniwue.feature.pageXML;

import java.util.ArrayList;
import java.util.Collections;

public class Page {
    private String imageFilename;
    private String imageWidth;
    private String imageHeight;

    private ReadingOrder readingOrder;
    private ArrayList<ImageRegion> imageRegions;
    private ArrayList<TextRegion> textRegions;

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

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public String getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(String imageWidth) {
        this.imageWidth = imageWidth;
    }

    public String getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(String imageHeight) {
        this.imageHeight = imageHeight;
    }

    public ReadingOrder getReadingOrder() {
        return readingOrder;
    }

    public void setReadingOrder(ReadingOrder readingOrder) {
        this.readingOrder = readingOrder;
    }

    public ArrayList<ImageRegion> getImageRegions() {
        return imageRegions;
    }

    public void setImageRegions(ArrayList<ImageRegion> imageRegions) {
        this.imageRegions = imageRegions;
    }

    public ArrayList<TextRegion> getTextRegions() {
        return textRegions;
    }

    public void setTextRegions(ArrayList<TextRegion> textRegions) {
        this.textRegions = textRegions;
    }
}
