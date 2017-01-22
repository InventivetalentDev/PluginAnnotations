package org.inventivetalent.pluginannotations.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({
				ElementType.METHOD,
				ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {

	String path();

	String defaultsTo() default "";

	char colorChar() default ' ';

}
