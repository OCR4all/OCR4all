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

        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        if (pageId.isEmpty()) {
            mv.addObject("error", "No pageId parameter was passed.\n"
                                + "Please return to the Project Overview page.");
            return mv;
        }

        String imageType = (String)session.getAttribute("imageType");
        OverviewHelper view = new OverviewHelper(projectDir, imageType);
        try {
            view.initialize(pageId);
        } catch (IOException e) {
            mv.addObject("error", "The page with Id " + pageId + " could not be accessed on the filesystem.\n"
                                + "Please return to the Project Overview page.");
            return mv;
        }

        String pageImage = pageId + ".png";
        mv.addObject("pageOverview", view.getOverview().get(pageImage));
        mv.addObject("segments", view.pageContent(pageImage));

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
        OverviewHelper view = new OverviewHelper(projectDir,imageType);
        try {
            view.initialize();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // @RequestMapping automatically transforms object to json format
        return new ArrayList<PageOverview>(view.getOverview().values());
    }

    /**
     * Response to the request to check the filenames
     *
     * @param projectDir Absolute path to the project
     * @param imageType Project type (Binary or Gray)
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the status of the filename check
     */
    @RequestMapping(value ="ajax/overview/check" , method = RequestMethod.GET)
    public @ResponseBody boolean checkFiles(
            @RequestParam("projectDir") String projectDir,
            @RequestParam("imageType") String imageType,
            HttpSession session, HttpServletResponse response
        ) {
        // Add file separator to end of the path (for usage in JSP files)
        if (!projectDir.endsWith(File.separator))
            projectDir = projectDir + File.separator;

        // Store project directory in session (serves as entry point)
        session.setAttribute("projectDir", projectDir);
        session.setAttribute("imageType", imageType);

        OverviewHelper view = new OverviewHelper(projectDir,imageType);
        boolean fileRenameRequired = view.checkFiles();

        // @RequestMapping automatically transforms object to json format
        return fileRenameRequired;
    }

    /**
     * Response to rename the filenames according to the project standard
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value ="ajax/overview/rename" , method = RequestMethod.GET)
    public @ResponseBody void renameFiles(
            HttpSession session, HttpServletResponse response
        ) {
        String projectDir = (String) session.getAttribute("projectDir");
        String imageType  = (String) session.getAttribute("imageType");

        OverviewHelper view = new OverviewHelper(projectDir,imageType);
        try {
            view.renameFiles();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
