package de.uniwue.feature;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Converter class for converting .pdf files to .png files
 */
public class PdfToImageConverter {
    //default dpi is set to 300
    /**
     * default dpi for rendering is set to 300
     */
    private static int pdfdpi = 300;

  /*  public static void main(String [] args) {
        String sourcePath = "sample.pdf";
        convertPDF(sourcePath);
    }*/

    /**
     * Constructor
     */
    public PdfToImageConverter() {
    }

    /**
     * Converts the .pdf from the given path to seperate .png files in the parent directory
     * Blank pages will not be converted in this process
     * @param sourceDir Directory of source .pdf
     */
    public static void convertPDF(String sourceDir, boolean deleteBlank) {

        File sourceFile = new File(sourceDir);
        String destinationDir = sourceFile.getPath();
        File destinationFile = new File(destinationDir);

        if(!destinationFile.exists()) {
            destinationFile.mkdir();
            System.out.println("Folder Created -> " +destinationFile.getAbsolutePath());
        }

        if(sourceFile.exists()) {
            try {
                        PDDocument document = PDDocument.load(sourceFile);
                        PDFRenderer renderer = new PDFRenderer(document);
                        int pageCounter = 0;
                        for(PDPage page : document.getPages()) {

                            //page number parameter is zero based
                            BufferedImage img = renderer.renderImageWithDPI(pageCounter, pdfdpi, ImageType.RGB);

                            if(deleteBlank) {
                                //check if image is blank page
                                if (!isBlank(img)) {
                                    //suffix in filename will be used as file format
                                    ImageIOUtil.writeImage(img, String.format("%04d", (pageCounter++) + 1) + ".png", pdfdpi);
                                }
                            }else {
                                ImageIOUtil.writeImage(img, String.format("%04d", (pageCounter++) + 1) + ".png", pdfdpi);
                            }
                        }
                        document.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

        } else {
            System.err.println(sourceFile.getName() + " does not exist");
        }

    }

    /**
     * Checks if rendered Image is blank white or light-gray
     * @param img rendered Image from .pdf
     * @return TRUE if Page is blank
     */
    private static boolean isBlank(BufferedImage img) {
        long count = 0;
        int height = img.getHeight();
        int width = img.getWidth();
        Double areaFactor = (width * height) * 0.99;

        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                Color c = new Color(img.getRGB(x,y));
                //verify light gray and white
                if (c.getRed() == c.getGreen() && c.getRed() == c.getBlue()
                        && c.getRed() >= 248) {
                    count++;
                }
            }
        }

        if(count >= areaFactor) {
            return true;
        } else{
            return false;
        }
    }

    /**
     * Setter for changing the default DPI value of 300
     * @param newDPI new DPI value
     */
    public static void setDPI(int newDPI) {
        pdfdpi = newDPI;
    }

    public static int getDPI() {
        return pdfdpi;
    }

}
