package de.uniwue.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
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
     * Manages the helper object and stores it in the session
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the helper object of the process
     */
    public OverviewHelper provideHelper(HttpSession session, HttpServletResponse response) {
        if (GenericController.isSessionValid(session, response) == false) {
            return null;
        }

        // Keep a single helper object in session
        OverviewHelper overviewHelper = (OverviewHelper) session.getAttribute("overviewHelper");
        if (overviewHelper == null) {
            overviewHelper = new OverviewHelper(
                session.getAttribute("projectDir").toString(),
                session.getAttribute("imageType").toString()
            );
            session.setAttribute("overviewHelper", overviewHelper);
        }
        return overviewHelper;
    }

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

        OverviewHelper overviewHelper = provideHelper(session, response);
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
            e.printStackTrace();
            return mv;
        }

        String pageImage = pageId + ".png";
        mv.addObject("pageOverview", overviewHelper.getOverview().get(pageImage));
        try {
            mv.addObject("segments", overviewHelper.pageContent(pageId));
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

        return mv;
    }

    /**
     * Response to the request to send the process status of every page
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the status of every page of the project
     */
    @RequestMapping(value = "/ajax/overview/list" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<PageOverview> jsonOverview(HttpSession session, HttpServletResponse response) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null)
            return new ArrayList<PageOverview>();

        try {
            overviewHelper.initialize();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

        // @RequestMapping automatically transforms object to json format
        return new ArrayList<PageOverview>(overviewHelper.getOverview().values());
    }

    /**
     * Response to the request to check the projectDir
     * !!! IMPORTANT !!!
     * This function serves as entry point and manages the overview helper
     *
     * @param projectDir Absolute path to the project
     * @param imageType Project type (Binary or Gray)
     * @param resetSession Triggers the creation and usage of a new session
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the status of the projectDir check
     */
    @RequestMapping(value ="/ajax/overview/checkDir" , method = RequestMethod.GET)
    public @ResponseBody boolean checkDir(
                @RequestParam("projectDir") String projectDir,
                @RequestParam("imageType") String imageType,
                @RequestParam("resetSession") Boolean resetSession,
                HttpSession session, HttpServletResponse response, HttpServletRequest request
            ) {
        // Add file separator to end of the path (for usage in JSP files)
        if (!projectDir.endsWith(File.separator))
            projectDir = projectDir + File.separator;

        // Resets the session, so that all previous moduleHelper attributes are discarded
        if (resetSession == true)
            session.invalidate();

        // Store necessary project related variables in session (serves as entry point)
        HttpSession newSession = request.getSession();
        newSession.setAttribute("projectDir", projectDir);
        newSession.setAttribute("imageType", imageType);
        // Determine and add the name of the project to the session as well (to display on each page)
        String[] projectDirParts = projectDir.substring(0, projectDir.length() - 1).split(Pattern.quote(File.separator));
        newSession.setAttribute("projectName", projectDirParts[projectDirParts.length - 1]);

        OverviewHelper overviewHelper = provideHelper(newSession, response);
        if (overviewHelper == null)
            return false;

        // Always reset progress before loading a new project
        // This simplifies the loading process in the Frontend
        overviewHelper.resetProgress();

        return overviewHelper.checkProjectDir();
    }

    /**
     * Response to the request to validate the project dir
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the validation status of the project dir
     */
    @RequestMapping(value ="/ajax/overview/validate" , method = RequestMethod.GET)
    public @ResponseBody boolean validateProjectDir(HttpSession session, HttpServletResponse response) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null)
            return false;

        return overviewHelper.validateProjectDir();
    }

    /**
     * Response to the request to validate the files of the Project
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the status of the filename check
     */
    @RequestMapping(value ="/ajax/overview/validateProject" , method = RequestMethod.GET)
    public @ResponseBody boolean validateProject(HttpSession session, HttpServletResponse response) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null)
            return false;

        try {
           return overviewHelper.isProjectValid();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Response to the request to check if this project includes legacy (Directory) files
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the status of the filename check
     */
    @RequestMapping(value ="/ajax/overview/isLegacy" , method = RequestMethod.GET)
    public @ResponseBody boolean checkLegacy(HttpSession session, HttpServletResponse response) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null)
            return false;

	   return overviewHelper.isLegacy();
    }

    /**
     * Response to adjust the files according to the project standard
     *
     * @param backupImages Determines if a backup of the image folder is required 
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value ="/ajax/overview/adjustProjectFiles" , method = RequestMethod.POST)
    public @ResponseBody void adjustFiles(
                @RequestParam("backupImages") Boolean backupImages,
                HttpSession session, HttpServletResponse response
            ) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null)
            return;

        try {
            session.setAttribute("projectAdjustment", "Please wait until the project adjustment is finished.");
            overviewHelper.execute(backupImages,false);
            session.setAttribute("projectAdjustment", "");
        } catch (IOException e) {
            // Prevent loading an invalid project
            session.invalidate();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Response to invalidate the session of the user
     * 
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value ="/ajax/overview/invalidateSession" , method = RequestMethod.GET)
    public @ResponseBody void invalidateSession(HttpSession session, HttpServletResponse response) {
        session.invalidate();
    }

    /**
     * Response to list the projects
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value ="/ajax/overview/listProjects" , method = RequestMethod.GET)
    public @ResponseBody TreeMap<String, String> listProjects(HttpSession session, HttpServletResponse response) {
        return OverviewHelper.listProjects();
    }

    /**
     * Response to the request to return the progress status of the adjust files service
     *
     * @param session Session of the user
     * @return Current progress (range: 0 - 100)
     */
    @RequestMapping(value = "/ajax/overview/progress" , method = RequestMethod.GET)
    public @ResponseBody int progress(HttpSession session, HttpServletResponse response) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null)
            return -1;

        return overviewHelper.getProgress();
    }

    /**
     * Response to the request to cancel the overview process
     *
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value = "/ajax/overview/cancel", method = RequestMethod.POST)
    public @ResponseBody void cancel(HttpSession session, HttpServletResponse response) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null)
            return;

        overviewHelper.cancelProcess();
    }

    /**
     * Response to the Request to check the Project Dir for PDFs
     * @param session Session of the user
     * @param response Response to the request
     * @return
     */
    @RequestMapping(value ="/ajax/overview/checkpdf" , method = RequestMethod.GET)
    public @ResponseBody boolean checkPdfConvertable(
            HttpSession session, HttpServletResponse response
    ) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null) {
            return false;
        }

        try {
            session.setAttribute("projectAdjustment", "Please wait until the project adjustment is finished.");
            session.setAttribute("projectAdjustment", "");
            return overviewHelper.checkPdfConvertable();
        } catch (Exception e) {
            // Prevent loading an invalid project
            session.invalidate();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Response to the Request to convert a PDF in to image files
     * @param deleteBlank flag to delete blank pages
     * @param session Session of the user
     * @param response Response to the request
     */
    @RequestMapping(value ="/ajax/overview/convertProjectFiles" , method = RequestMethod.POST)
    public @ResponseBody void convertFiles(
            @RequestParam("deleteBlank") Boolean deleteBlank,
            @RequestParam("dpi") int dpi,
            HttpSession session, HttpServletResponse response
    ) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        //PdfToImageConverter converter = provideConverter(session,response);
        if (overviewHelper == null)
            return;

        try {
            session.setAttribute("projectAdjustment", "Please wait until the project adjustment is finished.");
            overviewHelper.setDPI(dpi);
            overviewHelper.execute(deleteBlank, true);
            session.setAttribute("projectAdjustment", "");
        } catch (Exception e) {
            // Prevent loading an invalid project
            session.invalidate();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }


    /**
     * Response to the Request to export Ground Truth Data from Project
     * @param completeDir determines if complete project GTD will be zipped
     * @param pages selected pages to zip
     * @param binary determines if binary images will be included
     * @param gray determines if grayscale images will be included
     * @param session Session of the user
     * @param response Response to the Request
     */
    @RequestMapping(value ="ajax/overview/exportGtc" , method = RequestMethod.POST)
    public @ResponseBody String exportGtc(
            @RequestParam("completeDir") Boolean completeDir,
            @RequestParam("pages") String pages,
            @RequestParam("binary") Boolean binary,
            @RequestParam("gray") Boolean gray,
            HttpSession session, HttpServletResponse response
    ) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null) {
            return null;
        }

        try {
            session.setAttribute("projectAdjustment", "Please wait until the project adjustment is finished.");

            if(completeDir) {
                overviewHelper.zipDir(binary, gray);
            } else {
                overviewHelper.zipPages(pages, binary, gray);
            }
            session.setAttribute("projectAdjustment", "");
        } catch (Exception e) {
            // Prevent loading an invalid project
            session.invalidate();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
        return overviewHelper.getProjDir();
    }

    /**
     * Response to the Request to check for exportable Ground Truth Data
     * @param session Session of the user
     * @param response Response to the Request
     * @return
     */
    @RequestMapping(value ="ajax/overview/checkGtc" , method = RequestMethod.GET)
    public @ResponseBody boolean checkGtcExportable(
            HttpSession session, HttpServletResponse response
    ) {
        OverviewHelper overviewHelper = provideHelper(session, response);
        if (overviewHelper == null) {
            return false;
        }
        try {
            session.setAttribute("projectAdjustment", "Please wait until the project adjustment is finished.");
            session.setAttribute("projectAdjustment", "");
            return overviewHelper.checkGtcExportable();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
