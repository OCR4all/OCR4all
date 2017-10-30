package de.uniwue.feature.pageXML;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opencv.core.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PageXMLImport {
    private static ArrayList<Point> extractPointsOld(Element coords) {
        ArrayList<Point> points = new ArrayList<Point>();
        NodeList pointNodes = coords.getElementsByTagName("Point");

        for (int i = 0; i < pointNodes.getLength(); i++) {
            Element pointElement = (Element) pointNodes.item(i);
            String x = pointElement.getAttribute("x");
            String y = pointElement.getAttribute("y");

            Point point = new Point(Integer.parseInt(x), Integer.parseInt(y));
            points.add(point);
        }

        return points;
    }

    private static ArrayList<Point> extractPoints(Element coords) {
        if (!coords.hasAttribute("points")) {
            return extractPointsOld(coords);
        }

        String pointsString = coords.getAttribute("points");
        ArrayList<Point> points = new ArrayList<Point>();

        boolean finished = false;

        while (!finished) {
            int spacePosition = pointsString.indexOf(" ");

            if (spacePosition > 0) {
                String pointString = pointsString.substring(0, spacePosition);
                pointsString = pointsString.substring(spacePosition + 1);

                String x = pointString.substring(0, pointString.indexOf(","));
                String y = pointString.substring(pointString.indexOf(",") + 1);

                Point newPoint = new Point(Integer.parseInt(x), Integer.parseInt(y));
                points.add(newPoint);
            } else {
                String x = pointsString.substring(0, pointsString.indexOf(","));
                String y = pointsString.substring(pointsString.indexOf(",") + 1);

                Point newPoint = new Point(Integer.parseInt(x), Integer.parseInt(y));
                points.add(newPoint);

                finished = true;
            }
        }

        return points;
    }

    private static TextRegion extractTextRegion(Node regionNode) {
        Element regionElement = (Element) regionNode;
        String id = regionElement.getAttribute("id");
        String type = regionElement.getAttribute("type");

        Element coords = (Element) regionElement.getElementsByTagName("Coords").item(0);
        ArrayList<Point> points = extractPoints(coords);

        TextRegion textRegion = new TextRegion(id, type, points);

        return textRegion;
    }

    private static ImageRegion extractImageRegion(Node regionNode) {
        Element regionElement = (Element) regionNode;
        String id = regionElement.getAttribute("id");

        Element coords = (Element) regionElement.getElementsByTagName("Coords").item(0);
        ArrayList<Point> points = extractPoints(coords);

        ImageRegion imageRegion = new ImageRegion(id, points);

        return imageRegion;
    }

    private static ArrayList<RegionRefIndexed> extractRegionRefIndices(Element orderedGroup) {
        ArrayList<RegionRefIndexed> regionRefIndices = new ArrayList<RegionRefIndexed>();
        NodeList rriNodes = orderedGroup.getElementsByTagName("RegionRefIndexed");

        for (int i = 0; i < rriNodes.getLength(); i++) {
            Element rriElement = (Element) rriNodes.item(i);
            String index = rriElement.getAttribute("index");
            String regionRef = rriElement.getAttribute("regionRef");

            RegionRefIndexed rri = new RegionRefIndexed(index, regionRef);
            regionRefIndices.add(rri);
        }

        return regionRefIndices;
    }

    private static ReadingOrder extractReadingOrder(Element readingOrderElement) {
        if (readingOrderElement == null) {
            return null;
        }

        Element orderedGroup = (Element) readingOrderElement.getElementsByTagName("OrderedGroup").item(0);
        String id = orderedGroup.getAttribute("id");

        ArrayList<RegionRefIndexed> regionRefIndices = extractRegionRefIndices(orderedGroup);
        ReadingOrder readingOrder = new ReadingOrder(id, regionRefIndices);

        return readingOrder;
    }

    public static Page readPageXML(String inputPath) {

        Page page = null;

        try {
            File inputFile = new File(inputPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(inputFile);
            document.getDocumentElement().normalize();

            Element pageInfo = (Element) document.getElementsByTagName("Page").item(0);
            String imageFilename = pageInfo.getAttribute("imageFilename");
            String imageWidth = pageInfo.getAttribute("imageWidth");
            String imageHeight = pageInfo.getAttribute("imageHeight");

            Element readingOrderElement = (Element) document.getElementsByTagName("ReadingOrder").item(0);
            ReadingOrder readingOrder = extractReadingOrder(readingOrderElement);

            NodeList imageNodes = document.getElementsByTagName("ImageRegion");
            NodeList textNodes = document.getElementsByTagName("TextRegion");

            ArrayList<ImageRegion> imageRegions = new ArrayList<ImageRegion>();
            ArrayList<TextRegion> textRegions = new ArrayList<TextRegion>();
            for (int i = 0; i < imageNodes.getLength(); i++) {
                ImageRegion imageRegion = extractImageRegion(imageNodes.item(i));
                imageRegions.add(imageRegion);
            }

            for (int i = 0; i < textNodes.getLength(); i++) {
                TextRegion textRegion = extractTextRegion(textNodes.item(i));
                textRegions.add(textRegion);
            }

            page = new Page(imageFilename, imageWidth, imageHeight, readingOrder, imageRegions, textRegions);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Reading XML file failed!");
        }

        return page;
    }

}
