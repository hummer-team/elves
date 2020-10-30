package io.elves.core.command;


import io.elves.core.request.HttpMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.elves.core.ElvesConstants.TEXT_PLAIN_CODER;

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
    String desc() default "";

    /**
     * if <code>true</code> then run at thread pool ,else run at main thread.
     */
    boolean async() default false;

    /**
     * get,post,put
     */
    HttpMethod httpMethod() default HttpMethod.GET;

    /**
     * command name
     */
    String name();

    /**
     * set response object encoder format,default is {@link io.elves.core.ElvesConstants#TEXT_PLAIN_CODER}
     * {@link io.elves.core.ElvesConstants#TEXT_PLAIN_CODER}
     * {@link io.elves.core.ElvesConstants#JSON_CODER}
     */
    String respEncoderType() default TEXT_PLAIN_CODER;
}
