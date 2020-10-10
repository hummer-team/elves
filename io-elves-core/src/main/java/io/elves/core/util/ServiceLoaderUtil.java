package io.elves.core.util;

import java.util.ServiceLoader;

public class ServiceLoaderUtil {
    private ServiceLoaderUtil() {

    }

    public static <S> ServiceLoader<S> getServiceLoader(Class<S> clazz) {
        return ServiceLoader.load(clazz, clazz.getClassLoader());
    }
}
