package de.uniwue.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import de.uniwue.helper.OverviewHelper;
import de.uniwue.helper.PreprocessingHelper;
import de.uniwue.model.PageOverview;

/**
 * Controller class for pages of preprocessing module
 * Use response.setStatus to trigger AJAX fail (and therefore show errors)
 */
@Controller
public class PreprocessingController {
    /**
     * Response to the request to send the content of the /Preprocessing page
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return Returns the content of the /Preprocessing page with the specific pageId
     */
    @RequestMapping("/Preprocessing")
    public ModelAndView showPreprocessing(
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        ModelAndView mv = new ModelAndView("preprocessing");

        String projectDir = (String)session.getAttribute("projectDir");
        if (projectDir == null) {
            mv.addObject("error", "Session expired.\nPlease return to the Project Overview page.");
            return mv;
        }

        return mv;
    }

    /**
     * Response to the request to return the preprocessing status and output information
     *
     * @param session Session of the user
     * @param response Response to the request
     * @return
     */
    @RequestMapping(value = "/ajax/preprocessing/execute", method = RequestMethod.GET)
    public @ResponseBody StreamingResponseBody executePreprocessing(
                HttpSession session, HttpServletResponse response
            ) throws IOException {
        String projectDir = (String) session.getAttribute("projectDir");
        if (projectDir == null || projectDir.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        PreprocessingHelper preproHelper = new PreprocessingHelper(projectDir);
        session.setAttribute("preproHelper", preproHelper);
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream out) throws IOException {
                // Stream seems to flush initially at ~2kb.
                // This is a workaround to get the important content ASAP.
                // TODO: Verify or replace this solution (maybe with Websockets)
                for(int i = 0; i < 1000; i++ ) {
                    out.write(" ".getBytes());
                }

                // Pass OutputStream to allow PreprocessingHelper to update page content
                preproHelper.preprocessAllPages(out);
            }
        };
    }
    @RequestMapping(value = "/ajax/preprocessing/progress" , method = RequestMethod.GET)
    public @ResponseBody int jsonOverview( 
                HttpSession session, HttpServletResponse response
            ) throws IOException {

        PreprocessingHelper preproHelper = (PreprocessingHelper) session.getAttribute("preproHelper");
        return preproHelper.getProgress();
    }
}
