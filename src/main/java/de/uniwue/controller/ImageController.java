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
        if (projectDir == null) {
            // To trigger AJAX fail (and therefore show errors)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        ImageHelper imageHelper = new ImageHelper();
        //TODO: Load image and send it as Base64 String

        return imageHelper.getImage(projectDir, pageId, imageId);
    }
}
