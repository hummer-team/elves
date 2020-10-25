package io.elves.core.properties;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static io.elves.core.ElvesConstants.DEFAULT_PORT;

/**
 * property
 *
 * @author lee
 */
@Slf4j
public class ElvesProperties {
    private static final InnerElvesProperties ELVES_PROPERTIES = new InnerElvesProperties();

    public static int maxRequestContentSize() {
        return ELVES_PROPERTIES.getMaxRequestContentSize();
    }

    public static int getIdleReadTimeOutSecond() {
        return ELVES_PROPERTIES.getIdleReadTimeOutSecond();
    }

    public static int getIdleWriteTimeoutSecond() {
        return ELVES_PROPERTIES.getIdleWriteTimeoutSecond();
    }

    public static boolean enableH2() {
        return ELVES_PROPERTIES.isH2();
    }

    public static long getBufferSize() {
        return ELVES_PROPERTIES.getBufferSize();
    }

    public static int getIoThread() {
        return ELVES_PROPERTIES.getIoThread();
    }

    public static int getWorkThread() {
        return ELVES_PROPERTIES.getWorkThread();
    }

    public static int getPort() {
        return ELVES_PROPERTIES.getHttpPort();
    }

    public static int getSslPort() {
        return ELVES_PROPERTIES.getSslPort();
    }

    public static void load(String profilesActive) throws IOException {
        if (ELVES_PROPERTIES.isLoad()) {
            return;
        }
        log.debug("elves profiles active {}", profilesActive);
        String fileName = String.format("elves-%s.properties", profilesActive);
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (url == null) {
            throw new IllegalArgumentException(String.format("this profile active %s invalid", profilesActive));
        }
        Properties properties = new Properties(System.getProperties());
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            properties.load(stream);
            parseProperties(properties);
        } catch (IOException e) {
            throw e;
        }
    }

    private static void parseProperties(Properties properties) {
        ELVES_PROPERTIES.setH2(Boolean.parseBoolean(properties.getProperty("elves.h2", "false")));
        ELVES_PROPERTIES.setIoThread(Integer.parseInt(properties.getProperty("elves.ioThread"
                , "" + Runtime.getRuntime().availableProcessors())));
        ELVES_PROPERTIES.setWorkThread(Integer.parseInt(properties.getProperty("elves.workThread"
                , "" + Runtime.getRuntime().availableProcessors() * 2)));
        ELVES_PROPERTIES.setCharset(properties.getProperty("elves.charset", "UTF-8"));
        ELVES_PROPERTIES.setIdleReadTimeOutSecond(
                Integer.parseInt(properties.getProperty("elves.idleReadTimeOutSecond", "45")));
        ELVES_PROPERTIES.setIdleWriteTimeoutSecond(
                Integer.parseInt(properties.getProperty("elves.idleWriteTimeoutSecond", "0")));
        ELVES_PROPERTIES.setBufferSize(Long.parseLong(properties.getProperty("elves.bufferSize"
                , "" + 10 * 1024 * 1024)));
        ELVES_PROPERTIES.setHttpPort(Integer.parseInt(properties.getProperty("elves.port"
                , "" + DEFAULT_PORT)));
        ELVES_PROPERTIES.setSslPort(Integer.parseInt(properties.getProperty("elves.sslPort"
                , "" + (DEFAULT_PORT + 1000))));
        ELVES_PROPERTIES.setMaxRequestContentSize(Integer.parseInt(properties.getProperty("elves.maxRequestContentSize"
                , "" + 10 * 1024 * 1024)));

        ELVES_PROPERTIES.setLoad(true);
    }

    public String getCharset() {
        return ELVES_PROPERTIES.getCharset();
    }

    static class InnerElvesProperties {
        private boolean load;
        private int ioThread;
        private int workThread;
        private String charset;
        private boolean h2;
        private int idleReadTimeOutSecond;
        private int idleWriteTimeoutSecond;
        private long bufferSize;
        private int httpPort;
        private int sslPort;
        private int maxRequestContentSize;

        public boolean isLoad() {
            return load;
        }

        public void setLoad(boolean load) {
            this.load = load;
        }

        public int getMaxRequestContentSize() {
            return maxRequestContentSize;
        }

        public void setMaxRequestContentSize(int maxRequestContentSize) {
            this.maxRequestContentSize = maxRequestContentSize;
        }

        public int getSslPort() {
            return sslPort;
        }

        public void setSslPort(int sslPort) {
            this.sslPort = sslPort;
        }

        public int getHttpPort() {
            return httpPort;
        }

        public void setHttpPort(int httpPort) {
            this.httpPort = httpPort;
        }

        public int getIoThread() {
            return ioThread;
        }

        public void setIoThread(int ioThread) {
            this.ioThread = ioThread;
        }

        public int getWorkThread() {
            return workThread;
        }

        public void setWorkThread(int workThread) {
            this.workThread = workThread;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public boolean isH2() {
            return h2;
        }

        public void setH2(boolean h2) {
            this.h2 = h2;
        }

        public int getIdleReadTimeOutSecond() {
            return idleReadTimeOutSecond;
        }

        public void setIdleReadTimeOutSecond(int idleReadTimeOutSecond) {
            this.idleReadTimeOutSecond = idleReadTimeOutSecond;
        }

        public int getIdleWriteTimeoutSecond() {
            return idleWriteTimeoutSecond;
        }

        public void setIdleWriteTimeoutSecond(int idleWriteTimeoutSecond) {
            this.idleWriteTimeoutSecond = idleWriteTimeoutSecond;
        }

        public long getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(long bufferSize) {
            this.bufferSize = bufferSize;
        }
    }
}
