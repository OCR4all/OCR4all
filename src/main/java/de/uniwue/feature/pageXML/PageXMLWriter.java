package de.uniwue.feature.pageXML;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PageXMLWriter {

    /**
     * PageXML 2017
     *
     * @param document
     * @param coordsElement
     * @param points
     */
    private static void addPoints2017(Document document, Element coordsElement, Point[] points) {
        StringBuilder pointCoords = new StringBuilder();

        for (Point point : points) {
            int x = (int) point.x;
            int y = (int) point.y;

            pointCoords.append(x).append(",").append(y).append(" ");
        }

        pointCoords = new StringBuilder(pointCoords.substring(0, pointCoords.length() - 1));
        coordsElement.setAttribute("points", pointCoords.toString());
    }

    /**
     * PageXML 2010
     *
     * @param document
     * @param coordsElement
     * @param points
     */
    private static void addPoints2010(Document document, Element coordsElement, Point[] points) {
        for (Point point : points) {
            Element pointElement = document.createElement("Point");
            pointElement.setAttribute("x", "" + (int) (point.x));
            pointElement.setAttribute("y", "" + (int) (point.y));
            coordsElement.appendChild(pointElement);
        }
    }

    /**
     * Create pageXML document containing the dummy segmentation for an image
     *
     * @param image
     * @param imageFilename
     * @param pageXMLVersion
     * @return pageXML document or null if parse error
     * @throws ParserConfigurationException
     */
    public static Document getPageXML(final Mat image, String imageFilename, String pageXMLVersion) throws ParserConfigurationException {
        if (!pageXMLVersion.equals("2019-07-15") && !pageXMLVersion.equals("2017-07-15") && !pageXMLVersion.equals("2010-03-19")) {
            pageXMLVersion = "2010-03-19";
        }
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element rootElement = document.createElement("PcGts");
        rootElement.setAttribute("xmlns", "http://schema.primaresearch.org/PAGE/gts/pagecontent/" + pageXMLVersion);
        rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootElement.setAttribute("xsi:schemaLocation",
                "http://schema.primaresearch.org/PAGE/gts/pagecontent/" + pageXMLVersion
                        + " http://schema.primaresearch.org/PAGE/gts/pagecontent/" + pageXMLVersion
                        + "/pagecontent.xsd");

        document.appendChild(rootElement);

        Element metadataElement = document.createElement("Metadata");
        Element creatorElement = document.createElement("Creator");
        Element createdElement = document.createElement("Created");
        Element changedElement = document.createElement("LastChange");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String date = sdf.format(new Date());

        Node creatorTextNode = document.createTextNode("OCR4all");
        Node createdTextNode = document.createTextNode(date);
        Node changedTextNode = document.createTextNode(date);

        creatorElement.appendChild(creatorTextNode);
        createdElement.appendChild(createdTextNode);
        changedElement.appendChild(changedTextNode);

        metadataElement.appendChild(creatorElement);
        metadataElement.appendChild(createdElement);
        metadataElement.appendChild(changedElement);

        rootElement.appendChild(metadataElement);

        Element pageElement = document.createElement("Page");
        pageElement.setAttribute("imageFilename", imageFilename);

        pageElement.setAttribute("imageWidth", "" + image.width());
        pageElement.setAttribute("imageHeight", "" + image.height());
        rootElement.appendChild(pageElement);

        //create and add a single paragraph region covering the entire page
        Element regionElement = document.createElement("TextRegion");
        regionElement.setAttribute("type", "paragraph");
        regionElement.setAttribute("id", "r0");
        Element coordsElement = document.createElement("Coords");

        //tl, tr, br, bl
        Point[] points = new Point[4];
        points[0] = new Point(1, 1);
        points[1] = new Point(image.width() - 2, 1);
        points[2] = new Point(image.width() - 2, image.height() - 2);
        points[3] = new Point(1, image.height() - 2);

        switch (pageXMLVersion) {
            case "2019-07-15":
            case "2017-07-15":
                addPoints2017(document, coordsElement, points);
                break;
            case "2010-03-19":
            default:
                addPoints2010(document, coordsElement, points);
                break;
        }
        regionElement.appendChild(coordsElement);
        pageElement.appendChild(regionElement);

        return document;
    }

    /**
     * Save document to outputFolder
     *
     * @param document
     * @param imageFilename
     * @param outputFolder
     * @throws TransformerException
     */
    public static void saveDocument(Document document, String imageFilename, String outputFolder) throws TransformerException {
        // write content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);

        if (!outputFolder.endsWith(File.separator)) {
            outputFolder += File.separator;
        }

        String xmlFilename = imageFilename.substring(0, imageFilename.lastIndexOf("."));
        String outputPath = outputFolder + xmlFilename + ".xml";
        StreamResult result = new StreamResult(new File(outputPath));
        transformer.transform(source, result);

    }
}
