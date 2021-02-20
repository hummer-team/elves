package io.elves.core.log;

import io.elves.core.ElvesConstants;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.util.Objects;


/**
 * @author edz
 */
public class LogDriverInit {
    private LogDriverInit() {

    }

    public static void init() throws IOException {
        String logFileName = String.format("log4j2-%s.xml"
                , System.getProperty(ElvesConstants.PROFILES_ACTIVE));
        ConfigurationSource source =
                new ConfigurationSource(Objects.requireNonNull(Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(logFileName)));
        Configurator.initialize(null, source);
    }
}
