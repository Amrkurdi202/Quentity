package com.quentity.entity.annotions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for icons putting it on top of entity will specify that entity Icon.
 * SVG files are supported only
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Icon {
    String value() default "doc.png";
}
