package io.elves.core.log;

import io.elves.core.ElvesConstants;
import org.apache.logging.log4j.core.config.Configurator;

import java.net.URISyntaxException;


/**
 * @author edz
 */
public class LogDriverInit {
    private LogDriverInit() {

    }

    public static void init() throws URISyntaxException {
        String logConfigFileName = String.format("log4j2-%s.xml"
                , System.getProperty(ElvesConstants.PROFILES_ACTIVE));
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Configurator.initialize(logConfigFileName, cl, cl.getResource(logConfigFileName).toURI());
        cl = cl == null ? ClassLoader.getSystemClassLoader() : cl;
        Thread.currentThread().setContextClassLoader(cl);
    }
}
