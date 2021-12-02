package de.uniwue.feature;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Class to despeckle images
 */
public class ImageDespeckle {
    /**
     * Constructor
     */
    public ImageDespeckle() { }

    /**
     * Despeckling of a Binary image
     *
     * @param binary Mat of the binary image
     * @param maxContourRemovalSize Maximum size of the contours to be removed
     * @param illustrationType Standard: the result image shows the resulting binary image | 
     *                         Marked:  the result image shows the resulting binary image and additionally represents the removed contours
     * @return Resulting binary image (new mat)
     */
    public static Mat despeckle(final Mat binary, double maxContourRemovalSize, String illustrationType) {
        // Convert to gray channel only (binary images sometimes seem to have RGB channels)
        final Mat result = new Mat();
        Imgproc.cvtColor(binary, result, Imgproc.COLOR_RGB2GRAY);

        final Mat inverted = new Mat();
        Core.bitwise_not(result, inverted);

        final ArrayList<MatOfPoint> contours = new ArrayList<>();
        final Mat hierarchy = new Mat();
        Imgproc.findContours(inverted, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        inverted.release();
        hierarchy.release();

        if (contours.size() > 1) {
            final ArrayList<MatOfPoint> toRemove = new ArrayList<>();
            for (final MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area < maxContourRemovalSize) {
                    toRemove.add(contour);
                } else {
                    contour.release();
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

            for(final MatOfPoint contour: toRemove){
                contour.release();
            }
        }

        return result;
    }
}
