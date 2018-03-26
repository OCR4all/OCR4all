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
     * Check if mandatory session variables are set correctly
     *
     * @param session Session of the user
     * @param response response to the request
     * @return
     */
    public static boolean isSessionValid(HttpSession session, HttpServletResponse response) {
        String projectDir = (String) session.getAttribute("projectDir");
        String projectImageType = (String) session.getAttribute("imageType");
        if (projectDir == null || projectDir.isEmpty() || projectImageType == null || projectImageType.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
        return true;
    }

    /**
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public GenericHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (isSessionValid(session, response) == false)
            return null;

        // Keep a single helper object in session
        GenericHelper genericHelper = (GenericHelper) session.getAttribute("genericHelper");
        if (genericHelper == null) {
            genericHelper = new GenericHelper(session.getAttribute("projectDir").toString());
            session.setAttribute("genericHelper", genericHelper);
        }
        return genericHelper;
    }

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
        GenericHelper genericHelper = provideHelper(session, response);
        if (genericHelper == null)
            return null;

        ArrayList<String> pageIds = null;
        try {
            pageIds = genericHelper.getPageList(imageType);
        }
        catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return pageIds;
    }

    /**
     * Response to the request to check if the imageType directory exists
     *
     * @param imageType Type of the image directory (original, gray, binary, despeckled, OCR)
     * @param session Session of the user
     * @param response Response to the request
     * @return Status of the check
     */
    @RequestMapping(value = "/ajax/generic/checkDir", method = RequestMethod.GET)
    public @ResponseBody boolean checkIfImageDirectoryExists(
                @RequestParam("imageType") String imageType,
                HttpSession session, HttpServletResponse response
            ) {
        GenericHelper genericHelper = provideHelper(session, response);
        if (genericHelper == null)
            return false;

        return genericHelper.checkIfImageDirectoryExists(imageType);
    }

    /**
     * Response to the request to return the number of logical threads of the system
     *
     * @param session Session of the user
     * @return Number of logical threads
     */
    @RequestMapping(value = "/ajax/generic/threads" , method = RequestMethod.GET)
    public @ResponseBody int hostProcessors(HttpSession session) {
        return GenericHelper.getLogicalThreadCount();
    }
}
