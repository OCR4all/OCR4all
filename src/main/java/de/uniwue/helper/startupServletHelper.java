package de.uniwue.helper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class startupServletHelper implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("Stopped");
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        // Load OpenCV library (!important)
        loadNativeLibrary();
        System.out.println("Server initialized");
    }

    public static void loadNativeLibrary() {
        nu.pattern.OpenCV.loadShared();
    }
}
