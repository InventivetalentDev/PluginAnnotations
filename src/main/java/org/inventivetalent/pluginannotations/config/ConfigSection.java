package org.inventivetalent.pluginannotations.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by shell on 2017/12/8.
 * <p>
 * Github: https://github.com/shellljx
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigSection {

    String path();
}
