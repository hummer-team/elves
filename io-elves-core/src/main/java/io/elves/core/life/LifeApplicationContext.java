package io.elves.core.life;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;


/**
 * @author edz
 */
@Slf4j
public class LifeApplicationContext {
    private static final List<Life> LIST = Lists.newArrayListWithCapacity(16);

    private LifeApplicationContext() {

    }

    public static void register(Life life) {
        LIST.add(life);
    }

    public static void postconstruct() {
        LIST.sort(Comparator.comparing(Life::sort));
        for (Life life : LIST) {
            life.postconstruct();
            log.debug("life container {} execute done.", life);
        }
    }

    public static void destroy() {
        LIST.sort(Comparator.comparing(Life::sort));
        for (Life life : LIST) {
            life.destroy();
            log.debug("life container {} destroy done.", life);
        }
    }

    public static void register(Supplier<Life> supplier) {
        register(supplier.get());
    }
}
