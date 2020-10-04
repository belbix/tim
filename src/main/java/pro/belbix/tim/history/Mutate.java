package pro.belbix.tim.history;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Mutate {
    double value();

    double max() default 0;

    double min() default 0;

    double chance() default 0.5;
}
