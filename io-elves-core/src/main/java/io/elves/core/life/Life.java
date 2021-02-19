package io.elves.core.life;


/**
 * @author edz
 */
public interface Life {
    void postconstruct();

    void destroy();

    int sort();
}
