package org.inventivetalent.pluginannotations.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({
		ElementType.METHOD,
		ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageValue {

	String path();

	String defaultsTo() default "";

	char colorChar() default '&';

	boolean allowLinks() default true;

	Class<? extends MessageFormatter> formatter() default MessageFormatter.class;

}
