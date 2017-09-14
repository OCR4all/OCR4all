package de.uniwue.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import de.uniwue.helper.ImageHelper;
import de.uniwue.helper.OverviewHelper;
import de.uniwue.model.PageOverview;

@Controller
public class OverviewController {
    @RequestMapping("/")
    public ModelAndView showOverview() throws IOException {
        ModelAndView mv = new ModelAndView("overview");
        return mv;
    }

    @RequestMapping("/pageOverview")
    public ModelAndView showPageOverview(
                HttpServletRequest request, HttpServletResponse response
            ) throws IOException {
        ModelAndView mv = new ModelAndView("pageOverview");

        String projectDir = (String)request.getSession().getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        String pageId = request.getParameter("pageId");
        if (pageId == null) {
            mv.addObject("error", "No pageId parameter was passed.\n"
                                + "Please return to the Project Overview page.");
            return mv;
        }

        String imageType = (String)request.getSession().getAttribute("imageType");
        OverviewHelper view = new OverviewHelper(projectDir, imageType);
        ImageHelper imageHelper = new ImageHelper();
        try {
            view.initialize(pageId);
        } catch (IOException e) {
            mv.addObject("error", "The page with Id " + pageId + " could not be accessed on the filesystem.\n"
                                + "Please return to the Project Overview page.");
            return mv;
        }

        String pageImage = pageId + ".png";
        PageOverview pageOverview = view.getOverview().get(pageImage);
        mv.addObject("pageOverview", pageOverview);

        Map<String,String> pageImages = new HashMap<String, String>();
        File f =  new File(projectDir + File.separator + "Original" + File.separator + pageImage);
        pageImages.put("Original", imageHelper.encodeFileToBase64Binary(f));
        String [] preprocesSteps = {"Binary", "Despeckled", "Gray"};
        for (String pPS: preprocesSteps) {
            f = new File(projectDir + File.separator + "PreProc" + File.separator
                    + pPS + File.separator + pageImage);
            if (f.exists())
                pageImages.put(pPS, imageHelper.encodeFileToBase64Binary(f));
        }
        mv.addObject("image", pageImages);
        mv.addObject("segments",view.pageContent(pageImage));

        return mv;
    }

    @RequestMapping(value = "/ajax/overview/list" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<PageOverview> jsonOverview(
                @RequestParam("projectDir") String projectDir,
                @RequestParam("imageType") String imageType,
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        // Store project directory in session (serves as entry point)
        session.setAttribute("projectDir", projectDir);
        session.setAttribute("imageType", imageType);
        OverviewHelper view = new OverviewHelper(projectDir,imageType);
        try {
            view.initialize();
        } catch (IOException e) {
            // To trigger AJAX fail (and therefore show errors)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // @RequestMapping automatically transforms object to json format
        return new ArrayList<PageOverview>(view.getOverview().values());
    }
}
