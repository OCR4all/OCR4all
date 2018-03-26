package de.uniwue.feature;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.w3c.dom.Document;

import de.uniwue.feature.pageXML.PageXMLWriter;

public class DummySegmentator {
    public static void runDummySegmentation(String inputFolder, String outputFolder) {
        for(File file : new File(inputFolder).listFiles()) {
            String imagePath = file.getAbsolutePath();
            String imageFilename = imagePath.substring(imagePath.lastIndexOf(File.separator) + 1);
            Mat image = Imgcodecs.imread(imagePath);
            if(image.width() == 0)
                continue;
            Document xml = PageXMLWriter.getPageXML(image, imageFilename, "");
            PageXMLWriter.saveDocument(xml, imageFilename, outputFolder);
        }
    }
}
