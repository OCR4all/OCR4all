package de.uniwue.feature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.xml.sax.SAXException;

import de.uniwue.feature.pageXML.Page;
import de.uniwue.feature.pageXML.PageXMLImport;
import de.uniwue.model.TextRegion;

/** 
 * Class for region extraction functionalities
 */
public class RegionExtractor {

    public static final String OUTPUT_FORMAT = ".png";
    public static final String SEPARATOR = "__";
    public static final int RO_ID_LENGTH = 3;

    /** 
     * Exports the segments of a page to an xml file
     * 
     * @param xmlPath Path to the xml file
     * @param imagePath Path to the image file
     * @param useAvgBgd parameter to use AVGBgD
     * @param spacing spacing parameter
     * @param outputFolder path to the output folder
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     */
    public static List<String> extractSegments(String xmlPath, String imagePath, int spacing,
            String outputFolder) throws ParserConfigurationException, SAXException, IOException {
        // List of extracted regions
        List<String> regions = new ArrayList<String>();
        Page page = PageXMLImport.readPageXML(xmlPath);
        //maybe use flags if regions are extracted from binary/grayscale (which should be the case)
        final Mat image = Imgcodecs.imread(imagePath);

        //maybe some checks needed. image.width == 0 etc.
        String pageIdentifier = page.getImageFilename().substring(0,
                page.getImageFilename().lastIndexOf("."));

        outputFolder += pageIdentifier + File.separator;
        new File(outputFolder).mkdirs();

        for (TextRegion region : page.getTextRegions()) {
            String outputFileName = pageIdentifier + SEPARATOR;

            String index = page.getReadingOrder().findIndex(region.getId());

            if (index != null) {
                String id = index.replace("r", "");

                while (id.length() < RO_ID_LENGTH) {
                    id = "0" + id;
                }

                outputFileName += id + SEPARATOR + region.getType().toString()
                        + OUTPUT_FORMAT;
            }  else {
            	continue;
            }

            String outputPath = outputFolder + outputFileName;
            boolean status = saveImage(region.getPoints(), image, 
            		spacing, outputPath);
            if(status == true)
                regions.add(outputPath);
        }

        image.release();
        // force garbage collection, if necessary; maybe somewhere else and not after every image
        // System.gc();
		return regions;
    }

    /** 
     * Writes the .offset information to a file
     * @param pointList points of a region
     * @param image Mat structure from the image
     * @param spacing Spacing parameter of region extractor
     * @param outputPath Path, where the file should be stored
     * @throws IOException
     */
    public static boolean saveImage(ArrayList<Point> pointList, final Mat image,
            int spacing,
            String outputPath) throws IOException {
        Scalar avgBgd = new Scalar(255, 255, 255);

        final MatOfPoint points = new MatOfPoint(
                pointList.toArray(new Point[pointList.size()]));
        Rect rect = Imgproc.boundingRect(points);

        final Mat mask = new Mat(image.size(), CvType.CV_8UC1, new Scalar(0));
        final Mat bgd = new Mat(image.size(), image.type(), avgBgd);

        final ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        contours.add(points);
        Imgproc.drawContours(mask, contours, -1, new Scalar(255), -1);

        for(final MatOfPoint contour : contours){
            contour.release();
        }

        image.copyTo(bgd, mask);

        if (rect.x < 0 || rect.y < 0 || rect.br().x > image.width() - 1 || rect
                .br().y > image.height() - 1) {
            System.out.println("Rect out of range! Skipping region! "
                    + outputPath);
            return false;
        }

        String offsetPath = FilenameUtils.removeExtension(outputPath)
                + ".offset";
        File offsetFile = new File(offsetPath);

        FileUtils.writeStringToFile(offsetFile, (rect.x - spacing) + ","
                + (rect.y - spacing), "UTF-8");

        Mat result = null;

        if (spacing > 0) {
            Rect newRect = new Rect(new Point(rect.x - spacing, rect.y
                    - spacing), new Point(rect.br().x + spacing, rect.br().y
                    + spacing));
            if (newRect.x >= 0 && newRect.y >= 0
                    && newRect.br().x <= bgd.width() - 1
                    && newRect.br().y <= bgd.height() - 1) {
                result = new Mat(bgd, newRect);
            } else {
                result = new Mat(newRect.size(), bgd.type(), avgBgd);
                final Mat submat = result.submat(new Rect(spacing, spacing,
                        rect.width, rect.height));
                final Mat tempResult = new Mat(bgd, rect);
                tempResult.copyTo(submat);

                releaseAll(submat, tempResult);
            }
        } else {
            result = new Mat(bgd, rect);
        }

        Imgcodecs.imwrite(outputPath, result);
        releaseAll(mask, bgd, result);
        return true;
    }

    /**
     * Frees memory of the mat object which was allocated by the imread method
     * @param mats objects which should get released
     */
    public static void releaseAll(final Mat... mats) {
        for(final Mat mat : mats) {
            if (mat != null) {
                mat.release();
            }
        }
    }

    /**
     * Calculates the average color of the channels in an image
     * @param original Mat object of the image
     * @return average background color
     */
    public static double[] calcAverageBackground(final Mat original) {
        if (original.channels() == 1) {
            return calcAverageBackgroundGray(original);
        }
        
        final Mat gray = new Mat();
        Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);
        final Mat binary = new Mat();
        Imgproc.threshold(gray, binary, -1, 255, Imgproc.THRESH_OTSU);

        double sumRed = 0;
        double sumGreen = 0;
        double sumBlue = 0;
        int cnt = 0;

        for (int y = 0; y < binary.rows(); y++) {
            for (int x = 0; x < binary.cols(); x++) {
                if (binary.get(y, x)[0] > 0) {
                    double[] colors = original.get(y, x);
                    sumRed += colors[2];
                    sumGreen += colors[1];
                    sumBlue += colors[0];
                    cnt++;
                }
            }
        }
        double[] avgBackground = { sumBlue / cnt, sumGreen / cnt, sumRed / cnt };

        releaseAll(gray, binary);
        
        return avgBackground;
    }

    /**
     * Calculates the average background color of a gray image
     * @param gray Mat object of the grayscale image
     * @return average background color
     */
    public static double[] calcAverageBackgroundGray(final Mat gray) {
        final Mat binary = new Mat();
        Imgproc.threshold(gray, binary, -1, 255, Imgproc.THRESH_OTSU);

        double sum = 0;
        int cnt = 0;

        for (int y = 0; y < binary.rows(); y++) {
            for (int x = 0; x < binary.cols(); x++) {
                if (binary.get(y, x)[0] > 0) {
                    double[] colors = gray.get(y, x);
                    sum += colors[0];
                    cnt++;
                }
            }
        }

        double[] avgBackground = { sum / cnt };

        releaseAll(binary);
        
        return avgBackground;
    }
}
