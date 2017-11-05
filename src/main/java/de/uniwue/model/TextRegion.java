package de.uniwue.model;

import java.util.ArrayList;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/** 
 * Represents the content of an TextRegion of a page
 */
public class TextRegion implements Comparable<TextRegion> {

    /**
     * Id of the text region
     */
    private String id;

    /**
     * type of the text region
     */
    private String type;

    /**
     * List of points, which form the text region
     */
    private ArrayList<Point> points;

    /**
     * Rectangle, which cover the textregion
     */
    private Rect rect;

    /**
     * Constructor
     * @param id Id of the text region
     * @param type type of the text region
     * @param points List of points, which form the text region
     */
    public TextRegion(String id, String type, ArrayList<Point> points) {
        this.id = id;
        this.type = type;
        this.points = points;
    }

    /**
     * Calculates the rectangle, which cover a text region
     */
    public void calcRect() {
        Point[] pointArray = new Point[points.size()];
        pointArray = points.toArray(pointArray);
        
        MatOfPoint pointMat = new MatOfPoint(pointArray);
        Rect rect = Imgproc.boundingRect(pointMat);

        this.rect = rect;
    }

    /**
     * Id of the text region
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Type of the textRegion
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Points, which cover the text region
     * @return List of points
     */
    public ArrayList<Point> getPoints() {
        return points;
    }

    /**
     * Gets the rectangle representation of the text region
     * @return rectangle representation
     */
    public Rect getRect() {
        return rect;
    }

    /**
     * Compares two text region rectangles
     */
    @Override
    public int compareTo(TextRegion toCompare) {
        if (toCompare.getRect() == null) {
            toCompare.calcRect();
        }

        if (this.rect == null) {
            this.calcRect();
        }

        if (this.rect.y < toCompare.getRect().y) {
            return -1;
        } else {
            return 1;
        }
    }
}