package de.uniwue.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.uniwue.helper.GenericHelper;

/**
 * Controller class for all pages that provides general functionalities
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
 class GenericController {
    /**
     * Response to the request to return all pageIds of the specified imageType
     *
     * @param imageType Type of the image e.g.(gray, binary, original)
     * @param session Session of the user
     * @param response Response to the request
     * @return Array of page IDs
     */
    @RequestMapping(value = "/ajax/generic/pagelist", method = RequestMethod.GET)
    public @ResponseBody ArrayList<String> getPageList(
                @RequestParam("imageType") String imageType,
                HttpSession session, HttpServletResponse response
            ) {
        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null || projectDir.isEmpty())
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        ArrayList<String> pageIds = null;
        GenericHelper genericHelper = new GenericHelper(projectDir);
        try {
            pageIds = genericHelper.getPageList(imageType);
        }
        catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return pageIds;
    }
}
