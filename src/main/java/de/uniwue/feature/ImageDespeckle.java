package de.uniwue.feature;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ImageDespeckle {
    /**
     * Constructor
     */
    public ImageDespeckle() {
    }

    /**
     * Despeckling of a Binary image
     *
     * @param binary Mat of the binary image
     * @param maxContourRemovalSize Maximum size of the contours to be removed
     * @param illustrationType Standard: the result image shows the resulting binary image | 
     *                         Marked:  the result image shows the resulting binary image and additionally represents the removed contours
     * @return Resulting binary image
     */
    public Mat despeckle(Mat binary, double maxContourRemovalSize, String illustrationType) {
        // Convert to gray channel only (binary images sometimes seem to have RGB channels)
        Mat bwImage = new Mat();
        Imgproc.cvtColor(binary, bwImage, Imgproc.COLOR_RGB2GRAY);

        Mat inverted = new Mat();
        Core.bitwise_not(bwImage, inverted);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(inverted, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat result = bwImage.clone();
        if (contours.size() > 1) {
            ArrayList<MatOfPoint> toRemove = new ArrayList<MatOfPoint>();
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area < maxContourRemovalSize) {
                    toRemove.add(contour);
                }
            }

            if (illustrationType.equals("marked")) {
                // Convert to BGR image to be able to draw contours in red
                Imgproc.cvtColor(result, result, Imgproc.COLOR_GRAY2BGR);
                Imgproc.drawContours(result, toRemove, -1, new Scalar(0, 0, 255), -1);
            }
            else {
                Imgproc.drawContours(result, toRemove, -1, new Scalar(255), -1);
            }
        }

        return result;
    }
}
