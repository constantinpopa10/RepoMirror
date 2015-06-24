package org.alfresco.repomirror.web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

public class WebApp implements WebApplicationInitializer
{
    public static final String SEPARATOR = "/";
    public static final String PROP_APP_CONTEXT_PATH = "app.contextPath";
    public static final String PROP_APP_DIR = "app.dir";
    public static final String PROP_APP_RELEASE = "app.release";
    public static final String PROP_APP_DESCRIPTION = "app.description";
    public static final String PROP_APP_SCHEMA = "app.schema";
    public static final String PROP_APP_INHERITANCE = "app.inheritance";
    public static final String PROP_SYSTEM_CAPABILITIES = "system.capabilities";

    @Override
    public void onStartup(ServletContext container)
    {
        // Grab the driver capabilities, otherwise just use the java version
        String javaVersion = System.getProperty("java.version");
        String systemCapabilities = System.getProperty(PROP_SYSTEM_CAPABILITIES, javaVersion);

        String appDir = new File(".").getAbsolutePath();
        String appContext = container.getContextPath();
        String appName = container.getContextPath().replace(SEPARATOR, "");

        // Create an application context (don't start, yet)
        XmlWebApplicationContext ctx = new XmlWebApplicationContext();
        ctx.setConfigLocations(
                new String[]
                        {
                        "classpath:app-context.xml"
                        });

        // Pass our properties to the new context
//        Properties ctxProperties = new Properties();
//        {
//            ctxProperties.put(PROP_SYSTEM_CAPABILITIES, systemCapabilities);
//            ctxProperties.put(PROP_APP_CONTEXT_PATH, appContext);
//            ctxProperties.put(PROP_APP_DIR, appDir);
//        }
        ConfigurableEnvironment ctxEnv = ctx.getEnvironment();
//        ctxEnv.getPropertySources().addFirst(
//                new PropertiesPropertySource(
//                        appName,
//                        ctxProperties));
        // Override all properties with system properties
        ctxEnv.getPropertySources().addFirst(
                new PropertiesPropertySource(
                        "system",
                        System.getProperties()));
        // Bind to shutdown
        ctx.registerShutdownHook();

        ContextLoaderListener ctxLoaderListener = new ContextLoaderListener(ctx);
        container.addListener(ctxLoaderListener);

        ServletRegistration.Dynamic jerseyServlet = container.addServlet("jersey-serlvet", SpringServlet.class);
        jerseyServlet.setInitParameter("com.sun.jersey.config.property.packages", "org.alfresco.repomirror.rest");
        jerseyServlet.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        jerseyServlet.addMapping("/api/*");
    }

}
