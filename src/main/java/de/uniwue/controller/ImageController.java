package de.uniwue.controller;

import java.io.IOException;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uniwue.helper.ImageHelper;


/**
 * Controller class for image based request/response functionality
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class ImageController {
    /**
     * Response to the request to return the specified page image as base64 string
     *
     * @param pageId Identifier of the page (e.g 0002)
     * @param imageId Image identifier (Original, Gray or Despeckled)
     * @param session Session of the user
     * @param response Response to the request
     * @param request Request
     * @return Returns the required image as a base64 string
     * @throws IM4JavaException 
     * @throws InterruptedException 
     * @throws MagickException 
     */
    @RequestMapping(value = "/ajax/image/page", method = RequestMethod.GET)
    public @ResponseBody String getImageOfPage(
                @RequestParam("pageId") String pageId,
                @RequestParam("imageId") String imageId, HttpSession session, HttpServletResponse response, HttpServletRequest request
            ) throws IOException, InterruptedException {

        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null || projectDir.isEmpty() || pageId == null || pageId.isEmpty()
                || imageId == null || imageId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        String image64 = null;
        try {
            ImageHelper imageHelper = new ImageHelper(projectDir);
            //imageHelper.setDimension(new Dimension(516,664));
            String height = request.getParameter("height");
            String width = request.getParameter("width");
            if (height != null)
                imageHelper.setHeight(Integer.parseInt(height));
            if (width != null)
                imageHelper.setHeight(Integer.parseInt(width));
            image64 = imageHelper.getPageImage(pageId, imageId);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        if (image64 == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return image64;
    }

    /**
     * Response to the request to return the specified segment page image as base64 string
     *
     * @param pageId Identifier of the page (e.g 0002)
     * @param imageId Identifier of the segment (e.g 0002__000__paragraph)
     * @param session Session of the user
     * @param response Response to the request
     * @param request Request
     * @return Returns the required image as a base64 string
     */
    @RequestMapping(value = "/ajax/image/segment", method = RequestMethod.GET)
    public @ResponseBody String getImageOfSegment(
                @RequestParam("pageId") String pageId,
                @RequestParam("imageId") String imageId,
                HttpSession session, HttpServletResponse response, HttpServletRequest request
            ) throws IOException {
        String projectDir = (String) session.getAttribute("projectDir");
        String imageType = session.getAttribute("imageType").toString();
        if (projectDir == null || projectDir.isEmpty() || imageType == null || imageType.isEmpty()
                || pageId == null || pageId.isEmpty() || imageId == null || imageId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        String image64 = null;
        try {
            ImageHelper imageHelper = new ImageHelper(projectDir);
            String height = request.getParameter("height");
            String width = request.getParameter("width");
            if (height != null)
                imageHelper.setHeight(Integer.parseInt(height));
            if (width != null)
                imageHelper.setHeight(Integer.parseInt(width));
            image64 = imageHelper.getSegmentImage(pageId, imageId, imageType);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        if (image64 == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return image64;
    }

    /**
     * Response to the request to return the specified segment page image as base64 string
     *
     * @param pageId Identifier of the page (e.g 0002)
     * @param segmentId Identifier of the segment (e.g 0002__000__paragraph)
     * @param imageId Identifier of the line (e.g 0002__000__paragraph__000)
     * @param session Session of the user
     * @param response Response to the request
     * @param request Request
     * @return Returns the required image as a base64 string
     */
    @RequestMapping(value = "/ajax/image/line", method = RequestMethod.GET)
    public @ResponseBody String getImageOfLine(
                @RequestParam("pageId") String pageId,
                @RequestParam("segmentId") String segmentId,
                @RequestParam("imageId") String imageId,
                HttpSession session, HttpServletResponse response, HttpServletRequest request
            ) throws IOException {
        String projectDir = (String) session.getAttribute("projectDir");
        String imageType = session.getAttribute("imageType").toString();
        if (projectDir == null || projectDir.isEmpty() || imageType == null || imageType.isEmpty()
                || pageId == null || pageId.isEmpty() || segmentId == null || segmentId.isEmpty()
                || imageId == null || imageId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        String image64 = null;
        try {
            ImageHelper imageHelper = new ImageHelper(projectDir);
            image64 = imageHelper.getLineImage(pageId, segmentId, imageId, imageType);
            String height = request.getParameter("height");
            String width = request.getParameter("width");
            if (height != null)
                imageHelper.setHeight(Integer.parseInt(height));
            if (width != null)
                imageHelper.setHeight(Integer.parseInt(width));
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        if (image64 == null)
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return image64;
    }

    /**
     * Response to the request to return a list of all binary images as base64 strings
     *
     * @param imageType Type of the images in the list
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns a list of page IDs with their images as base64 string
     */
    @RequestMapping(value = "/ajax/image/list", method = RequestMethod.GET)
    public @ResponseBody TreeMap<String, String> getBinaryImageList(
                @RequestParam("imageType") String imageType,
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null || projectDir.isEmpty() || imageType == null || imageType.isEmpty())
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        TreeMap<String, String> imageList = new TreeMap<String, String>();
        try {
            ImageHelper imageHelper = new ImageHelper(projectDir);
            imageList = imageHelper.getImageList("Binary");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return imageList;
    }
}
