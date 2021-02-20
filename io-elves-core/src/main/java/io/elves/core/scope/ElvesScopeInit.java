package io.elves.core.scope;


/**
 * @author edz
 */
public interface ElvesScopeInit {
    void postconstruct();

    void destroy();

    int sort();
}
