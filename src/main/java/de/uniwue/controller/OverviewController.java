package de.uniwue.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.OverviewHelper;
import de.uniwue.model.PageOverview;

/**
 * Controller class for pages of overview module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class OverviewController {
    /**
     * Response to the request to send content of the project root
     *
     * @return Returns the content of the project overview page
     */
    @RequestMapping("/")
    public ModelAndView showOverview() {
        ModelAndView mv = new ModelAndView("overview");
        return mv;
    }

    /**
     * Response to the request to send the content of the /pageOverview page
     *
     * @param pageId Identifier of the page (e.g 0002)
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the content of the /pageOverview page with the specific pageId
     */
    @RequestMapping("/pageOverview")
    public ModelAndView showPageOverview(
                @RequestParam("pageId") String pageId,
                HttpSession session, HttpServletResponse response
            ) {
        ModelAndView mv = new ModelAndView("pageOverview");

        if (pageId.isEmpty()) {
            mv.addObject("error", "No pageId parameter was passed.\n"
                                + "Please return to the Project Overview page.");
            return mv;
        }

        OverviewHelper overviewHelper = (OverviewHelper) session.getAttribute("overviewHelper");
        if (overviewHelper == null) {
            mv.addObject("error", "Project could not be loaded.\n"
                                + "Please return to the Project Overview page.");
            return mv;
        }

        try {
            overviewHelper.initialize(pageId);
        } catch (IOException e) {
            mv.addObject("error", "The page with Id " + pageId + " could not be accessed on the filesystem.\n"
                                + "Please return to the Project Overview page.");
            return mv;
        }

        String pageImage = pageId + ".png";
        mv.addObject("pageOverview", overviewHelper.getOverview().get(pageImage));
        try {
            mv.addObject("segments", overviewHelper.pageContent(pageImage));
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return mv;
    }

    /**
     * Response to the request to send the process status of every page
     *
     * @param projectDir Absolute path to the project
     * @param imageType Project type (Binary or Gray)
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the status of every page of the project
     */
    @RequestMapping(value = "/ajax/overview/list" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<PageOverview> jsonOverview(
                @RequestParam("projectDir") String projectDir,
                @RequestParam("imageType") String imageType,
                HttpSession session, HttpServletResponse response
            ) {
        OverviewHelper overviewHelper = (OverviewHelper) session.getAttribute("overviewHelper");
        if (overviewHelper == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return new ArrayList<PageOverview>();
        }

        try {
            overviewHelper.initialize();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // @RequestMapping automatically transforms object to json format
        return new ArrayList<PageOverview>(overviewHelper.getOverview().values());
    }

    /**
     * Response to the request to check the filenames
     * !!! IMPORTANT !!!
     * This function serves as entry point and manages the overview helper
     *
     * @param projectDir Absolute path to the project
     * @param imageType Project type (Binary or Gray)
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the status of the filename check
     */
    @RequestMapping(value ="/ajax/overview/check" , method = RequestMethod.GET)
    public @ResponseBody boolean checkFiles(
            @RequestParam("projectDir") String projectDir,
            @RequestParam("imageType") String imageType,
            HttpSession session, HttpServletResponse response
        ) {
        // Add file separator to end of the path (for usage in JSP files)
        if (!projectDir.endsWith(File.separator))
            projectDir = projectDir + File.separator;

        // Fetch old project specific session variables
        String projectDirOld = (String) session.getAttribute("projectDir");
        String imageTypeOld  = (String) session.getAttribute("imageType");
        // Store project directory in session (serves as entry point)
        session.setAttribute("projectDir", projectDir);
        session.setAttribute("imageType", imageType);

        // Keep a single helper object in session (change if not existing or project is changed)
        OverviewHelper overviewHelper = (OverviewHelper) session.getAttribute("overviewHelper");
        if (overviewHelper == null || projectDirOld != projectDir || imageTypeOld != imageType) {
            overviewHelper = new OverviewHelper(projectDir, imageType);
            session.setAttribute("overviewHelper", overviewHelper);
        }

        boolean fileRenameRequired = overviewHelper.checkFiles();

        // @RequestMapping automatically transforms object to json format
        return fileRenameRequired;
    }

    /**
     * Response to rename the filenames according to the project standard
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value ="/ajax/overview/rename" , method = RequestMethod.GET)
    public @ResponseBody void renameFiles(
            HttpSession session, HttpServletResponse response
        ) {
        OverviewHelper overviewHelper = (OverviewHelper) session.getAttribute("overviewHelper");
        if (overviewHelper == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        try {
            overviewHelper.renameFiles();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
