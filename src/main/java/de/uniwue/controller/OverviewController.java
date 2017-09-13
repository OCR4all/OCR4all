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
    public ModelAndView showPageOverview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView("pageOverview");

        String projectDir = request.getSession().getAttribute("projectDir").toString();
        String pageId = request.getParameter("pageId")+".png";
        OverviewHelper view = new OverviewHelper(projectDir);
        try {
            view.initialize();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        PageOverview pageOverview = view.getOverview().get(pageId);
        mv.addObject("pageOverview", pageOverview);

        Map<String,String> image = new HashMap<String, String>();
        File f =  new File(projectDir+File.separator+"Original"+File.separator+pageId);
        image.put("Original", view.encodeFileToBase64Binary(f));
        String [] preprocesSteps = {"Binary","Despeckled","Gray"};
        for (String i: preprocesSteps) {
            f =  new File(projectDir+File.separator+"PreProc"+File.separator+i+File.separator+pageId);
            if (f.exists())
                image.put(i, view.encodeFileToBase64Binary(f));
        }
        mv.addObject("image", image);

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

        OverviewHelper view = new OverviewHelper(projectDir);
        try {
            view.initialize();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // @RequestMapping automatically transforms object to json format
        return new ArrayList<PageOverview>(view.getOverview().values());
    }
}
