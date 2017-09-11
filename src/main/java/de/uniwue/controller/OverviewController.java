package de.uniwue.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
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
    OverviewHelper view;
    String ProjectDir;
    @RequestMapping("/")
    public ModelAndView showOverview() throws IOException {
        ModelAndView mv = new ModelAndView("overview");
        return mv;
    }

    @RequestMapping("/pageOverview")
    public ModelAndView showPageOverview(HttpServletRequest request) throws IOException {
        ModelAndView mv = new ModelAndView("pageOverview");
        String pageId = request.getParameter("pageId")+".png";
        PageOverview pageOverview=view.getOverview().get(pageId);
        Map<String,String> image = new HashMap<String, String>();
        File f =  new File(request.getSession().getAttribute("projectDir").toString()+File.separator+"Original"+File.separator+pageId);
        image.put("Orginal",view.encodeFileToBase64Binary(f));
        String [] preprocesSteps = {"Binary","Despeckled","Gray"};
        for (String i: preprocesSteps) {
            f =  new File(request.getSession().getAttribute("projectDir").toString()+File.separator+"PreProc"+File.separator+i+File.separator+pageId);
            
            if( f.exists())
                image.put(i,view.encodeFileToBase64Binary(f));
        }
        mv.addObject("preprocessed", pageOverview.isPreprocessed());
        mv.addObject("segmented", pageOverview.isSegmented());
        mv.addObject("segmentsExtracted", pageOverview.isSegmentsExtracted());
        mv.addObject("linesExtracted", pageOverview.isLinesExtracted());
        mv.addObject("hasGT", pageOverview.isHasGT());
        mv.addObject("image", image);
        return mv;
    } 

    @RequestMapping(value = "/ajax/overview/list" , method = RequestMethod.GET)
    public @ResponseBody ArrayList<PageOverview> jsonOverview(@RequestParam("projectDir") String projectDir, HttpSession session, HttpServletResponse response) throws IOException{
        // Store project directory in session (serves as entry point)
        session.setAttribute("projectDir", projectDir);
        ProjectDir = projectDir;
        view = new OverviewHelper(projectDir);
        try {
            view.initialize();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // @RequestMapping automatically transforms object to json format
        return new ArrayList<PageOverview>(view.getOverview().values());
    }
}
