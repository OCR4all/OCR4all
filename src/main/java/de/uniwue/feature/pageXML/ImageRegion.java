package de.uniwue.feature.pageXML;

import java.util.ArrayList;

import org.opencv.core.Point;

public class ImageRegion {
    private String id;
    private ArrayList<Point> points;

    public ImageRegion(String id, ArrayList<Point> points) {
        setId(id);
        setPoints(points);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }
}