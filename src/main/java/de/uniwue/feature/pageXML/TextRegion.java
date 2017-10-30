package de.uniwue.feature.pageXML;

import java.util.ArrayList;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class TextRegion implements Comparable<TextRegion> {

    private String id;
    private String type;
    private ArrayList<Point> points;
    private Rect rect;

    public TextRegion(String id, String type, ArrayList<Point> points) {
        setId(id);
        setType(type);
        setPoints(points);
    }

    public void calcRect() {
        Point[] pointArray = new Point[points.size()];
        pointArray = points.toArray(pointArray);
        
        MatOfPoint pointMat = new MatOfPoint(pointArray);
        Rect rect = Imgproc.boundingRect(pointMat);
        
        setRect(rect);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

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