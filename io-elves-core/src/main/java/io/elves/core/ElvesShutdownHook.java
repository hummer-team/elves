package io.elves.core;

import com.google.common.collect.Lists;

import java.util.List;

public class ElvesShutdownHook {
    private static final List<Runnable> THREADS = Lists.newArrayListWithCapacity(3);

    private ElvesShutdownHook() {

    }

    public static void register(Runnable runnable) {
        THREADS.add(runnable);
    }

    static List<Runnable> getAllHoots() {
        return THREADS;
    }
}
