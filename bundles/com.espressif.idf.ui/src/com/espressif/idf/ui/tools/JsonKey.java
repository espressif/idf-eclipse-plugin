package com.espressif.idf.ui.tools;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Annotation to apply a json key to the vos
 * @author Ali Azam Rana
 *
 */
public @interface JsonKey
{
	/**
	 * the json key name associated with the field
	 * @return JSON Key name associated with the field
	 */
	String key_name();
}
