package de.uniwue.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uniwue.helper.ImageHelper;

@Controller
public class ImageController {
    @RequestMapping(value = "/ajax/image/page" , method = RequestMethod.GET)
    public @ResponseBody String getImageOfPage(
                @RequestParam("pageId") String pageId,
                @RequestParam("imageId") String imageId,
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        String projectDir = (String)session.getAttribute("projectDir");
        // To trigger AJAX fail (and therefore show errors)
        ImageHelper imageHelper = new ImageHelper();
        String image64 = imageHelper.getPageImage(projectDir, pageId, imageId);
        if (image64 == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        if (projectDir == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return image64;
    }

    @RequestMapping(value = "/ajax/image/segment" , method = RequestMethod.GET)
    public @ResponseBody String getImageOfSegment(
                @RequestParam("pageId") String pageId,
                @RequestParam("segmentId") String segmentId,
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        String projectDir = (String)session.getAttribute("projectDir");
        String imageType = session.getAttribute("imageType").toString();
        ImageHelper imageHelper = new ImageHelper();
        String image64 = imageHelper.getSegmentImage(projectDir, pageId, segmentId, imageType);
        // To trigger AJAX fail (and therefore show errors)
        if (image64 == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        if (projectDir == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return image64;
    }

    @RequestMapping(value = "/ajax/image/line" , method = RequestMethod.GET)
    public @ResponseBody String getImageOfLine(
                @RequestParam("pageId") String pageId,
                @RequestParam("segmentId") String segmentId,
                @RequestParam("lineId") String lineId,
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        String projectDir = (String)session.getAttribute("projectDir");
        String imageType = session.getAttribute("imageType").toString();
        ImageHelper imageHelper = new ImageHelper();
        String image64 = imageHelper.getLineImage(projectDir, pageId, segmentId, lineId, imageType);
        // To trigger AJAX fail (and therefore show errors)
        if (image64 == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        if (projectDir == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return image64;
    }
}
