package de.uniwue.feature.pageXML;

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
        setId(id);
        setPoints(points);
    }

    /**
     * Gets the id of the image region
     * @return
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets a list of points for the image region
     * @return
     */
    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }
}