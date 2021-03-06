package io.elves.core.properties;

import io.elves.common.util.Assert;
import io.elves.core.ElvesConstants;
import io.elves.core.scope.ElvesScopeInit;
import io.elves.core.scope.ElvesScopeApplicationContext;
import io.elves.core.scope.ElvesScopeAction;
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
    private static Properties properties;
    private static boolean isLoad = false;

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

    public static String getProfilesActive() {
        return ELVES_PROPERTIES.getProfilesActive();
    }

    public static Properties getProperties() {
        checkIsLoadProperties();
        return properties;
    }

    public static <T> T valueOf(Object key, Object defaultValue) {
        checkIsLoadProperties();
        return (T) properties.getOrDefault(key, defaultValue);
    }

    public static Boolean valueOfBoolean(String key, String defaultValue) {
        checkIsLoadProperties();
        return Boolean.parseBoolean(properties.getProperty(key, defaultValue));
    }

    public static int valueOfInteger(String key, String defaultValue) {
        checkIsLoadProperties();
        return Integer.parseInt(properties.getProperty(key, defaultValue));
    }

    public static long valueOfLong(String key, String defaultValue) {
        checkIsLoadProperties();
        return Long.parseLong(properties.getProperty(key, defaultValue));
    }

    public static String valueOfString(String key, String defaultValue) {
        checkIsLoadProperties();
        return properties.getProperty(key, defaultValue);
    }

    public static String valueOfStringWithAssertNotNull(String key) {
        checkIsLoadProperties();
        String val = properties.getProperty(key);
        Assert.notNull(val, String.format("key : %s not found", key));
        return val;
    }

    public static String getProfileActive() {
        checkIsLoadProperties();
        return properties.getProperty(ElvesConstants.PROFILES_ACTIVE);
    }

    public static void load() throws IOException {
        if (ELVES_PROPERTIES.isLoad() || isLoad) {
            return;
        }
        String profilesActive = System.getProperty(ElvesConstants.PROFILES_ACTIVE);
        log.debug("elves profiles active - {}", profilesActive);
        String fileName = String.format("elves-%s.properties", profilesActive);
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (url == null) {
            throw new IllegalArgumentException(String.format("this profile active %s invalid", profilesActive));
        }
        properties = new Properties(System.getProperties());
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            properties.load(stream);
            parseProperties(properties);
            isLoad = true;
            log.debug("properties load done total item count {}", properties.size());
        } catch (IOException e) {
            isLoad = false;
            throw e;
        }
    }

    private static void parseProperties(Properties properties) {
        ELVES_PROPERTIES.setH2(Boolean.parseBoolean(properties.getProperty("elves.server.h2", "false")));
        ELVES_PROPERTIES.setIoThread(Integer.parseInt(properties.getProperty("elves.server.ioThread"
                , "" + Runtime.getRuntime().availableProcessors())));
        ELVES_PROPERTIES.setWorkThread(Integer.parseInt(properties.getProperty("elves.server.workThread"
                , "" + Runtime.getRuntime().availableProcessors() * 2)));
        ELVES_PROPERTIES.setCharset(properties.getProperty("elves.server.charset", "UTF-8"));
        ELVES_PROPERTIES.setIdleReadTimeOutSecond(
                Integer.parseInt(properties.getProperty("elves.server.idleReadTimeOutSecond", "45")));
        ELVES_PROPERTIES.setIdleWriteTimeoutSecond(
                Integer.parseInt(properties.getProperty("elves.server.idleWriteTimeoutSecond", "0")));
        ELVES_PROPERTIES.setBufferSize(Long.parseLong(properties.getProperty("elves.server.bufferSize"
                , "" + 10 * 1024 * 1024)));
        ELVES_PROPERTIES.setHttpPort(Integer.parseInt(properties.getProperty("elves.server.port"
                , "" + DEFAULT_PORT)));
        ELVES_PROPERTIES.setSslPort(Integer.parseInt(properties.getProperty("elves.server.sslPort"
                , "" + (DEFAULT_PORT + 1000))));
        ELVES_PROPERTIES.setMaxRequestContentSize(Integer.parseInt(
                properties.getProperty("elves.server.maxRequestContentSize", "" + 10 * 1024 * 1024)));

        ELVES_PROPERTIES.setProfilesActive(properties.getProperty("elves.profiles.active"));
        ELVES_PROPERTIES.setLoad(true);
    }

    private static void checkIsLoadProperties() {
        if (!isLoad) {
            throw new RuntimeException("please load properties.");
        }
    }

    @ElvesScopeAction
    public void registerProperties() {
        ElvesScopeApplicationContext.register(() -> new ElvesScopeInit() {
            @Override
            public void postconstruct() {

            }

            @Override
            public void destroy() {

            }

            @Override
            public int sort() {
                return Integer.MIN_VALUE + 2;
            }
        });
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
        private String profilesActive;

        public String getProfilesActive() {
            return profilesActive;
        }

        public void setProfilesActive(String profilesActive) {
            this.profilesActive = profilesActive;
        }

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
