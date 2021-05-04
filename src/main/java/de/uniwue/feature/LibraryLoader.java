package de.uniwue.feature;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Class to load shared libraries
 */
@WebListener
public class LibraryLoader implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent arg0) { }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        // Load OpenCV library
        try {
            nu.pattern.OpenCV.loadShared();
        } catch (UnsatisfiedLinkError ule) {
            if(!ule.getMessage().contains("already loaded")) throw ule;
        }
    }
}
