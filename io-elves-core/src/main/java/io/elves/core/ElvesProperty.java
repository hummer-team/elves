package io.elves.core;

import static io.elves.core.ElvesConstants.DEFAULT_PORT;

/**
 * property
 *
 * @author lee
 */
public class ElvesProperty {
    private int ioThread;
    private int workThread;
    private String charset;

    public static int getIoThread() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static int getWorkThread() {
        return Runtime.getRuntime().availableProcessors() * 2;
    }

    public static int getPort() {
        return DEFAULT_PORT;
    }

    public static int getSslPort() {
        return DEFAULT_PORT + 1;
    }

    public String getCharset() {
        return charset;
    }
}
