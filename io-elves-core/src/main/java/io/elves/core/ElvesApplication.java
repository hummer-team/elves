package io.elves.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lee
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ElvesApplication {
    /**
     * target package
     */
    String scanPackage() default "";

    /**
     * boot start class
     */
    Class<? extends ElvesServer> bootServer();
}
