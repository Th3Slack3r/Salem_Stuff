package org.latikai.bots;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface BotAnnotation {
   String bot();

   String step();
}
