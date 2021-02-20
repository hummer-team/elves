package io.elves.core.scope;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;


/**
 * @author edz
 */
@Slf4j
public class ElvesScopeApplicationContext {
    private static final List<ElvesScopeInit> LIST = Lists.newArrayListWithCapacity(16);

    private ElvesScopeApplicationContext() {

    }

    public static void register(ElvesScopeInit elvesScopeInit) {
        LIST.add(elvesScopeInit);
    }

    public static void postconstruct() {
        LIST.sort(Comparator.comparing(ElvesScopeInit::sort));
        for (ElvesScopeInit elvesScopeInit : LIST) {
            elvesScopeInit.postconstruct();
            log.debug("life container {} execute done.", elvesScopeInit);
        }
    }

    public static void destroy() {
        LIST.sort(Comparator.comparing(ElvesScopeInit::sort));
        for (ElvesScopeInit elvesScopeInit : LIST) {
            elvesScopeInit.destroy();
            log.debug("life container {} destroy done.", elvesScopeInit);
        }
    }

    public static void register(Supplier<ElvesScopeInit> supplier) {
        register(supplier.get());
    }
}
