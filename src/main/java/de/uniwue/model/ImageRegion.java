package de.uniwue.model;

import java.util.ArrayList;

import org.opencv.core.Point;
/** 
 * Represents the content of an image region of a page
 */
public class ImageRegion {

    /**
     * Id of the image region
     */
    private String id;

    /**
     * Points, which form the region
     */
    private ArrayList<Point> points;

    /**
     * Constructor
     * @param id of the image region
     * @param points Points of the image region
     */
    public ImageRegion(String id, ArrayList<Point> points) {
        this.id = id;
        this.points = points;
    }

    /**
     * Gets the id of the image region
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Gets a list of points for the image region
     * @return
     */
    public ArrayList<Point> getPoints() {
        return points;
    }
}