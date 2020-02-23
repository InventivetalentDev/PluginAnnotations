package org.inventivetalent.pluginannotations.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface MessageBase {

	String file() default "config.yml";

	String basePath() default "";

	boolean allowLinks() default true;

	Class<? extends MessageFormatter> formatter() default MessageFormatter.class;

}
