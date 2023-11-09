package com.oracle.oci.intellij.ui.appstack.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface VariableMetaData {

    String title() ;      // Variable name
    boolean required() default false ; // Is the variable required?
    String defaultVal() default ""; // Default value for the variable
    String type() ;        // Data type of the variable
   String visible() default "";
    String dependsOn() default "";
    String description() default "";
    String enumValues() default "";


}
