package io.elves.core.command;


import io.elves.core.request.HttpMethod;

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
public @interface CommandMapping {
    /**
     * command desc
     */
    String desc = "";

    /**
     * command name
     */
    String name();

    /**
     * get,post,put
     */
    HttpMethod httpMethod();
}
