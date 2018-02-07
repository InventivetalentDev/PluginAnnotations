package org.inventivetalent.pluginannotations.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by shell on 2017/12/15.
 * <p>
 * Github: https://github.com/shellljx
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigBean {

    String file() default "config.yml";

}
